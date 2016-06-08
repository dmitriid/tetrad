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


    private List<String> ignore;

    private String xmppService;
    private String xmppRoom;
    private String slackService;
    private String slackRoom;

    Mapping(JsonNode mapping){
        for (final JsonNode config : mapping) {
            String service = config.at("/xmpp/room").asText() + "@" + config.at("/xmpp/service").asText();
            map.put(service,
                    new Match(config.at("/slack/service").asText(), config.at("/slack/room").asText())
                   );

            ArrayList<String> ignrs = new ArrayList<>();

            for(final JsonNode i : config.at("/xmpp/ignores")){
                ignrs.add(i.asText());
            }

            ignores.put(service, ignrs);
        }
    }


    Match match(FirehoseMessage message){
        return map.getOrDefault(message.channel, null);
    }
}
