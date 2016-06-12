package com.dmitriid.tetrad.interfaces;

import com.dmitriid.tetrad.services.FirehoseMessage;

public interface ITransformer {
    public FirehoseMessage transform(FirehoseMessage firehoseMessage, IManagedService service);
    public FirehoseMessage transform(FirehoseMessage firehoseMessage);
}
