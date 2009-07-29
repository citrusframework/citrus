package com.consol.citrus.message;

import org.springframework.integration.core.Message;

public interface MessageSender {
    void send(Message<?> message);
}
