package com.consol.citrus.jms;

import javax.jms.Destination;

public interface JmsReplyDestinationHolder {
    Destination getReplyDestination();
}
