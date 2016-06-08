package com.dmitriid.tetrad.services.slack;

import com.dmitriid.tetrad.interfaces.MQEventCallback;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

import java.util.Map;

class PostHandler implements SlackMessagePostedListener {

  private MQEventCallback callback;
  private String identifier;
  private Map<String, Boolean> ignores;

  PostHandler(MQEventCallback callback, String identifier, Map<String, Boolean> ignores){
    super();
    this.callback = callback;
    this.identifier = identifier;
    this.ignores = ignores;
  }

  @Override
  public void onEvent(SlackMessagePosted event, SlackSession session) {
    System.out.println(event.getMessageContent());
    System.out.println(event.getSender().getUserName());
    System.out.println(event.getSender().getRealName());
    System.out.println(event.getSender().isBot());
//    System.out.println(event.getJsonSource().toJSONString());

    Boolean isBot = ignores.getOrDefault(event.getSender().getUserName(), null);
    if(isBot != null && event.getSender().isBot() == isBot){
      return;
    }
    callback.execute(new FirehoseMessage(
        "slack",
        "post",
        event.getSender().getUserName(),
        identifier,
        event.getChannel().getName(),
        event.getMessageContent()
    ));
  }
}
