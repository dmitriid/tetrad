package com.dmitriid.tetrad.services;

import com.dmitriid.tetrad.TetradObjectFactory;
import com.dmitriid.tetrad.adapters.TetradMQTT;
import com.dmitriid.tetrad.adapters.TetradSlack;
import com.dmitriid.tetrad.interfaces.IManagedService;
import com.dmitriid.tetrad.interfaces.ITransformer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlackService implements IManagedService {

    private final Map<String, TetradSlack> slacks = new HashMap<>();
    private TetradMQTT mqtt;
    private Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    @Override
    public void init(ServiceConfiguration configuration) {
        logger.debug("Init");

        List<ITransformer> transformers = new ArrayList<>();

        for(JsonNode transform : configuration.getConfiguration().at("/transformations")) {
            transformers.add(TetradObjectFactory.getTransformer(transform.asText()));
        }

        for(JsonNode node : configuration.getConfiguration().at("/slack")){
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

    private void postToSlack(FirehoseMessage firehoseMessage){
        ObjectMapper mapper = new ObjectMapper();

        logger.info("postToSlack: " + firehoseMessage.toLogString());
        if(!slacks.containsKey(firehoseMessage.service)){
            logger.info("Service " + firehoseMessage.service + " not found");
            return;
        }

        slacks.get(firehoseMessage.service).post(firehoseMessage);
    }
}



