package com.consol.citrus.message;

import org.springframework.integration.core.Message;

public abstract class AbstractMessageReceiver implements MessageReceiver {

    public Message<?> receive() {
        return receive(-1);
    }

    public abstract Message<?> receive(long timeout);

    public Message<?> receiveSelected(String selector) {
        return receiveSelected(selector, -1);
    }

    public abstract Message<?> receiveSelected(String selector, long timeout);
    
}
