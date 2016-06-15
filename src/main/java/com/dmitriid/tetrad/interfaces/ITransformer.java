package com.dmitriid.tetrad.interfaces;

import com.dmitriid.tetrad.services.FirehoseMessage;

public interface ITransformer {
    public FirehoseMessage transform(final FirehoseMessage firehoseMessage, final IGenericService service);
    public FirehoseMessage transform(final FirehoseMessage firehoseMessage);
}
