package com.dmitriid.tetrad.services.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.*;
import static java.util.stream.Collectors.joining;

public class JIDUtils {
    public static String jid_to_slack_username(String jid){
        List<String> parts = Arrays.asList(jid.split("@"));
        List<String> hostParts = Arrays.asList(parts.get(1).split("/"));
        List<String> host = Arrays.asList(hostParts.get(0).split("\\."));

        return host.stream()
                .map((s) -> valueOf(s.charAt(0)))
                .collect(joining(".", parts.get(0) + "@", ""));
    }

    public static String bareJID(String jid){
        return jid.split("/")[0];
    }

    public static String bareUsername(String jid) {
        return jid.split("@")[0];
    }
}
