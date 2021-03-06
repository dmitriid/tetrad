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
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import java.text.MessageFormat;

public class TetradTelegram extends TelegramLongPollingBot {
    private final String botid;
    private final String username;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    //private final List<String> rooms = new ArrayList<>();
    private ITetradCallback callback;
    private Long            startTimestamp;

    final TelegramBotsApi bot;

    public TetradTelegram(JsonNode configuration) {
        botid = configuration.at("/botid").asText();
        username = configuration.at("/username").asText();
        bot = new TelegramBotsApi();
    }

    public void start(ITetradCallback callback) {
        logger.debug("Run");
        logger.info(MessageFormat.format("Connecting with botid {0}",
                                         botid
                                        ));
        this.startTimestamp = System.currentTimeMillis() / 1000L;
        this.callback = callback;

        try {
            bot.registerBot(this);
        } catch (TelegramApiRequestException e) {
            logger.error(MessageFormat.format("Error connecting with botid {0}. Message: {1}",
                                              botid,
                                              e.getMessage()
                                             ));
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        logger.info("Received update: " + update.toString());
        logger.info("Has message: " + update.hasMessage());
        if (!update.hasMessage()) {
            return;
        }

        Message message = update.getMessage();

        String username = Optional.ofNullable(message.getFrom().getUserName())
            .orElse(
                MessageFormat.format(
                    "{0} {1}",
                    message.getFrom().getFirstName(),
                    message.getFrom().getLastName())
            );

        FirehoseMessage firehoseMessage = new FirehoseMessage("telegram",
                                                             "post",
                                                             username,
                                                             message.getChat().getId().toString(),
                                                             message.getChat().getTitle(),
                                                             message.getText());

        logger.info("Got event from service: " + firehoseMessage.toLogString());
        logger.info("Timestamp: " + message.getDate() + " vs local " + startTimestamp);

        if (message.getDate() < startTimestamp) {
            return;
        }

        if (!message.hasText()) {
            return;
        }

        callback.execute(firehoseMessage);
    }

    @Override
    public String getBotUsername() {
        logger.info("Username requested: " + username);
        return username;
    }

    @Override
    public String getBotToken() {
        logger.info("Token requested: " + botid);
        return botid;
    }

    public void post(FirehoseMessage firehoseMessage) {
        logger.info("Publish message " + firehoseMessage.toLogString());

        SendMessage sendMessageReq = new SendMessage();
        sendMessageReq.setChatId(firehoseMessage.service);

        sendMessageReq.enableMarkdown(true);
        sendMessageReq.setText("*" + firehoseMessage.user + "*: " + firehoseMessage.content);
        try {
            execute(sendMessageReq);
        } catch (TelegramApiException e) {
            logger.error(MessageFormat.format("Error publishing message. Message: {0} Original message: {1}",
                                              e.getMessage(),
                                              firehoseMessage.toLogString()
                                             ));
        }
    }
}
