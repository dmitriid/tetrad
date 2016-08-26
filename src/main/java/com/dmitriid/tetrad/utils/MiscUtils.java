package com.dmitriid.tetrad.utils;

public class MiscUtils {
    public static <T> String toRGB(T obj) {
        
        final int i = obj.hashCode();

        String r = Integer.toHexString(((i >> 16) & 0xFF));
        String g = Integer.toHexString(((i >> 8) & 0xFF));
        String b = Integer.toHexString((i & 0xFF));

        return ((r.length() == 1 ? "0" + r : r) +
                (g.length() == 1 ? "0" + g : g) +
                (b.length() == 1 ? "0" + b : b)).toUpperCase();
    }
}
