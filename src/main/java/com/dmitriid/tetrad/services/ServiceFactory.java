package com.dmitriid.tetrad.services;

import com.dmitriid.tetrad.interfaces.IManagedService;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ServiceFactory {
  public static IManagedService getService(String className) {
    Class[] intArgsClass = new Class[] {};

    Constructor intArgsConstructor = null;

    try {
      intArgsConstructor = Class.forName("com.dmitriid.tetrad.services." + className).getConstructor(intArgsClass);
    } catch (NoSuchMethodException | ClassNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }
    return (IManagedService) createObject(intArgsConstructor, new Object[0]);
  }

  private static Object createObject(Constructor constructor, Object[] arguments) {

    System.out.println("Constructor: " + constructor.toString());
    Object object = null;

    try {
      object = constructor.newInstance(arguments);
      System.out.println("Object: " + object.toString());
      return object;
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
      System.out.println(e);
      System.exit(1);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
      System.out.println(e.getCause().getMessage());
      System.exit(1);
    }
    return object;
  }
}
