package com.dmitriid.tetrad.services;

public class ServiceException extends Exception {
  private ServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public static ServiceException StartException(String reason){
    return new ServiceException(reason, new Throwable("Error on startup"));
  }

  public static ServiceException RunException(String reason){
    return new ServiceException(reason, new Throwable("Error on run"));
  }

  public static ServiceException ShutdownException(String reason){
    return new ServiceException(reason, new Throwable("Error on shutdown"));
  }
}
