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

import com.dmitriid.tetrad.adapters.TetradMQTT;
import com.dmitriid.tetrad.adapters.TetradXMPP;
import com.dmitriid.tetrad.interfaces.IManagedService;
import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class XMPPService implements IManagedService {
    private final Map<String, TetradXMPP> xmpps = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    private TetradMQTT mqtt;

    @Override
    public void init(ServiceConfiguration configuration) {
        logger.debug("Init");
        for (JsonNode node : configuration.getConfiguration().at("/xmpp")) {
            TetradXMPP xmpp = new TetradXMPP(node);
            xmpps.put(xmpp.getChatService(), xmpp);
        }

        mqtt = new TetradMQTT(configuration.getConfiguration().at("/mqtt"));
    }

    @Override
    public void start() {
        logger.debug("Start");
        xmpps.forEach((s, tetradXMPP) -> tetradXMPP.start(this.mqtt::sendMessage));
        mqtt.start(this::postToXMPP);
    }

    @Override
    public void shutdown() {

    }

    private void postToXMPP(FirehoseMessage firehoseMessage) {
        logger.info("Publish to XMPP: " + firehoseMessage.toLogString());
        if (!xmpps.containsKey(firehoseMessage.service)) {
            logger.info("Service " + firehoseMessage.service + " not found");
            return;
        }

        xmpps.get(firehoseMessage.service).post(firehoseMessage);
    }

}
