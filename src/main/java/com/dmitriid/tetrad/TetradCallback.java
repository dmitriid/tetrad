package com.dmitriid.tetrad;

import com.dmitriid.tetrad.services.FirehoseMessage;

@FunctionalInterface
interface TetradCallback {
    void execute(FirehoseMessage message);
}
