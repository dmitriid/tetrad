package com.dmitriid.tetrad;

import com.dmitriid.tetrad.interfaces.IManagedService;
import com.dmitriid.tetrad.interfaces.ITransformer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static javafx.scene.input.KeyCode.T;

public class TetradObjectFactory {

    public static IManagedService getService(String name){
        return (IManagedService) getObject("com.dmitriid.tetrad.services." + name);
    }

    public static ITransformer getTransformer(String name){
        return (ITransformer) getObject("com.dmitriid.tetrad.transformers." + name);
    }

    private static Object getObject(String name) {
        Class[] intArgsClass = new Class[]{};

        Constructor<Object> intArgsConstructor = null;
        try {
            intArgsConstructor =
                    (Constructor<Object>) Class.forName(name).getConstructor(intArgsClass);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return createObject(intArgsConstructor, new Object[0]);
    }

    private static Object createObject(Constructor<Object> constructor, Object[] arguments) {

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
