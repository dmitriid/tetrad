package com.dmitriid.tetrad.adapters;

import com.dmitriid.tetrad.interfaces.ITetradCallback;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.text.MessageFormat;

public class TetradTelegram extends TelegramLongPollingBot {
    private final String botid;
    private final String username;
    //private final List<String> rooms = new ArrayList<>();
    private ITetradCallback callback;

    private Long startTimestamp;

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    public TetradTelegram(JsonNode configuration){
        botid = configuration.at("/botid").asText();
        username = configuration.at("/username").asText();

/*
        for(JsonNode room : configuration.at("/channels")){
            rooms.add(room.asText());
        }
*/
    }

    public void start(ITetradCallback callback){
        logger.debug("Run");
        logger.info(MessageFormat.format("Connecting with botid {0}",
                                         botid
                                        ));
        this.startTimestamp = System.currentTimeMillis() / 1000L;
        this.callback = callback;
        TelegramBotsApi bot = new TelegramBotsApi();
        try {
            bot.registerBot(this);
        } catch (TelegramApiException e) {
            logger.error(MessageFormat.format("Error connecting with botid {0}. Message: {1}",
                                              botid,
                                              e.getMessage()
                                             ));
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(!update.hasMessage()){
            return;
        }

        Message message = update.getMessage();
        FirehoseMessage firehoseMessage = new FirehoseMessage("telegram",
                                                              "post",
                                                              message.getFrom().getUserName(),
                                                              message.getChat().getId().toString(),
                                                              message.getChat().getTitle(),
                                                              message.getText());

        logger.info("Got event from service: " + firehoseMessage.toLogString());

        if(message.getDate() < startTimestamp){
            return;
        }

        if(!message.hasText()){
            return;
        }

        callback.execute(firehoseMessage);
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return botid;
    }

    public void post(FirehoseMessage firehoseMessage){
        logger.info("Publish message " + firehoseMessage.toLogString());

        SendMessage sendMessageReq = new SendMessage();
        sendMessageReq.setChatId(firehoseMessage.service);

        sendMessageReq.enableMarkdown(true);
        sendMessageReq.setText("*" + firehoseMessage.user + "*: " + firehoseMessage.content);
        try {
            sendMessage(sendMessageReq);
        } catch (TelegramApiException e) {
            logger.error(MessageFormat.format("Error publishing message. Message: {0} Original message: {1}",
                                              e.getMessage(),
                                              firehoseMessage.toLogString()
                                             ));
        }
    }
}
