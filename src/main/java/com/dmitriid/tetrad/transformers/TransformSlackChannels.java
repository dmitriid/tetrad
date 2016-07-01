package com.dmitriid.tetrad.transformers;

import com.dmitriid.tetrad.interfaces.IAdapter;
import com.dmitriid.tetrad.interfaces.ITransformer;
import com.dmitriid.tetrad.services.FirehoseMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransformSlackChannels implements ITransformer {

    @Override
    public FirehoseMessage transform(FirehoseMessage firehoseMessage, IAdapter service, Object rawEvent) {
        return transform(firehoseMessage, service);
    }

    public FirehoseMessage transform(final FirehoseMessage firehoseMessage, final IAdapter service){
        FirehoseMessage msg = (FirehoseMessage) firehoseMessage.clone();

        Pattern pattern = Pattern.compile("<#([^\\|>]+)(|[^>]+)?>");
        Matcher matcher = pattern.matcher(msg.content);

        while(matcher.find()){
            msg.content = msg.content.replace(matcher.group(0), "#" + service.getChannelById(matcher.group(1)));
        }

        return msg;
    }

    @Override
    public FirehoseMessage transform(final FirehoseMessage firehoseMessage) {
        return firehoseMessage;
    }
}
