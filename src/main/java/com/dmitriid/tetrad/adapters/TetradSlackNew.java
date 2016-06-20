package com.dmitriid.tetrad.adapters;

import allbegray.slack.SlackClientFactory;
import allbegray.slack.rtm.Event;
import allbegray.slack.rtm.EventListener;
import allbegray.slack.rtm.SlackRealTimeMessagingClient;
import com.dmitriid.tetrad.interfaces.IAdapter;
import com.dmitriid.tetrad.interfaces.ITetradCallback;
import com.dmitriid.tetrad.interfaces.ITransformer;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TetradSlackNew implements SlackMessagePostedListener, IAdapter, EventListener {
    private final SlackConfig                  slackConfig;
    private       SlackRealTimeMessagingClient slackSession;
    private       ITetradCallback              callback;
    private final List<ITransformer>           transformers;

    public TetradSlackNew(JsonNode configuration, List<ITransformer> transformers) {
        slackConfig = new SlackConfig(configuration);
        this.transformers = transformers;
    }

    public void run(ITetradCallback callback) {

        //Slack webApiClient = SlackClientFactory.createWebApiClient(slackConfig.botid);

        slackSession = SlackClientFactory.createSlackRealTimeMessagingClient(slackConfig.botid);
        slackSession.addListener(Event.MESSAGE, this);
        slackSession.connect();
        //slackSession.

        /*slackSession = SlackSessionFactory.createWebSocketSlackSession(slackConfig.botid);
        slackSession.addMessagePostedListener(this);
        slackSession.addMessageUpdatedListener(new Updated());
        this.callback = callback;
        try {
            slackSession.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public String getIdentifier() {
        return slackConfig.identifier;
    }

    public void post(FirehoseMessage firehoseMessage) {
//        if (!slackSession.isConnected()) return;
//        SlackChannel slackChannel = slackSession.findChannelByName(firehoseMessage.channel);
//        if (slackChannel == null) {
//            return;
//        }
//        slackSession.sendMessage(slackChannel, "*" + firehoseMessage.user + "*: " + firehoseMessage.content);
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
//        return slackSession
//                .getChannels()
//                .stream()
//                .filter(slackChannel -> slackChannel.getId().equals(id))
//                .findFirst()
//                .map(SlackChannel::getName)
//                .orElse(id);
        return id;
    }

    @Override
    public String getUserById(String id) {
//        return slackSession
//                .getUsers()
//                .stream()
//                .filter(slackChannel -> slackChannel.getId().equals(id))
//                .findFirst()
//                .map(SlackUser::getUserName)
//                .orElse(id);
        return id;
    }

    @Override
    public void handleMessage(JsonNode jsonNode) {
        System.out.println(jsonNode.toString());
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

//class Updated implements SlackMessageUpdatedListener {
//    @Override
//    public void onEvent(SlackMessageUpdated event, SlackSession session) {
//        System.out.println(event.getEventType());
//        System.out.println(event.getNewMessage());
//    }
//}
