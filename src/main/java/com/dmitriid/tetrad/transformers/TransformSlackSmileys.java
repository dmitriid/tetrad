package com.dmitriid.tetrad.transformers;

import com.dmitriid.tetrad.interfaces.IAdapter;
import com.dmitriid.tetrad.interfaces.ITransformer;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.vdurmont.emoji.EmojiParser;

public class TransformSlackSmileys implements ITransformer {

    @Override
    public FirehoseMessage transform(FirehoseMessage firehoseMessage, IAdapter service){
        return transform(firehoseMessage);
    }

    @Override
    public FirehoseMessage transform(FirehoseMessage firehoseMessage) {
        FirehoseMessage msg = (FirehoseMessage) firehoseMessage.clone();
        msg.content = msg.content.replaceAll(":slightly_smiling_face:", ":smile:");
        msg.content = EmojiParser.parseToUnicode(msg.content);
        return msg;
    }
}
