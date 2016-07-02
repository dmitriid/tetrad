package com.dmitriid.tetrad.interfaces;

import com.dmitriid.tetrad.services.ServiceConfiguration;

public interface IManagedService {
    void init(ServiceConfiguration configuration);

    void start();

    void shutdown();
}
