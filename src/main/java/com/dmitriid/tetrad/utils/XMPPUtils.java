package com.dmitriid.tetrad.utils;

import org.jivesoftware.smackx.XHTMLText;

import java.util.stream.IntStream;

public class XMPPUtils {
    public static void toXMPPXHTML(String message, XHTMLText xhtml) {
        String[] parts = message.split("\n");

        IntStream.range(0, parts.length).forEach(value -> {
            xhtml.append(parts[value]);
            if (value != parts.length - 1) {
                xhtml.appendBrTag();
            }
        });
    }
}
