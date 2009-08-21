package com.consol.citrus.adapter.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.message.MessageHandler;

public class StaticResponseProducingMessageHandler implements MessageHandler {
    
    private String messagePayload = "";
    
    private Map<String, Object> messageHeader = new HashMap<String, Object>();
    
    public Message handleMessage(Message message) {
        return MessageBuilder.withPayload(messagePayload).copyHeaders(messageHeader).build();
    }

    /**
     * @param messagePayload the messagePayload to set
     */
    public void setMessagePayload(String messagePayload) {
        this.messagePayload = messagePayload;
    }

    /**
     * @param messageHeader the messageHeader to set
     */
    public void setMessageHeader(Map<String, Object> messageHeader) {
        this.messageHeader = messageHeader;
    }

}
