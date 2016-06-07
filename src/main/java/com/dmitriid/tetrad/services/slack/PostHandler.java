package com.dmitriid.tetrad.services.slack;

import com.dmitriid.tetrad.interfaces.MQEventCallback;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

class PostHandler implements SlackMessagePostedListener {

  private MQEventCallback callback;
  private String identifier;

  PostHandler(MQEventCallback callback, String identifier){
    super();
    this.callback = callback;
    this.identifier = identifier;
  }

  @Override
  public void onEvent(SlackMessagePosted event, SlackSession session) {
    System.out.println(event.getMessageContent());
    System.out.println(event.getSender().getUserName());
    System.out.println(event.getSender().getRealName());
    System.out.println(event.getSender().isBot());
//    System.out.println(event.getJsonSource().toJSONString());

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
