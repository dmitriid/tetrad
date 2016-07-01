package com.dmitriid.tetrad.adapters;

import com.dmitriid.tetrad.interfaces.IAdapter;
import com.dmitriid.tetrad.interfaces.ITetradCallback;
import com.dmitriid.tetrad.interfaces.ITransformer;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.dmitriid.tetrad.transformers.TransformSlackNiceties;
import com.fasterxml.jackson.databind.JsonNode;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.events.SlackMessageUpdated;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackMessageUpdatedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

public class TetradSlack implements SlackMessagePostedListener, IAdapter {
    private final SlackConfig        slackConfig;
    private       SlackSession       slackSession;
    private       ITetradCallback    callback;
    private final List<ITransformer> transformers;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    public TetradSlack(JsonNode configuration, List<ITransformer> transformers) {
        slackConfig = new SlackConfig(configuration);
        this.transformers = transformers;
    }

    public void run(ITetradCallback callback) {
        logger.debug("Run");
        logger.info(MessageFormat.format("Connecting with botid {0}",
                                         slackConfig.botid
                                        ));
        slackSession = SlackSessionFactory.createWebSocketSlackSession(slackConfig.botid);
        slackSession.addMessagePostedListener(this);
        slackSession.addMessageUpdatedListener(this::onUpdateEvent);
        this.callback = callback;
        try {
            slackSession.connect();
        } catch (IOException e) {
            logger.error(MessageFormat.format("Error connecting with botid {0}. Message: {1}",
                                              slackConfig.botid,
                                              e.getMessage()
                                             ));
        }
    }

    public String getIdentifier() {
        return slackConfig.identifier;
    }

    public void post(FirehoseMessage firehoseMessage) {
        logger.info("Publish message " + firehoseMessage.toLogString());
        if (!slackSession.isConnected()) return;
        SlackChannel slackChannel = slackSession.findChannelByName(firehoseMessage.channel);
        if (slackChannel == null) {
            return;
        }
        slackSession.sendMessage(slackChannel, "*" + firehoseMessage.user + "*: " + firehoseMessage.content);
    }

    @Override
    public void onEvent(SlackMessagePosted event, SlackSession session) {

        ArrayList<SlackAttachment> attachs = event.getAttachments();

        SlackUser sender = event.getSender();
        FirehoseMessage firehoseMessage = new FirehoseMessage(
          "slack",
          "post",
          sender.getUserName(),
          slackConfig.identifier,
          event.getChannel().getName(),
          event.getMessageContent()
        );

        logger.info("Got post event from service: " + firehoseMessage.toLogString());

        Boolean isBot = slackConfig.ignore.getOrDefault(sender.getUserName(), null);
        if (isBot != null && isBot == sender.isBot()) {
            return;
        }

        for(ITransformer transfomer : transformers) {
            firehoseMessage = transfomer.transform(firehoseMessage, this);
        }

        if(!firehoseMessage.content.isEmpty()) callback.execute(firehoseMessage);

        FirehoseMessage possibleFollowup = new TransformSlackNiceties().transform(firehoseMessage, this, event);
        Optional.ofNullable(possibleFollowup).ifPresent(msg -> {
            callback.execute(msg);
        });
    }

    public void onUpdateEvent(SlackMessageUpdated event, SlackSession slackSession){
        FirehoseMessage possibleFollowup = new TransformSlackNiceties().transform(new FirehoseMessage(), this, event);
        Optional.ofNullable(possibleFollowup).ifPresent(msg -> {
            callback.execute(msg);
        });
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

class Updated implements SlackMessageUpdatedListener {
    @Override
    public void onEvent(SlackMessageUpdated event, SlackSession session) {
    }
}
