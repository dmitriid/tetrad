package com.dmitriid.tetrad.services.xmpp;

import com.dmitriid.tetrad.interfaces.ManagedService;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.dmitriid.tetrad.services.ServiceConfiguration;
import com.dmitriid.tetrad.services.ServiceException;
import com.dmitriid.tetrad.services.utils.JIDUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirehoseService implements ManagedService, MqttCallback {
  private List<String>               rooms          = new ArrayList<>();
  private Map<String, MultiUserChat> connectedRooms = new HashMap<>();
  private List<String>               ignores        = new ArrayList<>();

  private String service_domain;
  private String chat_service;
  private String username;
  private String password;
  private String resource;

  private String mqttBroker;
  private String mqttClientID;
  private String mqttTopic;
  private String mqttTopicSub;

  private Integer mqttQoS;

  private MqttAsyncClient mqttSession;
  private XMPPConnection  xmppConnection;


  public FirehoseService(){

  }

  public void init(ServiceConfiguration configuration) throws ServiceException {
    service_domain = configuration.getConfiguration().at("/xmpp/service_domain").asText();
    username = configuration.getConfiguration().at("/xmpp/username").asText();
    password = configuration.getConfiguration().at("/xmpp/password").asText();
    resource = configuration.getConfiguration().at("/xmpp/resource").asText();
    chat_service = configuration.getConfiguration().at("/xmpp/chat_service").asText();

    JsonNode channels = configuration.getConfiguration().at("/xmpp/rooms");
    for (final JsonNode channel : channels) {
      rooms.add(channel.asText());
    }

    JsonNode ignores = configuration.getConfiguration().at("/xmpp/ignore");

    for (final JsonNode ignore : ignores) {
      this.ignores.add(ignore.asText());
    }

    this.mqttBroker = configuration.getConfiguration().at("/mqtt/publish/broker").asText();
    this.mqttClientID = configuration.getConfiguration().at("/mqtt/publish/clientid").asText();
    this.mqttQoS = configuration.getConfiguration().at("/mqtt/publish/qos").asInt();
    this.mqttTopic = configuration.getConfiguration().at("/mqtt/publish/topic").asText();
    this.mqttTopicSub = configuration.getConfiguration().at("/mqtt/subscribe/topic").asText();

  }

  public void start() throws ServiceException {
    try {
      xmppConnection = new XMPPConnection(service_domain);
      xmppConnection.connect();

      if (!xmppConnection.isConnected())
        throw ServiceException.StartException("Could not connect XMPP");

      xmppConnection.login(username, password, resource);

      if(!xmppConnection.isAuthenticated())
        throw ServiceException.StartException("Could not authenticate XMPP");

      DiscussionHistory history = new DiscussionHistory();
      history.setMaxStanzas(0);

      for(String room : rooms){
        final String roomJID = room + "@" + chat_service;
        MultiUserChat chat = new MultiUserChat(xmppConnection, roomJID);
        chat.join(xmppConnection.getUser(), password, history, SmackConfiguration.getPacketReplyTimeout());
        chat.addMessageListener(packet -> this.handleMessage((Message)packet, roomJID));
        connectedRooms.put(room, chat);
      }
      this.connectMQTT();
      Thread.currentThread().join();
    } catch (InterruptedException | XMPPException e) {
      e.printStackTrace();
    }

  }

  public void shutdown() {
  }

  private void handleMessage(Message message, String room){
    if(ignores.contains(JIDUtils.bareJID(message.getFrom().replace(room + "/", "")))){
      return;
    }

    FirehoseMessage firehoseMessage = new FirehoseMessage("xmpp",
                                                          message.getType().name(),
                                                          message.getFrom(),
                                                          chat_service,
                                                          room,
                                                          message.getBody());
    try {
      ObjectMapper mapper = new ObjectMapper();
      byte[] content = mapper.writeValueAsBytes(firehoseMessage);
      MqttMessage mqttMessage = new MqttMessage(content);
      mqttMessage.setQos(mqttQoS);

      if (!mqttSession.isConnected()) this.connectMQTT();
      if (mqttSession.isConnected())
        mqttSession.publish(mqttTopic, mqttMessage);
    } catch (JsonProcessingException | MqttException e) {
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
          exception.printStackTrace();
        }
      });
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void connectionLost(Throwable cause) {
    cause.printStackTrace();
  }

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    String msg = new String(message.getPayload());

    ObjectMapper mapper = new ObjectMapper();
    FirehoseMessage firehoseMessage = mapper.readValue(msg, FirehoseMessage.class);

    if(!chat_service.equals(firehoseMessage.service)){
      return;
    }

    if(!connectedRooms.containsKey(firehoseMessage.channel)){
      return;
    }

    MultiUserChat chat = connectedRooms.get(firehoseMessage.channel);
    chat.sendMessage(firehoseMessage.user + ": " + firehoseMessage.content);
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    System.out.println("a");
  }
}
