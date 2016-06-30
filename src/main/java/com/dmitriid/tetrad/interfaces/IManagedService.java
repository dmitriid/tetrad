package com.dmitriid.tetrad.interfaces;

import com.dmitriid.tetrad.services.ServiceConfiguration;
import com.dmitriid.tetrad.services.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface IManagedService {
  void init(ServiceConfiguration configuration);
  void start();

  void shutdown();
}
