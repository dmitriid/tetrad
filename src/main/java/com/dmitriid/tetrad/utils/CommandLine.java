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


import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandLine {
    private final HashMap<String, String> _options = new HashMap<>();

    // handle -o and --opt and -opt

    public CommandLine(String[] args) {
        String optionName = null;
        Matcher matcher;

        for (String o : args) {
            Pattern _optionMatch = Pattern.compile("^[-]{1,2}(\\w+)");
            matcher = _optionMatch.matcher(o);
            if (matcher.find()) {
                if (optionName != null) { // in case there was an option without a parameter before this one
                    _options.put(matcher.group(1), null);
                }
                optionName = matcher.group(1);
            } else {
                if (optionName != null) {
                    _options.put(optionName, o);
                }

                optionName = null;
            }
        }
    }

    public boolean hasOption(String... keys) {
        for (String key : keys) {
            if (_options.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    public String optionValue(String... options) {
        for (String option : options) {
            if (_options.containsKey(option)) {
                return _options.get(option);
            }
        }

        return null;
    }
}
