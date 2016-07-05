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

package com.dmitriid.tetrad.services;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FirehoseMessage implements Cloneable {
    public String type;  // "slack", "xmpp"
    public String subtype; // "post", "edit", "subscribe", "login"
    public String user; // "xyz", "foo@bar
    public String service; // "c.j.r", "erlyclub"
    public String channel; // "random", "erlang-talks", "erlanger"
    public String content;

    public FirehoseMessage() {
    } // for Jackson

    public FirehoseMessage(
            String type,
            String subtype,
            String user,
            String service,
            String channel,
            String content
    ) {
        this.type = type;
        this.subtype = subtype;
        this.user = user;
        this.service = service;
        this.channel = channel;
        this.content = content;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return new FirehoseMessage(
                    this.type,
                    this.subtype,
                    this.user,
                    this.service,
                    this.channel,
                    this.content
            );
        }
    }

    public String toLogString() {
        return MessageFormat.format("type: {0}, subtype: {1}, user: {2}, service: {3}, channel: {4}, content: {5}",
                type, subtype, user, service, channel,
                Arrays.stream(content.split("\n")).collect(Collectors.joining(" ")));
    }

}
