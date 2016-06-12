package com.dmitriid.tetrad.transformers;

import com.dmitriid.tetrad.interfaces.IManagedService;
import com.dmitriid.tetrad.interfaces.ITransformer;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vdurmont.emoji.EmojiParser;
import jdk.nashorn.internal.parser.JSONParser;
import sun.misc.Regexp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransformSlackSmileys implements ITransformer {

    public FirehoseMessage transform(FirehoseMessage firehoseMessage, IManagedService service){
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
