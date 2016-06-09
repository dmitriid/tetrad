package com.dmitriid.tetrad;

import com.dmitriid.tetrad.interfaces.ManagedService;
import com.dmitriid.tetrad.services.FirehoseMessage;
import com.dmitriid.tetrad.services.ServiceConfiguration;
import com.dmitriid.tetrad.services.ServiceException;
import com.dmitriid.tetrad.services.utils.JIDUtils;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class MappingService implements ManagedService {
    private List<TetradMap> mapping;

    TetradMQTT mqtt;


    @Override
    public void init(ServiceConfiguration configuration) throws ServiceException {
        mqtt = new TetradMQTT(configuration.getConfiguration().at("/mqtt"));

        mapping = new ArrayList<>();
        for(JsonNode map : configuration.getConfiguration().at("/mapping")){
            mapping.add(new TetradMap(map));
        }
    }

    @Override
    public void start() throws ServiceException {
        mqtt.start(this::firehose);
    }

    @Override
    public void shutdown() throws ServiceException {

    }

    public void firehose(FirehoseMessage firehoseMessage){
        mapping.stream().forEach(tetradMap -> {
            if(tetradMap.matches(firehoseMessage)){
                mqtt.sendMessage(tetradMap.convert(firehoseMessage));
            }
        });
    }
}

class TetradMap {
    private final String fromService;
    private final String fromChannel;
    private final String fromType;
    private final String fromSubtype;

    private final String toService;
    private final String toChannel;
    private final String toType;
    private final String toSubtype;

    private Boolean shortUserNames;

    TetradMap(JsonNode config){
        fromService = config.at("/from_service").asText(null);
        fromChannel = config.at("/from_channel").asText(null);
        fromType = config.at("/from_type").asText(null);
        fromSubtype = config.at("/from_subtype").asText(null);

        toService = config.at("/to_service").asText(null);
        toChannel = config.at("/to_channel").asText(null);
        toType = config.at("/to_type").asText(null);
        toSubtype = config.at("/to_subtype").asText(null);

        shortUserNames = config.at("/short_username").asBoolean(false);
    }

    Boolean matches(FirehoseMessage firehoseMessage) {
        return !(fromService != null && !fromService.equals(firehoseMessage.service))
                && !(fromChannel != null && !fromChannel.equals(firehoseMessage.channel))
                && !(fromType != null && !fromType.equals(firehoseMessage.type))
                && !(fromSubtype != null && !fromSubtype.equals(firehoseMessage.subtype));

    }

    FirehoseMessage convert(FirehoseMessage firehoseMessage){
        FirehoseMessage fm = (FirehoseMessage) firehoseMessage.clone();

        if(toService != null){
            fm.service = toService;
        }
        if(toChannel != null){
            fm.channel = toChannel;
        }
        if(toType != null){
            fm.type = toType;
        }
        if(toSubtype != null){
            fm.subtype = toSubtype;
        }

        if(shortUserNames){
            fm.user = JIDUtils.jid_to_slack_username(fm.user);
        }

        return fm;
    }
}
