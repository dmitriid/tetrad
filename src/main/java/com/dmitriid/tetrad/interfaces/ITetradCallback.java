package com.dmitriid.tetrad.interfaces;

import com.dmitriid.tetrad.services.FirehoseMessage;

@FunctionalInterface
public interface ITetradCallback {
    void execute(FirehoseMessage message);
}
