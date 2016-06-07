package com.dmitriid.tetrad.services.slack;

import com.dmitriid.tetrad.interfaces.ManagedService;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.dmitriid.tetrad.services.ServiceConfiguration;
import com.dmitriid.tetrad.services.ServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackConnected;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackConnectedListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class FirehoseService implements ManagedService {
  private List<String> slackChannels = new ArrayList<>();
  private String slackBotId;
  private String identifier;

  private SlackSession slackSession;
  private MqttClient mqttSession;

  private String mqttBroker;
  private String mqttClientID;
  private String mqttTopic;
  private Integer mqttQoS;

  public FirehoseService(){
  }


  public void init(ServiceConfiguration configuration) throws ServiceException {
    //while(!)
    JsonNode channels = configuration.getConfiguration().at("/slack/channels");

    for(final JsonNode channel : channels){
      slackChannels.add(channel.asText());
    }
    //this.slackTopic = ((ArrayNode)configuration.getConfiguration().at("/slack/channels")).asL
    this.slackBotId = configuration.getConfiguration().at("/slack/botid").asText();
    this.identifier = configuration.getConfiguration().at("/slack/identifier").asText();

    this.mqttBroker = configuration.getConfiguration().at("/mqtt/broker").asText();
    this.mqttClientID = configuration.getConfiguration().at("/mqtt/clientid").asText();
    this.mqttQoS = configuration.getConfiguration().at("/mqtt/qos").asInt();
    this.mqttTopic = configuration.getConfiguration().at("/mqtt/topic").asText();
  }

  public void start() throws ServiceException {
    slackSession = SlackSessionFactory.createWebSocketSlackSession(slackBotId);

    try {
      mqttSession = new MqttClient(mqttBroker, mqttClientID, new MemoryPersistence());
      slackSession.addMessagePostedListener(new PostHandler(this::firehose, identifier));
      slackSession.connect();
      while(slackSession.isConnected()){}
    }catch (IOException | MqttException e) {
      e.printStackTrace();
      throw ServiceException.RunException(e.getMessage());
    }
  }

  private void firehose(FirehoseMessage msg) {
    ObjectMapper mapper = new ObjectMapper();

    try{
      byte[] content = mapper.writeValueAsBytes(msg);
      MqttMessage message = new MqttMessage(content);
      message.setQos(mqttQoS);
      if(!mqttSession.isConnected()) this.connectMQTT();
      if(mqttSession.isConnected()) mqttSession.publish(mqttTopic, message);
    } catch (MqttException | JsonProcessingException e) {
      e.printStackTrace();
      exit(1);
    }
  }

  public void shutdown() {
    try {
      if (mqttSession.isConnected()) mqttSession.disconnect();
      if (slackSession.isConnected()) slackSession.disconnect();
    } catch (IOException | MqttException e) {
      e.printStackTrace();
    }
  }

  private void connectMQTT() {
    MqttConnectOptions connOpts = new MqttConnectOptions();
    connOpts.setCleanSession(true);
    System.out.println("Connecting to mqttBroker: " + mqttBroker);
    try {
      mqttSession.connect(connOpts);
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }
}
