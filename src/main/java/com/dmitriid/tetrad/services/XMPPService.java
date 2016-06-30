package com.dmitriid.tetrad.services;

import com.dmitriid.tetrad.adapters.TetradMQTT;
import com.dmitriid.tetrad.adapters.TetradXMPP;
import com.dmitriid.tetrad.interfaces.IManagedService;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.dmitriid.tetrad.services.ServiceConfiguration;
import com.dmitriid.tetrad.services.ServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class XMPPService implements IManagedService {
    private final Map<String, TetradXMPP> xmpps = new HashMap<>();
    private TetradMQTT mqtt;
    private Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

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
        logger.info("postToSlack: " + firehoseMessage.toLogString());
        if (!xmpps.containsKey(firehoseMessage.service)) {
            logger.info("Service " + firehoseMessage.service + " not found");
            return;
        }

        xmpps.get(firehoseMessage.service).post(firehoseMessage);
    }

}
