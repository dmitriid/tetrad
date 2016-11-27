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

import com.dmitriid.tetrad.adapters.TetradIRC;
import com.dmitriid.tetrad.adapters.TetradMQTT;
import com.dmitriid.tetrad.interfaces.IManagedService;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class IRCService implements IManagedService {
    private final Map<String, TetradIRC> ircs   = new HashMap<>();
    private final Logger                 logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    private TetradMQTT mqtt;

    @Override
    public void init(ServiceConfiguration configuration) {
        logger.debug("Init");
        for (JsonNode config : configuration.getConfiguration().at("/irc")) {
            ircs.put(config.at("/server").asText(), new TetradIRC(config));
        }
        mqtt = new TetradMQTT(configuration.getConfiguration().at("/mqtt"));
    }

    @Override
    public void start() {
        logger.debug("Start");
        mqtt.start(this::postToIRC);
        ircs.forEach((s, tetradIRC) -> tetradIRC.start(this.mqtt::sendMessage));
    }

    @Override
    public void shutdown() {

    }

    private void postToIRC(FirehoseMessage firehoseMessage) {
        logger.info("Publish to IRC: " + firehoseMessage.toLogString());
        ircs.forEach((s, tetradIRC) -> tetradIRC.post(firehoseMessage));
    }
}
