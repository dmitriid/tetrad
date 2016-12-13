/*******************************************************************************
 * Copyright (c) 2016 Dmitrii "Mamut" Dimandt <dmitrii@dmitriid.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

package com.dmitriid.tetrad.services;

import com.dmitriid.tetrad.TetradObjectFactory;
import com.dmitriid.tetrad.adapters.TetradMQTT;
import com.dmitriid.tetrad.interfaces.IManagedService;
import com.dmitriid.tetrad.interfaces.ITransformer;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class MappingService implements IManagedService {
    private final List<ITransformer> transformers = new ArrayList<>();
    private List<TetradMap> mapping;
    private TetradMQTT mqtt;


    @Override
    public void init(ServiceConfiguration configuration) {
        mqtt = new TetradMQTT(configuration.getConfiguration().at("/mqtt"));

        mapping = new ArrayList<>();
        for (JsonNode map : configuration.getConfiguration().at("/mapping")) {
            mapping.add(new TetradMap(map));
        }

        for (JsonNode transform : configuration.getConfiguration().at("/transformations")) {
            transformers.add(TetradObjectFactory.getTransformer(transform.asText()));
        }
    }

    @Override
    public void start() {
        mqtt.start(this::firehose);
    }

    @Override
    public void shutdown() {

    }

    private void firehose(FirehoseMessage firehoseMessage) {
        mapping.forEach(tetradMap -> {
            if (tetradMap.matches(firehoseMessage)) {
                FirehoseMessage msg = tetradMap.convert(firehoseMessage);

                for (ITransformer transfomer : transformers) {
                    msg = transfomer.transform(msg);
                }

                mqtt.sendMessage(msg);
            }
        });
    }
}

class TetradMap {
    private final String fromService;
    private final String fromChannel;
    private final String fromType;
    private final String fromSubtype;

    private final String toService;
    private final String toChannel;
    private final String toType;
    private final String toSubtype;

    private final Boolean shortUserNames;

    TetradMap(JsonNode config) {
        fromService = config.at("/from_service").asText(null);
        fromChannel = config.at("/from_channel").asText(null);
        fromType = config.at("/from_type").asText(null);
        fromSubtype = config.at("/from_subtype").asText(null);

        toService = config.at("/to_service").asText(null);
        toChannel = config.at("/to_channel").asText(null);
        toType = config.at("/to_type").asText(null);
        toSubtype = config.at("/to_subtype").asText(null);

        shortUserNames = config.at("/short_username").asBoolean(false);
    }

    Boolean matches(FirehoseMessage firehoseMessage) {
        return !(fromService != null && !fromService.equals(firehoseMessage.service))
                && !(fromChannel != null && !fromChannel.equals(firehoseMessage.channel))
                && !(fromType != null && !fromType.equals(firehoseMessage.type))
                && !(fromSubtype != null && !fromSubtype.equals(firehoseMessage.subtype));

    }

    FirehoseMessage convert(FirehoseMessage firehoseMessage) {
        FirehoseMessage fm = (FirehoseMessage) firehoseMessage.clone();

        if (toService != null) {
            fm.service = toService;
        }
        if (toChannel != null) {
            fm.channel = toChannel;
        }
        if (toType != null) {
            fm.type = toType;
        }
        if (toSubtype != null) {
            fm.subtype = toSubtype;
        }

        /*if (shortUserNames) {
            fm.user = JIDUtils.jid_to_slack_username(fm.user);
        }*/

        return fm;
    }
}
