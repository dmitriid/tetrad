package com.dmitriid.tetrad.services;

/**
 * Created by dmitriid on 05/06/16.
 */
public class ServiceException extends Exception {
  public static ServiceException StartException(String reason){
    return new ServiceException(reason, new Throwable("Error on startup"));
  }

  public static ServiceException RunException(String reason){
    return new ServiceException(reason, new Throwable("Error on run"));
  }

  public static ServiceException ShutdownException(String reason){
    return new ServiceException(reason, new Throwable("Error on shutdown"));
  }

  public ServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
