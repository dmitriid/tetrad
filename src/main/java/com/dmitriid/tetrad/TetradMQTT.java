package com.dmitriid.tetrad;

import com.dmitriid.tetrad.services.FirehoseMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static java.lang.System.exit;

public class TetradMQTT implements MqttCallback {
    private TetradMQTTConfig mqttConfig;
    private MqttAsyncClient mqttSession;
    private TetradCallback callback;

    public TetradMQTT(JsonNode config){
        mqttConfig = new TetradMQTTConfig(config);
    }

    public void start(TetradCallback callback){
        try {
            this.callback = callback;
            mqttSession = new MqttAsyncClient(mqttConfig.getBroker(),
                                              mqttConfig.getClientid(),
                                              new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            mqttSession.connect(connOpts, new TetradMQTTConnectionListener(this));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void onSuccessfulConnect(){
        mqttSession.setCallback(this);
        try {
            mqttSession.subscribe(mqttConfig.getSubscribe().getTopic(),
                                  mqttConfig.getSubscribe().getQos());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        cause.printStackTrace();

/*
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);

        try {
            mqttSession.connect(connOpts, new TetradMQTTConnectionListener(this));
        } catch (MqttException e) {
            e.printStackTrace();
        }
*/

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String msg = new String(message.getPayload());

        ObjectMapper mapper = new ObjectMapper();
        FirehoseMessage firehoseMessage = mapper.readValue(msg, FirehoseMessage.class);

        callback.execute(firehoseMessage);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public void sendMessage(FirehoseMessage firehoseMessage){
        ObjectMapper mapper = new ObjectMapper();

        try {
            byte[] content = mapper.writeValueAsBytes(firehoseMessage);
            MqttMessage message = new MqttMessage(content);
            message.setQos(mqttConfig.getFirehose().getQos());
            if (mqttSession.isConnected())
                mqttSession.publish(mqttConfig.getFirehose().getTopic(), message);
        } catch (MqttException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}

class TetradMQTTConfig {
    private String          clientid;
    private String          broker;
    private TetradMQTTTopic firehose;
    private TetradMQTTTopic subscribe;

    public TetradMQTTConfig(JsonNode config){
        setClientid(config.at("/clientid").asText());
        setBroker(config.at("/broker").asText());
        setFirehose(new TetradMQTTTopic(config.at("/firehose")));
        setSubscribe(new TetradMQTTTopic(config.at("/subscribe")));
    }

    public String getClientid() {
        return clientid;
    }

    public void setClientid(String clientid) {
        this.clientid = clientid;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public TetradMQTTTopic getFirehose() {
        return firehose;
    }

    public void setFirehose(TetradMQTTTopic firehose) {
        this.firehose = firehose;
    }

    public TetradMQTTTopic getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(TetradMQTTTopic subscribe) {
        this.subscribe = subscribe;
    }
}

class TetradMQTTTopic {
    private String topic;
    private int    qos;

    TetradMQTTTopic(JsonNode config){
        setTopic(config.at("/topic").asText());
        setQos(config.at("/qos").asInt(0));
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }
}


class TetradMQTTConnectionListener implements IMqttActionListener{

    private TetradMQTT mqtt;

    TetradMQTTConnectionListener(TetradMQTT mqtt){
        this.mqtt = mqtt;
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        mqtt.onSuccessfulConnect();
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

    }
}
