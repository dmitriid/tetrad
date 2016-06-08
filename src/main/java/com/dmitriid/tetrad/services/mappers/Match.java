package com.dmitriid.tetrad.services.mappers;

/**
 * Created by dmitriid on 07/06/16.
 */
class Match {
    private String slackService = null;
    private String slackRoom    = null;

    Match(String slackService, String slackRoom) {
        this.slackService = slackService;
        this.slackRoom = slackRoom;
    }

    public String getSlackService() {
        return slackService;
    }

    public String getslackRoom() {
        return slackRoom;
    }

}
