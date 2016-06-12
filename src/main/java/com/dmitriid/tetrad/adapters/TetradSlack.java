package com.dmitriid.tetrad.adapters;

import com.dmitriid.tetrad.interfaces.ITetradCallback;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TetradSlack implements SlackMessagePostedListener {
    private final SlackConfig     slackConfig;
    private       SlackSession    slackSession;
    private       ITetradCallback callback;

    public TetradSlack(JsonNode configuration) {
        slackConfig = new SlackConfig(configuration);
    }

    public void run(ITetradCallback callback) {
        slackSession = SlackSessionFactory.createWebSocketSlackSession(slackConfig.botid);
        slackSession.addMessagePostedListener(this);
        this.callback = callback;
        try {
            slackSession.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getIdentifier() {
        return slackConfig.identifier;
    }

    public void post(FirehoseMessage firehoseMessage) {
        if (!slackSession.isConnected()) return;
        SlackChannel slackChannel = slackSession.findChannelByName(firehoseMessage.channel);
        if (slackChannel == null) {
            return;
        }
        slackSession.sendMessage(slackChannel, "*" + firehoseMessage.user + "*: " + firehoseMessage.content);
    }

    @Override
    public void onEvent(SlackMessagePosted event, SlackSession session) {
        SlackUser sender = event.getSender();

        Boolean isBot = slackConfig.ignore.getOrDefault(sender.getUserName(), null);
        if (isBot != null && isBot == sender.isBot()) {
            return;
        }

        callback.execute(new FirehoseMessage(
                "slack",
                "post",
                sender.getUserName(),
                slackConfig.identifier,
                event.getChannel().getName(),
                event.getMessageContent()
        ));
    }

    static class SlackConfig {
        final String botid;
        final String identifier;
        final Map<String, Boolean> ignore   = new HashMap<>();
        final List<String>         channels = new ArrayList<>();

        SlackConfig(JsonNode config){
            botid = config.at("/botid").asText();
            identifier = config.at("/identifier").asText();

            for(JsonNode ignr : config.at("/ignore")){
                ignore.put(ignr.at("/username").asText(), ignr.at("/bot").asBoolean(false));
            }

            for (JsonNode chnl : config.at("/channels")) {
                channels.add(chnl.asText());
            }
        }
    }
}
