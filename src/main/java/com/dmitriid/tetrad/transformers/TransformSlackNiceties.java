/*******************************************************************************
 * Copyright (c) 2016 Dmitrii "Mamut" Dimandt <dmitrii@dmitriid.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

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
  public FirehoseMessage transform(FirehoseMessage firehoseMessage, IAdapter service,
      Object rawEvent) {
    if(Arrays.asList(rawEvent.getClass().getInterfaces()).contains(SlackMessageUpdated.class)) {
      return processUpdateEvent(service, (SlackMessageUpdated) rawEvent);
    }
    if(Arrays.asList(rawEvent.getClass().getInterfaces()).contains(SlackMessagePosted.class)) {
      return processPostEvent(service, (SlackMessagePosted) rawEvent);
    }
    return null;
  }

  public FirehoseMessage transform(final FirehoseMessage firehoseMessage, final IAdapter service) {
    return null;
  }

  @Override
  public FirehoseMessage transform(final FirehoseMessage firehoseMessage) {
    return null;
  }

  private FirehoseMessage processUpdateEvent(IAdapter service, SlackMessageUpdated rawEvent) {
    SlackMessageUpdated event = rawEvent;

    if(event.getAttachments().size() != 1) {
      return null;
    }

    Optional<FirehoseMessage> opt = Optional.ofNullable(getLinkExpansion(service, rawEvent));

    return opt.orElse(null);
  }

  private FirehoseMessage processPostEvent(IAdapter service, SlackMessagePosted rawEvent) {
    SlackMessagePosted event = rawEvent;

    if(event.getAttachments().size() != 1) {
      return null;
    }

    Optional<FirehoseMessage> opt =
        Optional.ofNullable(getImageFromIntegrations(service, rawEvent));

    return opt.orElse(getGiphyExpansion(service, rawEvent));
  }

  private FirehoseMessage getLinkExpansion(IAdapter service, SlackMessageUpdated event) {
    SlackAttachment attachment = event.getAttachments().get(0);
    String message = event.getNewMessage();

    if(attachment.getTitleLink() == null) {
      return null;
    }

    if(!MessageFormat.format("<{0}>", attachment.getTitleLink()).equals(message)) {
      return null;
    }

    String words = Arrays.stream(attachment.getText().split(" "))
        .limit(20)
        .collect(Collectors.joining(" "));

    return new FirehoseMessage(
        "slack",
        "post",
        "β",
        service.getIdentifier(),
        event.getChannel().getName(),
        MessageFormat.format("*{0}*: {1}...", attachment.getTitle(), words)
    );
  }

  private FirehoseMessage getImageFromIntegrations(IAdapter service, SlackMessagePosted event) {
    SlackAttachment attachment = event.getAttachments().get(0);

    String userName = event.getSender().getUserName();

    if(!userName.equals("RightGIF") && !userName.equals("ZOMG Memes!")) {
      return null;
    }

    return new FirehoseMessage(
        "slack",
        "post",
        userName,
        service.getIdentifier(),
        event.getChannel().getName(),
        attachment.getImageUrl()
    );
  }

  private FirehoseMessage getGiphyExpansion(IAdapter service, SlackMessagePosted event) {
    SlackAttachment attachment = event.getAttachments().get(0);
    if(!event.getMessageContent().startsWith("/giphy")) {
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
