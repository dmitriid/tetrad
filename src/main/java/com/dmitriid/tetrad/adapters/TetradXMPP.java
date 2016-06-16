package com.dmitriid.tetrad.adapters;

import com.dmitriid.tetrad.interfaces.ITetradCallback;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.dmitriid.tetrad.utils.JIDUtils;
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
    private final String service_domain;
    private       String chat_service;
    private final String username;
    private final String password;
    private final String resource;
    private Boolean resource_per_user = false;
    private final Integer max_resources;

    private final List<String> ignore = new ArrayList<>();

    private final List<String>               rooms          = new ArrayList<>();
    private final Map<String, MultiUserChat> connectedRooms = new HashMap<>();

    private final HashMap<String/*user*/, MultiUserChat>
            perUserRooms = new HashMap<>();
    private final HashMap<String/*user*/, XMPPConnection>
            perUserConnections = new HashMap<>();

    private XMPPConnection xmppConnection;

    private ITetradCallback callback;

    public TetradXMPP(JsonNode config){
        service_domain = config.at("/service_domain").asText();
        setChatService(config.at("/chat_service").asText());
        username = config.at("/username").asText();
        password = config.at("/password").asText();
        resource = config.at("/resource").asText();
        resource_per_user = config.at("/resource_per_user").asBoolean(false);
        max_resources = config.at("/max_resources").asInt(5);

        for (JsonNode user : config.at("/ignore")) {
            ignore.add(user.asText());
        }

        for (JsonNode room : config.at("/rooms")) {
            rooms.add(room.asText());
        }
    }

    public void start(ITetradCallback callback){
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

        System.out.println(message.getFrom());

        FirehoseMessage firehoseMessage = new FirehoseMessage("xmpp",
                                                              message.getType().name(),
                                                              JIDUtils.bareJID(jid) + "@" + getChatService(),
                                                              getChatService(),
                                                              room,
                                                              message.getBody());
        callback.execute(firehoseMessage);
    }

    public String getChatService() {
        return chat_service;
    }

    private void setChatService(String chat_service) {
        this.chat_service = chat_service;
    }

    public void post(FirehoseMessage firehoseMessage){
        if (!chat_service.equals(firehoseMessage.service)) {
            return;
        }

        if (!connectedRooms.containsKey(firehoseMessage.channel)) {
            return;
        }
        MultiUserChat chat = connectedRooms.get(firehoseMessage.channel);
        if (!chat.isJoined()) return;

        try {
            if(resource_per_user){
                postAsUser(chat, firehoseMessage);
            } else {
                chat.sendMessage(firehoseMessage.user + ": " + firehoseMessage.content);
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    private void postAsUser(MultiUserChat defaultChat, FirehoseMessage firehoseMessage){
        try{
            XMPPConnection perUserConn = new XMPPConnection(service_domain);
            if(!perUserConnections.containsKey(firehoseMessage.user)) {
                if (perUserConnections.size() == max_resources) {
                    defaultChat.sendMessage(firehoseMessage.user + ": " + firehoseMessage.content);
                    return;
                } else {
                    perUserConn.connect();
                    perUserConn.login(username, password, JIDUtils.bareUsername(firehoseMessage.user));
                    perUserConnections.put(firehoseMessage.user, perUserConn);
                }
            }
            MultiUserChat perUserChatRoom;
            String localUser = firehoseMessage.channel + "@" + firehoseMessage.service + "/" + firehoseMessage.user;
            if(perUserRooms.containsKey(localUser)){
                perUserChatRoom = perUserRooms.get(localUser);
            } else {
                DiscussionHistory history = new DiscussionHistory();
                history.setMaxStanzas(0);

                final String roomJID = firehoseMessage.channel + "@" + getChatService();
                perUserChatRoom = new MultiUserChat(perUserConn, roomJID);
                perUserChatRoom.join(perUserConn.getUser(), password);
                perUserRooms.put(localUser, perUserChatRoom);
            }

            perUserChatRoom.sendMessage(firehoseMessage.content);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }
}
