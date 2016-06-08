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
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.exit;

public class FirehoseService implements ManagedService, MqttCallback {
  private List<String> slackChannels = new ArrayList<>();
  private String slackBotId;
  private String identifier;

  private SlackSession    slackSession;
  private MqttAsyncClient mqttSession;

  private String  mqttBroker;
  private String  mqttClientID;
  private String  mqttTopic;
  private String  mqttTopicSub;
  private Integer mqttQoS;

  private Map<String, Boolean> ignores = new HashMap<>();

  public FirehoseService() {
  }


  public void init(ServiceConfiguration configuration) throws ServiceException {
    JsonNode channels = configuration.getConfiguration().at("/slack/channels");

    for (final JsonNode channel : channels) {
      slackChannels.add(channel.asText());
    }

    JsonNode ignores = configuration.getConfiguration().at("/slack/ignore");

    for (final JsonNode ignore : ignores) {
      String user = ignore.at("/username").asText();
      Boolean bot = ignore.at("/bot").asBoolean(false);
      this.ignores.put(user, bot);
    }


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
      this.connectMQTT();
      slackSession.addMessagePostedListener(new PostHandler(this::firehose, identifier, this.ignores));
      slackSession.connect();
      Thread.currentThread().join();
    } catch (IOException e) {
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
    try {
      mqttSession = new MqttAsyncClient(mqttBroker, mqttClientID, new MemoryPersistence());
      MqttConnectOptions connOpts = new MqttConnectOptions();
      connOpts.setCleanSession(true);
      System.out.println("Connecting to mqttBroker: " + mqttBroker);

      FirehoseService t = this;
      mqttSession.connect(connOpts, new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
          mqttSession.setCallback(t);
          try {
            mqttSession.subscribe(mqttTopicSub, mqttQoS);
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

  }

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    String msg = new String(message.getPayload());

    ObjectMapper mapper = new ObjectMapper();
    FirehoseMessage firehoseMessage = mapper.readValue(msg, FirehoseMessage.class);

    if (!identifier.equals(firehoseMessage.service)) {
      return;
    }
    SlackChannel slackChannel = slackSession.findChannelByName(firehoseMessage.channel);
    slackSession.sendMessage(slackChannel, "*" + firehoseMessage.user + "*: " + firehoseMessage.content);
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {

  }
}
