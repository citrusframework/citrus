package com.consol.citrus.message;

import org.springframework.integration.core.Message;

public abstract class AbstractMessageReceiver implements MessageReceiver {

    private long receiveTimeout = 5000L;
    
    public Message<?> receive() {
        return receive(receiveTimeout);
    }

    public abstract Message<?> receive(long timeout);

    public Message<?> receiveSelected(String selector) {
        return receiveSelected(selector, receiveTimeout);
    }

    public abstract Message<?> receiveSelected(String selector, long timeout);

    /**
     * @param receiveTimeout the receiveTimeout to set
     */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }
    
}
