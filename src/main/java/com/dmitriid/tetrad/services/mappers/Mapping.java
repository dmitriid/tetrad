package com.dmitriid.tetrad.services.mappers;

import com.dmitriid.tetrad.services.FirehoseMessage;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Mapping {
    private Map<String, Match>        map     = new HashMap<>();
    private Map<String, List<String>> ignores = new HashMap<>();

    Mapping(JsonNode mapping, String from, String to){
        for (final JsonNode config : mapping) {
            String service = config.at("/" + from + "/room").asText() + "@" + config.at("/" + from +"/service").asText();
            map.put(service,
                    new Match(config.at("/" + to + "/service").asText(), config.at("/" + to + "/room").asText())
                   );

            ArrayList<String> ignrs = new ArrayList<>();

            for(final JsonNode i : config.at("/" + from + "/ignores")){
                ignrs.add(i.asText());
            }

            ignores.put(service, ignrs);
        }
    }


    Match match(FirehoseMessage message){
        Match match = map.getOrDefault(message.channel, null);
        if(match == null) {
            String service = message.channel + "@" + message.service;
            match = map.getOrDefault(service, null);
            if(match == null) {
                return null;
            } else if(ignores.containsKey(service)){
                if (ignores.get(service).contains(message.user)) {
                    return null;
                }
            }
        } else if(ignores.containsKey(message.channel)){
            if(ignores.get(message.channel).contains(message.user)){
                return null;
            }
        }

        return match;
    }
}
