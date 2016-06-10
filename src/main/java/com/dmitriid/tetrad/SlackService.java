package com.dmitriid.tetrad;

import com.dmitriid.tetrad.interfaces.ManagedService;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.dmitriid.tetrad.services.ServiceConfiguration;
import com.dmitriid.tetrad.services.ServiceException;
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

public class SlackService implements ManagedService {

    Map<String, Slack> slacks = new HashMap<>();
    TetradMQTT mqtt;

    @Override
    public void init(ServiceConfiguration configuration) throws ServiceException {
        for(JsonNode node : configuration.getConfiguration().at("/slack")){
            Slack slack = new Slack(node);
            slacks.put(slack.getIdentifier(), slack);
        }

        mqtt = new TetradMQTT(configuration.getConfiguration().at("/mqtt"));
    }

    @Override
    public void start() throws ServiceException {
        slacks.forEach((s, slack) -> slack.run(this.mqtt::sendMessage));
        mqtt.start(this::postToSlack);
    }

    @Override
    public void shutdown() throws ServiceException {

    }

    private void postToSlack(FirehoseMessage firehoseMessage){
        if(!slacks.containsKey(firehoseMessage.service)){
            return;
        }

        slacks.get(firehoseMessage.service).post(firehoseMessage);
    }
}

class Slack implements SlackMessagePostedListener {
    private SlackConfig    slackConfig;
    private SlackSession   slackSession;
    private TetradCallback callback;

    public Slack(JsonNode configuration){
        ObjectMapper mapper = new ObjectMapper();
        slackConfig = new SlackConfig(configuration);
    }

    void run(TetradCallback callback){
        slackSession = SlackSessionFactory.createWebSocketSlackSession(slackConfig.botid);
        slackSession.addMessagePostedListener(this);
        this.callback = callback;
        try {
            slackSession.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getIdentifier(){
        return slackConfig.identifier;
    }

    void post(FirehoseMessage firehoseMessage){
        if(!slackSession.isConnected()) return;
        SlackChannel slackChannel = slackSession.findChannelByName(firehoseMessage.channel);
        if(slackChannel == null){
            return;
        }
        slackSession.sendMessage(slackChannel, "*" + firehoseMessage.user + "*: " + firehoseMessage.content);
    }

    @Override
    public void onEvent(SlackMessagePosted event, SlackSession session) {
        SlackUser sender = event.getSender();

        Boolean isBot = slackConfig.ignore.getOrDefault(sender.getUserName(), null);
        if(isBot != null && isBot == sender.isBot()){
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
}

class SlackConfig {
    String botid;
    String identifier;
    public Map<String, Boolean> ignore   = new HashMap<>();
    public List<String>         channels = new ArrayList<>();

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
