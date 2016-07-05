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
import com.vdurmont.emoji.EmojiParser;

public class TransformSlackSmileys implements ITransformer {

    @Override
    public FirehoseMessage transform(FirehoseMessage firehoseMessage, IAdapter service, Object rawEvent) {
        return transform(firehoseMessage);
    }

    @Override
    public FirehoseMessage transform(FirehoseMessage firehoseMessage, IAdapter service) {
        return transform(firehoseMessage);
    }

    @Override
    public FirehoseMessage transform(FirehoseMessage firehoseMessage) {
        FirehoseMessage msg = (FirehoseMessage) firehoseMessage.clone();
        msg.content = msg.content.replaceAll(":slightly_smiling_face:", ":smile:");
        msg.content = EmojiParser.parseToUnicode(msg.content);
        return msg;
    }
}
