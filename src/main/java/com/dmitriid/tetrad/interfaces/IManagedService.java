package com.dmitriid.tetrad.interfaces;

import com.dmitriid.tetrad.services.ServiceConfiguration;
import com.dmitriid.tetrad.services.ServiceException;

public interface IManagedService {
  void init(ServiceConfiguration configuration) throws ServiceException;
  void start() throws ServiceException;

  void shutdown() throws ServiceException;
}
