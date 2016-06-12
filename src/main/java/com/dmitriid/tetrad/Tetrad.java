package com.dmitriid.tetrad;

import com.dmitriid.tetrad.interfaces.IManagedService;
import com.dmitriid.tetrad.services.ServiceConfiguration;
import com.dmitriid.tetrad.services.ServiceException;
import com.dmitriid.tetrad.utils.CommandLine;

public class Tetrad {

  private final IManagedService      service;
  private final ServiceConfiguration configuration;

  public static void main(String[] args) throws ServiceException {
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

  private void run() throws ServiceException {
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
