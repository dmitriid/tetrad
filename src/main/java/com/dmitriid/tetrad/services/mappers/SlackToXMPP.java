package com.dmitriid.tetrad.services.mappers;

import com.dmitriid.tetrad.interfaces.ManagedService;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.dmitriid.tetrad.services.ServiceConfiguration;
import com.dmitriid.tetrad.services.ServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;

public class SlackToXMPP implements ManagedService, MqttCallback {

    private MqttAsyncClient mqttSession;

    private String  mqttBrokerSub;
    private String  mqttClientIDSub;
    private String  mqttTopicSub;

    private String  mqttTopicPub;
    private Integer mqttQoSPub;

    private Mapping mapping;

    @Override
    public void init(ServiceConfiguration configuration) throws ServiceException {
        this.mapping = new Mapping(configuration.getConfiguration().at("/mapping"), "slack", "xmpp");

        this.mqttBrokerSub = configuration.getConfiguration().at("/mqtt/subscribe/broker").asText();
        this.mqttClientIDSub = configuration.getConfiguration().at("/mqtt/subscribe/clientid").asText();
        this.mqttTopicSub = configuration.getConfiguration().at("/mqtt/subscribe/topic").asText();

        this.mqttQoSPub = configuration.getConfiguration().at("/mqtt/publish/qos").asInt();
        this.mqttTopicPub = configuration.getConfiguration().at("/mqtt/publish/topic").asText();
    }

    @Override
    public void start() throws ServiceException {
        this.connectMQTT();
    }

    private void mapToXMPP(MqttMessage message){
        try {
            final String msg = new String(message.getPayload());

            final ObjectMapper mapper = new ObjectMapper();
            final FirehoseMessage firehoseMessage = mapper.readValue(msg, FirehoseMessage.class);

            final Match match = mapping.match(firehoseMessage);
            if (match == null) return;

            final FirehoseMessage out = new FirehoseMessage(firehoseMessage.type,
                                                            firehoseMessage.subtype,
                                                            firehoseMessage.user + "@" + firehoseMessage.service,
                                                            match.getService(),
                                                            match.getRoom(),
                                                            firehoseMessage.content
            );

            final byte[] content = mapper.writeValueAsBytes(out);
            MqttMessage outMessage = new MqttMessage(content);
            outMessage.setQos(mqttQoSPub);

            try {
                mqttSession.publish(mqttTopicPub, outMessage);
            } catch(Exception e){
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() throws ServiceException {

    }

    private void connectMQTT() {
        try {
            mqttSession = new MqttAsyncClient(mqttBrokerSub, mqttClientIDSub, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to mqttBrokerSub: " + mqttBrokerSub);

            SlackToXMPP t = this;

            mqttSession.connect(connOpts, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqttSession.setCallback(t);
                    try {
                        mqttSession.subscribe(mqttTopicSub, mqttQoSPub);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        cause.printStackTrace();
        System.out.println(cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println(topic);
        mapToXMPP(message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}

