package com.dmitriid.tetrad.services;

import com.dmitriid.tetrad.interfaces.ManagedService;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ServiceFactory {
  public static ManagedService getService(String className) {
    Class[] intArgsClass = new Class[] {};

    Constructor intArgsConstructor = null;

    try {
      intArgsConstructor = Class.forName("com.dmitriid.tetrad.services." + className).getConstructor(intArgsClass);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }
    return (ManagedService) createObject(intArgsConstructor, new Object[0]);
  }

  private static Object createObject(Constructor constructor, Object[] arguments) {

    System.out.println("Constructor: " + constructor.toString());
    Object object = null;

    try {
      object = constructor.newInstance(arguments);
      System.out.println("Object: " + object.toString());
      return object;
    } catch (InstantiationException e) {
      System.out.println(e);
      System.exit(1);
    } catch (IllegalAccessException e) {
      System.out.println(e);
      System.exit(1);
    } catch (IllegalArgumentException e) {
      System.out.println(e);
      System.exit(1);
    } catch (InvocationTargetException e) {
      System.out.println(e);
      System.out.println(e.getMessage());
      e.printStackTrace();
      System.out.println(e.getCause());
      System.exit(1);
    }
    return object;
  }
}
