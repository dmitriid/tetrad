package com.dmitriid.tetrad;

import com.dmitriid.tetrad.interfaces.IManagedService;
import com.dmitriid.tetrad.services.ServiceConfiguration;
import com.dmitriid.tetrad.services.ServiceException;
import com.dmitriid.tetrad.utils.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class Tetrad {

  private final IManagedService      service;
  private final ServiceConfiguration configuration;

  public static void main(String[] args) {

    Logger logger = LoggerFactory.getLogger(Tetrad.class.getCanonicalName());
    logger.debug("Startup with args " + Arrays.toString(args));

    CommandLine line = new CommandLine(args);
    String configFile = line.optionValue("config");

    ServiceConfiguration configuration = new ServiceConfiguration(configFile);
    String handler = configuration.getConfiguration().at("/handler").asText();

    IManagedService service = TetradObjectFactory.getService(handler);

    new Tetrad(service, configuration).run();
  }


  private Tetrad(IManagedService service, ServiceConfiguration configuration){
    this.service = service;
    this.configuration = configuration;
  }

  private void run() {
    Logger logger = LoggerFactory.getLogger(service.getClass().getCanonicalName());
    logger.debug("Tetrad::run");

    try{
      service.init(configuration);
      service.start();
      Thread.currentThread().join();
    } catch(Exception e){
      e.printStackTrace();
    } finally {
      try {
        service.shutdown();
      } catch(Exception e){
        e.printStackTrace();
      }
    }
  }
}
