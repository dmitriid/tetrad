package com.dmitriid.tetrad;

import com.dmitriid.tetrad.interfaces.ManagedService;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.dmitriid.tetrad.services.ServiceConfiguration;
import com.dmitriid.tetrad.services.ServiceException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelegramService implements ManagedService {
    Map<String, TetradTelegram> telegrams = new HashMap<>();
    TetradMQTT mqtt;

    @Override
    public void init(ServiceConfiguration configuration) throws ServiceException {
        for(JsonNode config : configuration.getConfiguration().at("/telegram")){
            telegrams.put(config.at("/identifier").asText(), new TetradTelegram(config));
        }
        mqtt = new TetradMQTT(configuration.getConfiguration().at("/mqtt"));
    }

    @Override
    public void start() throws ServiceException {
        mqtt.start(this::postToTelegram);
        telegrams.forEach((s, tetradTelegram) -> tetradTelegram.start(this.mqtt::sendMessage));
    }

    @Override
    public void shutdown() throws ServiceException {

    }

    private void postToTelegram(FirehoseMessage firehoseMessage) {
/*
        if (!telegrams.containsKey(firehoseMessage.service)) {
            return;
        }

        telegrams.get(firehoseMessage.service).post(firehoseMessage);
*/
        telegrams.forEach((s, tetradTelegram) -> tetradTelegram.post(firehoseMessage));
    }
}
