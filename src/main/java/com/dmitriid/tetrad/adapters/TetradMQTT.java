package com.dmitriid.tetrad.adapters;

import com.dmitriid.tetrad.interfaces.ITetradCallback;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TetradMQTT implements MqttCallback {
  private final TetradMQTTConfig mqttConfig;
  private       MqttAsyncClient  mqttSession;
  private       ITetradCallback  callback;
  private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

  public TetradMQTT(JsonNode config){
    mqttConfig = new TetradMQTTConfig(config);
  }

  private void connect(){
    logger.debug(MessageFormat.format("Connecting to {0} as {1}", mqttConfig.getBroker(), mqttConfig.getClientid()));
    try {
      mqttSession = new MqttAsyncClient(mqttConfig.getBroker(),
                                        mqttConfig.getClientid(),
                                        new MemoryPersistence());
      MqttConnectOptions connOpts = new MqttConnectOptions();
      connOpts.setCleanSession(true);

      mqttSession.connect(connOpts, new TetradMQTTConnectionListener(this));
    } catch (MqttException e) {
      logger.error(MessageFormat.format("Error connecting to {0} as {1}. Message: {2}, ReasonCode: {3}",
                                        mqttConfig.getBroker(),
                                        mqttConfig.getClientid(),
                                        e.getMessage(),
                                        e.getReasonCode()
                                       ));
      e.printStackTrace();
    }

  }
  public void start(ITetradCallback callback){
    this.callback = callback;
    this.connect();
  }

  void onSuccessfulConnect(){
    logger.debug(MessageFormat.format("Successfully connected to {0} as {1}", mqttConfig.getBroker(), mqttConfig.getClientid()));
    logger.debug(MessageFormat.format("Subscribing to {0} with qos {1}", mqttConfig.getSubscribe().getTopic(), mqttConfig.getSubscribe().getQos()));
    mqttSession.setCallback(this);
    try {
      mqttSession.subscribe(mqttConfig.getSubscribe().getTopic(),
                            mqttConfig.getSubscribe().getQos());
    } catch (MqttException e) {
      logger.error(MessageFormat.format("Error connecting to {0} with qos {1}. Message: {2}, ReasonCode: {3}",
                                        mqttConfig.getSubscribe().getTopic(),
                                        mqttConfig.getSubscribe().getQos(),
                                        e.getMessage(),
                                        e.getReasonCode()
                                       ));
    }
  }

  @Override
  public void connectionLost(Throwable cause) {
    logger.info(MessageFormat.format("Lost connection to {0} as {1}. Message: {2}",
                                     mqttConfig.getBroker(),
                                     mqttConfig.getClientid(),
                                     cause.getMessage()
                                    ));
    this.connect();
  }

  @Override
  public void messageArrived(String topic, MqttMessage message) {

    String originalMessage = Arrays.stream(new String(message.getPayload()).split("\n")).collect(Collectors.joining(" "));

    logger.info(MessageFormat.format("Got message from subscription {0}: {1}",
                                     mqttConfig.getSubscribe().getTopic(),
                                     originalMessage
                                    ));
    if(callback == null){
      return;
    }
    String msg = new String(message.getPayload());

    try {
      ObjectMapper mapper = new ObjectMapper();
      FirehoseMessage firehoseMessage = mapper.readValue(msg, FirehoseMessage.class);
      callback.execute(firehoseMessage);
    } catch (IOException e) {
      logger.error(MessageFormat.format("Error deserializing message. Message: {0}, Original Message: {2}",
                                        e.getMessage(),
                                        originalMessage));
    }

  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {

  }

  public void sendMessage(FirehoseMessage firehoseMessage){

    logger.info(MessageFormat.format("Publish message to {0}: {1}",
                                     mqttConfig.getFirehose().getTopic(),
                                     firehoseMessage.toLogString()
                                    ));

    ObjectMapper mapper = new ObjectMapper();

    try {
      byte[] content = mapper.writeValueAsBytes(firehoseMessage);
      MqttMessage message = new MqttMessage(content);
      message.setQos(mqttConfig.getFirehose().getQos());
      if (mqttSession.isConnected())
        mqttSession.publish(mqttConfig.getFirehose().getTopic(), message);
    } catch (MqttException | JsonProcessingException e) {
      logger.error(MessageFormat.format("Error publishing message to {0}: Message: {1} OriginalMessage: {2}",
                                        mqttConfig.getFirehose().getTopic(),
                                        e.getMessage(),
                                        firehoseMessage.toLogString()
                                       ));
    }
  }
}

class TetradMQTTConfig {
  private String          clientid;
  private String          broker;
  private TetradMQTTTopic firehose;
  private TetradMQTTTopic subscribe;

  TetradMQTTConfig(JsonNode config){
    setClientid(config.at("/clientid").asText());
    setBroker(config.at("/broker").asText());
    setFirehose(new TetradMQTTTopic(config.at("/firehose")));
    setSubscribe(new TetradMQTTTopic(config.at("/subscribe")));
  }

  public String getClientid() {
    return clientid;
  }

  private void setClientid(String clientid) {
    this.clientid = clientid;
  }

  public String getBroker() {
    return broker;
  }

  private void setBroker(String broker) {
    this.broker = broker;
  }

  public TetradMQTTTopic getFirehose() {
    return firehose;
  }

  private void setFirehose(TetradMQTTTopic firehose) {
    this.firehose = firehose;
  }

  public TetradMQTTTopic getSubscribe() {
    return subscribe;
  }

  private void setSubscribe(TetradMQTTTopic subscribe) {
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

  private void setTopic(String topic) {
    this.topic = topic;
  }

  public int getQos() {
    return qos;
  }

  private void setQos(int qos) {
    this.qos = qos;
  }
}


class TetradMQTTConnectionListener implements IMqttActionListener{

  private final TetradMQTT mqtt;

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
