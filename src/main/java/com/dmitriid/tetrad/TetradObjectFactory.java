/*******************************************************************************
 * Copyright (c) 2016 Dmitrii "Mamut" Dimandt <dmitrii@dmitriid.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

package com.dmitriid.tetrad;

import com.dmitriid.tetrad.interfaces.IManagedService;
import com.dmitriid.tetrad.interfaces.ITransformer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class TetradObjectFactory {

    public static IManagedService getService(String name) {
        return (IManagedService) getObject("com.dmitriid.tetrad.services." + name);
    }

    public static ITransformer getTransformer(String name) {
        return (ITransformer) getObject("com.dmitriid.tetrad.transformers." + name);
    }

    private static Object getObject(String name) {
        Class[] intArgsClass = new Class[]{};

        Constructor<Object> intArgsConstructor = null;
        try {
            //noinspection unchecked
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
