package com.dmitriid.tetrad.interfaces;

import com.dmitriid.tetrad.services.FirehoseMessage;

public interface ITransformer {
    FirehoseMessage transform(final FirehoseMessage firehoseMessage, final IAdapter service, final Object rawEvent);

    FirehoseMessage transform(final FirehoseMessage firehoseMessage, final IAdapter service);

    FirehoseMessage transform(final FirehoseMessage firehoseMessage);
}
