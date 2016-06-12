package com.dmitriid.tetrad.services;

import com.dmitriid.tetrad.adapters.TetradMQTT;
import com.dmitriid.tetrad.adapters.TetradSlack;
import com.dmitriid.tetrad.interfaces.IManagedService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class SlackService implements IManagedService {

    private final Map<String, TetradSlack> slacks = new HashMap<>();
    private TetradMQTT mqtt;

    @Override
    public void init(ServiceConfiguration configuration) throws ServiceException {
        for(JsonNode node : configuration.getConfiguration().at("/slack")){
            TetradSlack slack = new TetradSlack(node);
            slacks.put(slack.getIdentifier(), slack);
        }

        mqtt = new TetradMQTT(configuration.getConfiguration().at("/mqtt"));
    }

    @Override
    public void start() throws ServiceException {
        slacks.forEach((s, slack) -> slack.run(this.mqtt::sendMessage));
        mqtt.start(this::postToSlack);
    }

    @Override
    public void shutdown() throws ServiceException {

    }

    private void postToSlack(FirehoseMessage firehoseMessage){
        if(!slacks.containsKey(firehoseMessage.service)){
            return;
        }

        slacks.get(firehoseMessage.service).post(firehoseMessage);
    }
}



