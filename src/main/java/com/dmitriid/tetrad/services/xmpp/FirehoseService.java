package com.dmitriid.tetrad.services.xmpp;

import com.dmitriid.tetrad.interfaces.ManagedService;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.dmitriid.tetrad.services.ServiceConfiguration;
import com.dmitriid.tetrad.services.ServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.ArrayList;
import java.util.List;

public class FirehoseService implements ManagedService {
  private List<String> rooms = new ArrayList<>();

  private String service_domain;
  private String chat_service;
  private String username;
  private String password;
  private String resource;

  private String  mqttBroker;
  private String  mqttClientID;
  private String  mqttTopic;
  private Integer mqttQoS;

  private MqttClient mqttSession;
  private XMPPConnection xmppConnection;
  //private List<MultiUserChat> chatRooms;


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

    this.mqttBroker = configuration.getConfiguration().at("/mqtt/broker").asText();
    this.mqttClientID = configuration.getConfiguration().at("/mqtt/clientid").asText();
    this.mqttQoS = configuration.getConfiguration().at("/mqtt/qos").asInt();
    this.mqttTopic = configuration.getConfiguration().at("/mqtt/topic").asText();
  }

  public void start() throws ServiceException {
/*
    SSLContext sslcontext = null;
    try {
      sslcontext = SSLContexts.custom()
              .loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true)
              .build();
    } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
      e.printStackTrace();
    }
*/
/*
    XmppClient xmppClient = XmppClient.create(service_domain,
                                              TcpConnectionConfiguration.builder()
                                                      .secure(true)
                                                      .sslContext(sslcontext)
                                                      .build()
                                             );
*/

    try {

      xmppConnection = new XMPPConnection(service_domain);
      xmppConnection.connect();

      if (!xmppConnection.isConnected())
        throw ServiceException.StartException("Could not connect XMPP");

      xmppConnection.login(username, password, resource);

      if(!xmppConnection.isAuthenticated())
        throw ServiceException.StartException("Could not authenticate XMPP");

      for(String room : rooms){
        final String roomJID = room + "@" + chat_service;
        MultiUserChat chat = new MultiUserChat(xmppConnection, roomJID);
        chat.join(xmppConnection.getUser());
        chat.addMessageListener(packet -> this.handleMessage((Message)packet, roomJID));


                /*new PacketListener() {
          @Override
          public void processPacket(Packet packet) {
            System.out.println(packet.getFrom());
          }*/
//        });
                /*m -> {
          this.handleMessage(m, room);
        });*/
      }

      mqttSession = new MqttClient(mqttBroker, mqttClientID, new MemoryPersistence());

/*      xmppClient.connect();
      xmppClient.login(username, password, resource);
      MultiUserChatManager multiUserChatManager =
              xmppClient.getManager(MultiUserChatManager.class);
      ChatService chatService =
              multiUserChatManager.createChatService(Jid.of(chat_service));
      ChatRoom chatRoom = chatService.createRoom("dmitriid");

      chatRoom.addInboundMessageListener(this::handleMessage);

      xmppClient.send(new Presence(Presence.Show.CHAT));


      chatRoom.enter(username);
      chatRoom.sendMessage("wut");*/

      /*xmppClient.connect();
      xmppClient.login("fluor", "!hesitate101", "moi");

      MultiUserChatManager multiUserChatManager = xmppClient.getManager(MultiUserChatManager.class);
      ChatService chatService = multiUserChatManager.createChatService(Jid.of("conference.jabber.ru"));
      ChatRoom chatRoom = chatService.createRoom("dmitriid");

      chatRoom.addInboundMessageListener(e -> {
        Message message = e.getMessage();
        System.out.println("----- chartroom incoming listener -----");
        System.out.println(message.getBody());
        System.out.println(message.getFrom().toString());

      });

      RosterManager rosterManager = xmppClient.getManager(RosterManager.class);
      rosterManager.addRosterListener(e -> {
        // The roster event contains information about added, updated or deleted contacts.
        // TODO: Update your roster!
        Collection<Contact> contacts = rosterManager.getContacts();
        for (Contact contact : contacts) {
          System.out.println(contact.getName());
        }
      });

      xmppClient.addInboundMessageListener(e -> {
        Message message = e.getMessage();
        System.out.println("----- client incoming listener -----");
        System.out.println(message.getBody());
        System.out.println(message.getFrom().toString());
      });


      xmppClient.send(new Presence(Presence.Show.CHAT));

      chatRoom.enter("fluor");
      chatRoom.sendMessage("wut");
      System.out.println(chatRoom.hasEntered());*/



      Thread.currentThread().join();
    } catch (MqttException | InterruptedException | XMPPException e) {
      e.printStackTrace();
    }

  }

  public void shutdown() {
  }

  private void handleMessage(Message message, String room){

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
