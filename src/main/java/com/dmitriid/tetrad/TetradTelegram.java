package com.dmitriid.tetrad;

import com.dmitriid.tetrad.services.FirehoseMessage;
import com.dmitriid.tetrad.services.utils.JIDUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class TetradTelegram extends TelegramLongPollingBot {
    private String botid;
    private String username;
    private List<String> rooms = new ArrayList<>();
    private TetradCallback callback;

    private Long startTimestamp;

    TelegramBotsApi bot;

    TetradTelegram(JsonNode configuration){
        botid = configuration.at("/botid").asText();
        username = configuration.at("/username").asText();

        for(JsonNode room : configuration.at("/channels")){
            rooms.add(room.asText());
        }
    }

    public void start(TetradCallback callback){
        this.startTimestamp = System.currentTimeMillis() / 1000L;
        this.callback = callback;
        bot = new TelegramBotsApi();
        try {
            bot.registerBot(this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(!update.hasMessage()){
            return;
        }

        Message message = update.getMessage();

        if(message.getDate() < startTimestamp){
            return;
        }

        if(!message.hasText()){
            return;
        }

        FirehoseMessage firehoseMessage = new FirehoseMessage("telegram",
                                                              "post",
                                                              message.getFrom().getUserName(),
                                                              message.getChat().getId().toString(),
                                                              message.getChat().getTitle(),
                                                              message.getText());
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
        SendMessage sendMessageReq = new SendMessage();
        sendMessageReq.setChatId(firehoseMessage.service);

        sendMessageReq.enableMarkdown(true);
        sendMessageReq.setText("*" + firehoseMessage.user + "*: " + firehoseMessage.content);
        try {
            sendMessage(sendMessageReq);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
