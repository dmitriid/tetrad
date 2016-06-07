package com.dmitriid.tetrad.services;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import java.io.File;
import java.io.IOException;
import jdk.nashorn.internal.parser.JSONParser;

import static java.lang.System.exit;

public class ServiceConfiguration {
  private JsonNode configuration;

  public ServiceConfiguration(String fileName){

    YAMLFactory ymlFactory = new YAMLFactory();

    try {
      this.configuration = new ObjectMapper().readTree(ymlFactory.createParser(new File(fileName)));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public JsonNode getConfiguration(){
    return this.configuration;
  }
}
