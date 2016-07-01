package com.dmitriid.tetrad.transformers;

import com.dmitriid.tetrad.interfaces.IAdapter;
import com.dmitriid.tetrad.interfaces.ITransformer;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.events.SlackMessageUpdated;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class TransformSlackNiceties implements ITransformer {

  @Override
  public FirehoseMessage transform(FirehoseMessage firehoseMessage, IAdapter service, Object rawEvent) {
    if(Arrays.asList(rawEvent.getClass().getInterfaces()).contains(SlackMessageUpdated.class)) {
      return processUpdateEvent(service, (SlackMessageUpdated) rawEvent);
    }
    if(Arrays.asList(rawEvent.getClass().getInterfaces()).contains(SlackMessagePosted.class)) {
      return processPostEvent(service, (SlackMessagePosted) rawEvent);
    }
    return null;
  }

  public FirehoseMessage transform(final FirehoseMessage firehoseMessage, final IAdapter service){
    return null;
  }

  @Override
  public FirehoseMessage transform(final FirehoseMessage firehoseMessage) {
    return null;
  }


  private FirehoseMessage processUpdateEvent(IAdapter service, SlackMessageUpdated rawEvent){
    SlackMessageUpdated event = (SlackMessageUpdated) rawEvent;

    if (event.getAttachments().size() != 1) return null;

    Optional<FirehoseMessage> opt = Optional.ofNullable(getLinkExpansion(service, rawEvent));

    return opt.orElse(null);
  }

  private FirehoseMessage processPostEvent(IAdapter service, SlackMessagePosted rawEvent) {
    SlackMessagePosted event = (SlackMessagePosted) rawEvent;

    if (event.getAttachments().size() != 1) return null;

    Optional<FirehoseMessage> opt = Optional.ofNullable(getRightGIFExpansion(service, rawEvent));

    return opt.orElse(getGiphyExpansion(service, rawEvent));
  }


  private FirehoseMessage getLinkExpansion(IAdapter service, SlackMessageUpdated event) {
    SlackAttachment attachment = event.getAttachments().get(0);
    String message = event.getNewMessage();

    if (attachment.getTitleLink() == null) return null;

    if (!MessageFormat.format("<{0}>", attachment.getTitleLink()).equals(message)) {
      return null;
    }

    String words = Arrays.stream(attachment.getText().split(" "))
      .limit(20)
      .collect(Collectors.joining(" "));

    return new FirehoseMessage(
      "slack",
      "post",
      "",
      service.getIdentifier(),
      event.getChannel().getName(),
      MessageFormat.format("*{0}*: {1}...", attachment.getTitle(), words)
    );
  }

  private FirehoseMessage getRightGIFExpansion(IAdapter service, SlackMessagePosted event) {
    SlackAttachment attachment = event.getAttachments().get(0);

    if(!event.getSender().getUserName().equals("RightGIF")){
      return null;
    }

    return new FirehoseMessage(
      "slack",
      "post",
      event.getSender().getUserName(),
      service.getIdentifier(),
      event.getChannel().getName(),
      attachment.getImageUrl()
    );
  }

  private FirehoseMessage getGiphyExpansion(IAdapter service, SlackMessagePosted event) {
    SlackAttachment attachment = event.getAttachments().get(0);
    if (!event.getMessageContent().startsWith("/giphy")) {
      return null;
    }

    return new FirehoseMessage(
      "slack",
      "post",
      "Giphy",
      service.getIdentifier(),
      event.getChannel().getName(),
      attachment.getImageUrl()
    );
  }
}
