package com.dmitriid.tetrad.services.slack;

import com.dmitriid.tetrad.interfaces.ManagedService;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.dmitriid.tetrad.services.ServiceConfiguration;
import com.dmitriid.tetrad.services.ServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackConnected;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackConnectedListener;
import org.eclipse.paho.client.mqttv3.*;
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
  private MqttClient   mqttSession;

  private String  mqttBroker;
  private String  mqttClientID;
  private String  mqttTopic;
  private String  mqttTopicSub;
  private Integer mqttQoS;

  public FirehoseService() {
  }


  public void init(ServiceConfiguration configuration) throws ServiceException {
    //while(!)
    JsonNode channels = configuration.getConfiguration().at("/slack/channels");

    for (final JsonNode channel : channels) {
      slackChannels.add(channel.asText());
    }
    //this.slackTopic = ((ArrayNode)configuration.getConfiguration().at("/slack/channels")).asL
    this.slackBotId = configuration.getConfiguration().at("/slack/botid").asText();
    this.identifier = configuration.getConfiguration().at("/slack/identifier").asText();

    this.mqttBroker = configuration.getConfiguration().at("/mqtt/publish/broker").asText();
    this.mqttClientID = configuration.getConfiguration().at("/mqtt/publish/clientid").asText();
    this.mqttQoS = configuration.getConfiguration().at("/mqtt/publish/qos").asInt();
    this.mqttTopic = configuration.getConfiguration().at("/mqtt/publish/topic").asText();
    this.mqttTopicSub = configuration.getConfiguration().at("/mqtt/subscribe/topic").asText();
  }

  public void start() throws ServiceException {
    slackSession = SlackSessionFactory.createWebSocketSlackSession(slackBotId);

    try {
      mqttSession = new MqttClient(mqttBroker, mqttClientID, new MemoryPersistence());
      slackSession.addMessagePostedListener(new PostHandler(this::firehose, identifier));
      slackSession.connect();
      Thread.currentThread().join();
    } catch (IOException | MqttException e) {
      e.printStackTrace();
      throw ServiceException.RunException(e.getMessage());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void firehose(FirehoseMessage msg) {
    ObjectMapper mapper = new ObjectMapper();

    try {
      byte[] content = mapper.writeValueAsBytes(msg);
      MqttMessage message = new MqttMessage(content);
      message.setQos(mqttQoS);
      if (!mqttSession.isConnected()) this.connectMQTT();
      if (mqttSession.isConnected()) mqttSession.publish(mqttTopic, message);
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

      mqttSession.setCallback(new MqttCallback() {
                                @Override
                                public void connectionLost(Throwable cause) {

                                }

                                @Override
                                public void messageArrived(String topic, MqttMessage message) throws Exception {
                                  String msg = new String(message.getPayload());

                                  ObjectMapper mapper = new ObjectMapper();
                                  FirehoseMessage firehoseMessage = mapper.readValue(msg, FirehoseMessage.class);

                                  if(!identifier.equals(firehoseMessage.service)) {
                                    return;
                                  }
                                  SlackChannel slackChannel = slackSession.findChannelByName(firehoseMessage.channel);
                                  slackSession.sendMessage(slackChannel, "*" + firehoseMessage.user + "*: " + firehoseMessage.content);
                                }

                                @Override
                                public void deliveryComplete(IMqttDeliveryToken token) {

                                }
                              }
                             );
      mqttSession.connect(connOpts);
      mqttSession.subscribe(mqttTopicSub);

    } catch (MqttException e) {
      e.printStackTrace();
    }
  }
}
