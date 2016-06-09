package com.dmitriid.tetrad;

import com.dmitriid.tetrad.interfaces.ManagedService;
import com.dmitriid.tetrad.services.ServiceConfiguration;
import com.dmitriid.tetrad.services.ServiceException;
import com.dmitriid.tetrad.services.ServiceFactory;
import com.dmitriid.tetrad.utils.CommandLine;

public class Tetrad {

  private ManagedService service;
  private ServiceConfiguration configuration;

  public static void main(String[] args) throws ServiceException {
    CommandLine line = new CommandLine(args);
    String configFile = line.optionValue("config");

    ServiceConfiguration configuration = new ServiceConfiguration(configFile);
    String handler = configuration.getConfiguration().at("/handler").asText();

    ManagedService service = ServiceFactory.getService(handler);

    new Tetrad(service, configuration).run();
  }


  private Tetrad(ManagedService service, ServiceConfiguration configuration){
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
