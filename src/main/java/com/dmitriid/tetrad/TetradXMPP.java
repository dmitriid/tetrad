package com.dmitriid.tetrad;

import com.dmitriid.tetrad.services.FirehoseMessage;
import com.dmitriid.tetrad.services.utils.JIDUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TetradXMPP {
    private String service_domain;
    private String chat_service;
    private String username;
    private String password;
    private String resource;
    private Boolean resources_per_user = false;
    private Integer max_resources;

    private List<String> ignore = new ArrayList<>();

    private List<String> rooms  = new ArrayList<>();
    private Map<String, MultiUserChat> connectedRooms = new HashMap<>();

    private Map<String, String> perUserResources = new HashMap<>();

    private XMPPConnection xmppConnection;

    private TetradCallback callback;

    TetradXMPP(JsonNode config){
        service_domain = config.at("/service_domain").asText();
        setChatService(config.at("/chat_service").asText());
        username = config.at("/username").asText();
        password = config.at("/password").asText();
        resource = config.at("/resource").asText();
        resources_per_user = config.at("/service_domain").asBoolean(false);
        max_resources = config.at("/service_domain").asInt(5);

        for (JsonNode user : config.at("/ignore")) {
            ignore.add(user.asText());
        }

        for (JsonNode room : config.at("/rooms")) {
            rooms.add(room.asText());
        }
    }

    public void start(TetradCallback callback){
        this.callback = callback;
        try {
            xmppConnection = new XMPPConnection(service_domain);
            xmppConnection.connect();

            xmppConnection.login(username, password, resource);

            DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(0);

            for (String room : rooms) {
                final String roomJID = room + "@" + getChatService();
                MultiUserChat chat = new MultiUserChat(xmppConnection, roomJID);
                chat.join(xmppConnection.getUser(), password, history, SmackConfiguration.getPacketReplyTimeout());
                chat.addMessageListener(packet -> this.handleMessage((Message) packet, roomJID));
                connectedRooms.put(room, chat);
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(Message message, String room) {
        String jid = message.getFrom().replace(room + "/", "");
        if(ignore.contains(JIDUtils.bareJID(jid))){
            return;
        }

        FirehoseMessage firehoseMessage = new FirehoseMessage("xmpp",
                                                              message.getType().name(),
                                                              message.getFrom(),
                                                              getChatService(),
                                                              room,
                                                              message.getBody());
        callback.execute(firehoseMessage);
    }

    String getChatService() {
        return chat_service;
    }

    private void setChatService(String chat_service) {
        this.chat_service = chat_service;
    }

    void post(FirehoseMessage firehoseMessage){
        if (!chat_service.equals(firehoseMessage.service)) {
            return;
        }

        if (!connectedRooms.containsKey(firehoseMessage.channel)) {
            return;
        }

        MultiUserChat chat = connectedRooms.get(firehoseMessage.channel);
        if(!chat.isJoined()) return;

        try {
            chat.sendMessage(firehoseMessage.user + ": " + firehoseMessage.content);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }
}
