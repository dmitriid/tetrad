package com.dmitriid.tetrad.interfaces;

import com.dmitriid.tetrad.services.FirehoseMessage;

@FunctionalInterface
public interface MQEventCallback {
  void execute(FirehoseMessage message);
}
