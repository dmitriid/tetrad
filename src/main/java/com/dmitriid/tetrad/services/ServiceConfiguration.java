package com.dmitriid.tetrad.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public class ServiceConfiguration {
    private JsonNode configuration;

    public ServiceConfiguration(String fileName) {

        YAMLFactory ymlFactory = new YAMLFactory();

        try {
            this.configuration = new ObjectMapper().readTree(ymlFactory.createParser(new File(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonNode getConfiguration() {
        return this.configuration;
    }
}
