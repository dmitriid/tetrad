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
import com.fasterxml.jackson.databind.JsonNode;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class TetradIRC extends ListenerAdapter {

    private final String serverName;
    private final String channel;
    private final String userName;

    private ITetradCallback callback;
    private PircBotX        bot;

    private final List<String> ignore = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    public TetradIRC(JsonNode config) {
        this.serverName = config.at("/service").asText();
        this.channel = config.at("/channel").asText();
        this.userName = config.at("/username").asText();

        for (JsonNode user : config.at("/ignore")) {
            ignore.add(user.asText());
        }
    }

    public void start(ITetradCallback callback) {
        logger.debug("start");
        logger.info(MessageFormat.format("Connecting to service {0} with username {1} and resource {2}",
                                         this.serverName,
                                         this.userName,
                                         this.getChannel()
                                        ));
        this.callback = callback;
        try {
            //IdentServer.startServer();

            Configuration configuration =
                    new Configuration.Builder()
                            .setName("B")
                            .setLogin("tetrad")
                            .setAutoNickChange(true)
                            .addServer(this.serverName)
                            .addAutoJoinChannel(this.getChannel())
                            //.setIdentServerEnabled(true)
                            .addListener(this)
                            .buildConfiguration();

            this.bot = new PircBotX(configuration);
            this.bot.startBot();
        } catch (IOException | IrcException e) {
            logger.error(MessageFormat.format("Error connecting to {0}{1}. Message: {2}",
                                              this.serverName,
                                              this.getChannel(),
                                              e.getMessage()
                                             ));
        }
    }

    public void onMessage(final MessageEvent event) {
        try {

            final String userNick = this.getUserName(event.getUser());

            long count = this.ignore.stream()
                    .filter(s -> s.equals(userNick))
                    .count();
            if (count > 0) {
                logger.info("Ignoring message because nickname matches ignore list");
                return;
            }

            FirehoseMessage firehoseMessage = new FirehoseMessage("irc",
                                                                  "message",
                                                                  this.getUserName(event.getUser()),
                                                                  this.serverName,
                                                                  event.getChannel().getName().replace("#", ""),
                                                                  event.getMessage());
            logger.info("Got event from service: " + firehoseMessage.toLogString());
            callback.execute(firehoseMessage);
        } catch (NullPointerException e) {
            logger.error(MessageFormat.format("Error {0}",
                                              e.getMessage()
                                             ));
        }
    }

    public void post(FirehoseMessage firehoseMessage) {
        logger.info("Publish message " + firehoseMessage.toLogString());

        this.bot
                .sendIRC()
                .message(
                        this.getChannel(),
                        "*" + firehoseMessage.user + "*: " + firehoseMessage.content
                        );
    }

    private String getChannel() {
        return MessageFormat.format("#{0}", this.channel);
    }

    private String getUserName(User user) {
        try {
            return user.getNick();
        } catch (NullPointerException ignored) {

        }
        try {
            return user.getRealName();
        } catch (NullPointerException ignored) {

        }

        try {
            return user.getLogin();
        } catch (NullPointerException ignored) {

        }

        return "anon";
    }
}
