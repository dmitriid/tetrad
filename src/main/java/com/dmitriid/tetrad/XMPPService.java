package com.dmitriid.tetrad;

import com.dmitriid.tetrad.interfaces.ManagedService;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.dmitriid.tetrad.services.ServiceConfiguration;
import com.dmitriid.tetrad.services.ServiceException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class XMPPService implements ManagedService{
    Map<String, TetradXMPP> xmpps = new HashMap<>();
    TetradMQTT mqtt;


    @Override
    public void init(ServiceConfiguration configuration) throws ServiceException {
        for (JsonNode node : configuration.getConfiguration().at("/xmpp")) {
            TetradXMPP xmpp = new TetradXMPP(node);
            xmpps.put(xmpp.getChatService(), xmpp);
        }

        mqtt = new TetradMQTT(configuration.getConfiguration().at("/mqtt"));
    }

    @Override
    public void start() throws ServiceException {
        xmpps.forEach((s, tetradXMPP) -> tetradXMPP.start(this.mqtt::sendMessage));
        mqtt.start(this::postToXMPP);
    }

    @Override
    public void shutdown() throws ServiceException {

    }

    private void postToXMPP(FirehoseMessage firehoseMessage) {
        if (!xmpps.containsKey(firehoseMessage.service)) {
            return;
        }

        xmpps.get(firehoseMessage.service).post(firehoseMessage);
    }

}
