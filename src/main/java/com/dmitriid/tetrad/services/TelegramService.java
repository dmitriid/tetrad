package com.dmitriid.tetrad.services;

import com.dmitriid.tetrad.adapters.TetradMQTT;
import com.dmitriid.tetrad.adapters.TetradTelegram;
import com.dmitriid.tetrad.interfaces.IManagedService;
import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class TelegramService implements IManagedService {
    private final Map<String, TetradTelegram> telegrams = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    private TetradMQTT mqtt;

    @Override
    public void init(ServiceConfiguration configuration) {
        logger.debug("Init");
        for (JsonNode config : configuration.getConfiguration().at("/telegram")) {
            telegrams.put(config.at("/identifier").asText(), new TetradTelegram(config));
        }
        mqtt = new TetradMQTT(configuration.getConfiguration().at("/mqtt"));
    }

    @Override
    public void start() {
        logger.debug("Start");
        mqtt.start(this::postToTelegram);
        telegrams.forEach((s, tetradTelegram) -> tetradTelegram.start(this.mqtt::sendMessage));
    }

    @Override
    public void shutdown() {

    }

    private void postToTelegram(FirehoseMessage firehoseMessage) {
/*
        if (!telegrams.containsKey(firehoseMessage.service)) {
            return;
        }

        telegrams.get(firehoseMessage.service).post(firehoseMessage);
*/
        logger.info("Publish to Telegram: " + firehoseMessage.toLogString());
        telegrams.forEach((s, tetradTelegram) -> tetradTelegram.post(firehoseMessage));
    }
}
