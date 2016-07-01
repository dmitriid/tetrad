package com.dmitriid.tetrad.transformers;

import com.dmitriid.tetrad.interfaces.IAdapter;
import com.dmitriid.tetrad.interfaces.ITransformer;
import com.dmitriid.tetrad.services.FirehoseMessage;

public class TransformSlackSpecialChars implements ITransformer {

    @Override
    public FirehoseMessage transform(FirehoseMessage firehoseMessage, IAdapter service, Object rawEvent) {
        return transform(firehoseMessage);
    }

    @Override
    public FirehoseMessage transform(FirehoseMessage firehoseMessage, IAdapter service){
        return transform(firehoseMessage);
    }

    @Override
    public FirehoseMessage transform(final FirehoseMessage firehoseMessage) {
        FirehoseMessage msg = (FirehoseMessage) firehoseMessage.clone();
        msg.content = msg.content
                         .replaceAll("&gt;", ">")
                         .replaceAll("&lt;", "<")
                         .replaceAll("&amp;", "&");
        return msg;
    }
}
