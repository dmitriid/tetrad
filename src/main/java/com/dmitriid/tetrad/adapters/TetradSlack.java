package com.dmitriid.tetrad.adapters;

import com.dmitriid.tetrad.interfaces.IGenericService;
import com.dmitriid.tetrad.interfaces.ITetradCallback;
import com.dmitriid.tetrad.interfaces.ITransformer;
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
import java.util.*;

public class TetradSlack implements SlackMessagePostedListener, IGenericService {
    private final SlackConfig        slackConfig;
    private       SlackSession       slackSession;
    private       ITetradCallback    callback;
    private final List<ITransformer> transformers;

    public TetradSlack(JsonNode configuration, List<ITransformer> transformers) {
        slackConfig = new SlackConfig(configuration);
        this.transformers = transformers;
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

        FirehoseMessage firehoseMessage = new FirehoseMessage(
                "slack",
                "post",
                sender.getUserName(),
                slackConfig.identifier,
                event.getChannel().getName(),
                event.getMessageContent()
        );

        for(ITransformer transfomer : transformers) {
            firehoseMessage = transfomer.transform(firehoseMessage, this);
        }

        callback.execute(firehoseMessage);
    }

    @Override
    public String getChannelById(String id) {
        return slackSession
                .getChannels()
                .stream()
                .filter(slackChannel -> slackChannel.getId().equals(id))
                .findFirst()
                .map(SlackChannel::getName)
                .orElse(id);
    }

    @Override
    public String getUserById(String id) {
        return slackSession
                .getUsers()
                .stream()
                .filter(slackChannel -> slackChannel.getId().equals(id))
                .findFirst()
                .map(SlackUser::getUserName)
                .orElse(id);
    }

    private static class SlackConfig {
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
