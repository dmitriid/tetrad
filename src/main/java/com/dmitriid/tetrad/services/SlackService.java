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
import com.dmitriid.tetrad.adapters.TetradSlack;
import com.dmitriid.tetrad.interfaces.IManagedService;
import com.dmitriid.tetrad.interfaces.ITransformer;
import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlackService implements IManagedService {

    private final Map<String, TetradSlack> slacks = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    private TetradMQTT mqtt;

    @Override
    public void init(ServiceConfiguration configuration) {
        logger.debug("Init");

        List<ITransformer> transformers = new ArrayList<>();

        for (JsonNode transform : configuration.getConfiguration().at("/transformations")) {
            transformers.add(TetradObjectFactory.getTransformer(transform.asText()));
        }

        for (JsonNode node : configuration.getConfiguration().at("/slack")) {
            TetradSlack slack = new TetradSlack(node, transformers);
            slacks.put(slack.getIdentifier(), slack);
        }

        mqtt = new TetradMQTT(configuration.getConfiguration().at("/mqtt"));
    }

    @Override
    public void start() {
        logger.debug("Start");
        slacks.forEach((s, slack) -> slack.run(this.mqtt::sendMessage));
        mqtt.start(this::postToSlack);
    }

    @Override
    public void shutdown() {
        logger.debug("Shutdown");
    }

    private void postToSlack(FirehoseMessage firehoseMessage) {
        logger.info("Publish to slack: " + firehoseMessage.toLogString());
        if (!slacks.containsKey(firehoseMessage.service)) {
            logger.info("Service " + firehoseMessage.service + " not found");
            return;
        }

        slacks.get(firehoseMessage.service).post(firehoseMessage);
    }
}



