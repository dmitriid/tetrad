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

package com.dmitriid.tetrad.utils;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.valueOf;
import static java.util.stream.Collectors.joining;

public class JIDUtils {
    public static String jid_to_slack_username(String jid) {
        List<String> parts = Arrays.asList(jid.split("@"));
        List<String> hostParts = Arrays.asList(parts.get(1).split("/"));
        List<String> host = Arrays.asList(hostParts.get(0).split("\\."));

        return host.stream()
                .map((s) -> valueOf(s.charAt(0)))
                .collect(joining(".", parts.get(0) + "@", ""));
    }

    public static String bareJID(String jid) {
        return jid.split("/")[0];
    }

    public static String bareUsername(String jid) {
        return jid.split("@")[0];
    }
}
