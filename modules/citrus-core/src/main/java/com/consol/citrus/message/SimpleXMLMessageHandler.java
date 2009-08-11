package com.consol.citrus.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

public class SimpleXMLMessageHandler implements BeanNameAware, MessageHandler {

    private String xmlData;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SimpleXMLMessageHandler.class);

    private String handlerName;

    public Message handleMessage(Message message) {
        log.info("MessageHandler " + handlerName  + " handling message " + message.getPayload());

        Message response;

        if (xmlData != null) {
            response = MessageBuilder.withPayload(xmlData).build();
        } else {
            response = MessageBuilder.withPayload("").build();
        }

        return response;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name) {
        this.handlerName = name;
    }


    public void setXmlData(String xmlData) {
        this.xmlData = xmlData;
    }
}
