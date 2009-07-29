package com.consol.citrus.message;

import org.springframework.integration.core.Message;

public interface MessageReceiver {
    Message<?> receive();
    
    Message<?> receive(long timeout);
    
    Message<?> receiveSelected(String selector);
    
    Message<?> receiveSelected(String selector, long timeout);
}
