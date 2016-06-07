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

    //String name = line.optionValue("n", "name");
    //String mbox_name = line.optionValue("m", "mbox");
    //String cookie = line.optionValue("c", "cookie");
    String connector = line.optionValue("connector");
    String config = line.optionValue("config");

    new Tetrad(ServiceFactory.getService(connector), new ServiceConfiguration(config)).run();
  }



  private Tetrad(ManagedService service, ServiceConfiguration configuration){
    this.service = service;
    this.configuration = configuration;
  }

  private void run() throws ServiceException {
    try{
      service.init(configuration);
      service.start();
    } catch(Exception e){
      e.printStackTrace();
    } finally {
      service.shutdown();
    }
  }
}
