package com.consol.citrus.ws.handler;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.message.MessageHandler;

public class EmptyResponseProducingMessageHandler implements MessageHandler {

    public Message handleMessage(Message message) {
        return MessageBuilder.withPayload("").build();
    }

}
