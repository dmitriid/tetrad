/*******************************************************************************
 * Copyright (c) 2016 Dmitrii "Mamut" Dimandt <dmitrii@dmitriid.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

package com.dmitriid.tetrad.adapters;

import com.dmitriid.tetrad.interfaces.ITetradCallback;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.dmitriid.tetrad.utils.JIDUtils;
import com.dmitriid.tetrad.utils.MiscUtils;
import com.dmitriid.tetrad.utils.XMPPUtils;
import com.fasterxml.jackson.databind.JsonNode;

import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.XHTMLManager;
import org.jivesoftware.smackx.XHTMLText;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TetradXMPP {
    private final String service_domain;
    private final String username;
    private final String password;
    private final String resource;
    private final Integer max_resources;
    private final List<String> ignore = new ArrayList<>();
    private final List<String> rooms = new ArrayList<>();
    private final Map<String, MultiUserChat> connectedRooms = new HashMap<>();
    private final HashMap<String/*user*/, MultiUserChat>
            perUserRooms = new HashMap<>();
    private final HashMap<String/*user*/, XMPPConnection>
            perUserConnections = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    private String chat_service;
    private Boolean resource_per_user = false;
    private ITetradCallback callback;

    public TetradXMPP(JsonNode config) {
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

    public void start(ITetradCallback callback) {
        logger.debug("start");
        logger.info(MessageFormat.format("Connecting to service {0} with username {1} and resource {2}",
                service_domain,
                username,
                resource
        ));
        this.callback = callback;
        try {
            XMPPConnection xmppConnection = new XMPPConnection(service_domain);
            xmppConnection.connect();

            xmppConnection.login(username, password, resource);

            DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(0);

            for (String room : rooms) {
                final String roomJID = room + "@" + getChatService();
                logger.info(MessageFormat.format("Connecting to room {0}", roomJID));
                MultiUserChat chat = new MultiUserChat(xmppConnection, roomJID);
                chat.join(xmppConnection.getUser(), password, history, SmackConfiguration.getPacketReplyTimeout());
                chat.addMessageListener(packet -> this.handleMessage((Message) packet, roomJID));
                chat.changeNickname("β");
                connectedRooms.put(room, chat);
            }
        } catch (XMPPException e) {
            logger.error(MessageFormat.format("Error connecting to service {0} with username {1} and resource {2}. Message: {3}",
                    service_domain,
                    username,
                    resource,
                    e.getMessage()
            ));
        }
    }

    private void handleMessage(Message message, String room) {
        String jid = message.getFrom().replace(room + "/", "");
        FirehoseMessage firehoseMessage = new FirehoseMessage("xmpp",
                message.getType().name(),
                JIDUtils.bareJID(jid) + "@" + getChatService(),
                getChatService(),
                room,
                message.getBody());

        logger.info("Got message from service: " + firehoseMessage.toLogString());

        long count = ignore.stream()
                .filter(s -> jid.equals(s) || jid.matches(s))
                .count();

        if (count > 0) {
            logger.info("Ignoring message because nickname matches ignore list");
            return;
        }

        callback.execute(firehoseMessage);
    }

    public String getChatService() {
        return chat_service;
    }

    private void setChatService(String chat_service) {
        this.chat_service = chat_service;
    }

    public void post(FirehoseMessage firehoseMessage) {
        logger.info("Publish message " + firehoseMessage.toLogString());

        if (!chat_service.equals(firehoseMessage.service)) {
            logger.info(
                    MessageFormat.format(
                            "Ignore because message service {0} does not match service {1}",
                            firehoseMessage.service,
                            chat_service
                    )
            );
            return;
        }

        if (!connectedRooms.containsKey(firehoseMessage.channel)) {
            logger.info(
                    MessageFormat.format(
                            "Ignore because message channel {0} does not match connected channels",
                            firehoseMessage.channel
                    )
            );
            return;
        }
        MultiUserChat chat = connectedRooms.get(firehoseMessage.channel);
        if (!chat.isJoined()) {
            logger.warn(
                    MessageFormat.format(
                            "Chat {0}@{1} is not joined",
                            firehoseMessage.service,
                            firehoseMessage.channel
                    )
            );
            try {
                chat.join(chat.getNickname());
            } catch (XMPPException e) {
                logger.error(
                        MessageFormat.format("Error connecting to chatroom {0} at service {1} with username {2}. Message: {3}",
                                chat.getRoom(),
                                service_domain,
                                username,
                                e.getMessage()
                        ));
                return;
            }
        }

        if (resource_per_user) {
            postAsUser(chat, firehoseMessage);
        } else {
            postDefaultMessage(chat, firehoseMessage);
        }
    }

    private void postDefaultMessage(MultiUserChat chat, FirehoseMessage firehoseMessage) {
        Message msg = chat.createMessage();

        // handle XMPP commands coming from other services
        // keep it as an attachment if it's a facepalm /o\
        if (firehoseMessage.content.startsWith("/") && !firehoseMessage.content.startsWith("/o\\")) {
            msg.setBody(firehoseMessage.content);
        } else {
            msg.setBody(firehoseMessage.user + ": " + firehoseMessage.content);

            XHTMLText xhtml = new XHTMLText(null, null);
            xhtml.appendOpenSpanTag("color: #" + MiscUtils.intToRGB(firehoseMessage.user.hashCode()));
            xhtml.append(firehoseMessage.user);
            xhtml.appendCloseSpanTag();
//        xhtml.appendOpenStrongTag();
//        xhtml.append(firehoseMessage.user);
//        xhtml.appendCloseStrongTag();
            xhtml.append(": ");

            XMPPUtils.toXMPPXHTML(firehoseMessage.content, xhtml);

            XHTMLManager.addBody(msg, xhtml.toString());
        }
        try {
            chat.sendMessage(msg);
        } catch (XMPPException e) {
            logger.error(MessageFormat.format("Error publishing XHTML-enabled message to chatroom {0}. Message: {1}. Original message: {2}",
                    chat.getRoom(),
                    e.getMessage(),
                    firehoseMessage.toLogString()
            ));
            try {
                logger.info(
                        MessageFormat.format(
                                "Attempting to send plain XMPP message to chatroom {0}: {1}",
                                chat.getRoom(),
                                firehoseMessage.toLogString()
                        )
                );
                chat.sendMessage(firehoseMessage.user + ": " + firehoseMessage.content);
            } catch (XMPPException e1) {
                logger.error(
                        MessageFormat.format(
                                "Error publishing plain message to chatroom {0}. Message: {1}. Original message: {2}",
                                chat.getRoom(),
                                e1.getMessage(),
                                firehoseMessage.toLogString()
                        )
                );
            }
        }
    }

    private void postAsUser(MultiUserChat defaultChat, FirehoseMessage firehoseMessage) {
        logger.info("Attempting to post as user");
        try {
            XMPPConnection perUserConn = new XMPPConnection(service_domain);
            if (!perUserConnections.containsKey(firehoseMessage.user)) {
                if (perUserConnections.size() == max_resources) {
                    logger.info("Max per-user resources reached");
                    postDefaultMessage(defaultChat, firehoseMessage);
                    return;
                } else {
                    logger.info(
                            MessageFormat.format(
                                    "Connecting to {0} as {1}/{2}",
                                    service_domain,
                                    username,
                                    resource
                            )
                    );
                    perUserConn.connect();
                    perUserConn.login(username, password, JIDUtils.bareUsername(firehoseMessage.user));
                    perUserConnections.put(firehoseMessage.user, perUserConn);
                }
            }
            MultiUserChat perUserChatRoom;
            String localUser = MessageFormat.format(
                    "{0}@{1}/{2}",
                    firehoseMessage.channel,
                    firehoseMessage.service,
                    firehoseMessage.user
            );
            if (perUserRooms.containsKey(localUser)) {
                perUserChatRoom = perUserRooms.get(localUser);
            } else {
                DiscussionHistory history = new DiscussionHistory();
                history.setMaxStanzas(0);

                final String roomJID = firehoseMessage.channel + "@" + getChatService();

                logger.info(
                        MessageFormat.format(
                                "Joining room {0} as {1}",
                                roomJID,
                                perUserConn.getUser()
                        )
                );

                perUserChatRoom = new MultiUserChat(perUserConn, roomJID);
                perUserChatRoom.join(perUserConn.getUser());//, password);
                //perUserChatRoom.changeNickname(firehoseMessage.user + '@' + firehoseMessage.type);
                perUserRooms.put(localUser, perUserChatRoom);
            }

            perUserChatRoom.sendMessage(firehoseMessage.content);
        } catch (XMPPException e) {
            logger.error(
                    MessageFormat.format(
                            "Error publishing as user to chatroom. Message: {0}. Original message: {1}",
                            e.getMessage(),
                            firehoseMessage.toLogString()
                    )
            );
        }
    }
}
