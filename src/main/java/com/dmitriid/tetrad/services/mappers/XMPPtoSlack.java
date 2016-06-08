package com.dmitriid.tetrad.services.mappers;

import com.dmitriid.tetrad.interfaces.ManagedService;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.dmitriid.tetrad.services.ServiceConfiguration;
import com.dmitriid.tetrad.services.ServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class XMPPtoSlack implements ManagedService {

    private MqttClient mqttSession;

    private String  mqttBrokerSub;
    private String  mqttClientIDSub;
    private String  mqttTopicSub;
    private Integer mqttQoSSub;

    private String  mqttBrokerPub;
    private String  mqttClientIDPub;
    private String  mqttTopicPub;
    private Integer mqttQoSPub;

    private Mapping mapping;

    @Override
    public void init(ServiceConfiguration configuration) throws ServiceException {
        this.mapping = new Mapping(configuration.getConfiguration().at("/mapping"));

        this.mqttBrokerSub = configuration.getConfiguration().at("/mqtt/subscribe/broker").asText();
        this.mqttClientIDSub = configuration.getConfiguration().at("/mqtt/subscribe/clientid").asText();
        this.mqttQoSSub = configuration.getConfiguration().at("/mqtt/subscribe/qos").asInt();
        this.mqttTopicSub = configuration.getConfiguration().at("/mqtt/subscribe/topic").asText();

        this.mqttBrokerPub = configuration.getConfiguration().at("/mqtt/publish/broker").asText();
        this.mqttClientIDPub = configuration.getConfiguration().at("/mqtt/publish/clientid").asText();
        this.mqttQoSPub = configuration.getConfiguration().at("/mqtt/publish/qos").asInt();
        this.mqttTopicPub = configuration.getConfiguration().at("/mqtt/publish/topic").asText();
    }

    @Override
    public void start() throws ServiceException {
        try {
            mqttSession = new MqttClient(mqttBrokerSub, mqttClientIDSub, new MemoryPersistence());
            mqttSession.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    String msg = new String(message.getPayload());

                    ObjectMapper mapper = new ObjectMapper();
                    FirehoseMessage firehoseMessage = mapper.readValue(msg, FirehoseMessage.class);

                    Match match = mapping.match(firehoseMessage);
                    if(match == null) return;

                    FirehoseMessage out = new FirehoseMessage("xmpp",
                                                              "groupchat",
                                                              "dmitriid",
                                                              match.getSlackService(),
                                                              match.getslackRoom(),
                                                              firehoseMessage.content
                                                              );

                    byte[] content = mapper.writeValueAsBytes(msg);
                    MqttMessage outMessage = new MqttMessage(content);
                    outMessage.setQos(mqttQoSPub);

                    mqttSession.publish(mqttTopicPub, outMessage);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
            this.connectMQTT();
            mqttSession.subscribe(mqttTopicSub);
        } catch (MqttException e) {
            e.printStackTrace();
            throw ServiceException.RunException(e.getMessage());
        }
    }

    @Override
    public void shutdown() throws ServiceException {

    }

    private void connectMQTT() {
        try {
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to mqttBrokerSub: " + mqttBrokerSub);
            mqttSession.connect(connOpts);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}

