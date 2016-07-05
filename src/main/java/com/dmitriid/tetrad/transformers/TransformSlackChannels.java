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

package com.dmitriid.tetrad.transformers;

import com.dmitriid.tetrad.interfaces.IAdapter;
import com.dmitriid.tetrad.interfaces.ITransformer;
import com.dmitriid.tetrad.services.FirehoseMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransformSlackChannels implements ITransformer {

    @Override
    public FirehoseMessage transform(FirehoseMessage firehoseMessage, IAdapter service, Object rawEvent) {
        return transform(firehoseMessage, service);
    }

    public FirehoseMessage transform(final FirehoseMessage firehoseMessage, final IAdapter service) {
        FirehoseMessage msg = (FirehoseMessage) firehoseMessage.clone();

        Pattern pattern = Pattern.compile("<#([^\\|>]+)(|[^>]+)?>");
        Matcher matcher = pattern.matcher(msg.content);

        while (matcher.find()) {
            msg.content = msg.content.replace(matcher.group(0), "#" + service.getChannelById(matcher.group(1)));
        }

        return msg;
    }

    @Override
    public FirehoseMessage transform(final FirehoseMessage firehoseMessage) {
        return firehoseMessage;
    }
}
