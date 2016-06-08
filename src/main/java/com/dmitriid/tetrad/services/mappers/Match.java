package com.dmitriid.tetrad.services.mappers;

class Match {
    private String service = null;
    private String room    = null;

    Match(String service, String room) {
        this.service = service;
        this.room = room;
    }

    public String getService() {
        return service;
    }

    public String getRoom() {
        return room;
    }

}
