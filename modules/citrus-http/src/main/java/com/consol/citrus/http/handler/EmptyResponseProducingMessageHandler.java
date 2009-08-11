package com.consol.citrus.http.handler;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageHandler;

public class EmptyResponseProducingMessageHandler implements MessageHandler {

    public Message handleMessage(Message message) throws CitrusRuntimeException {
        return MessageBuilder.withPayload("").build();
    }

}
