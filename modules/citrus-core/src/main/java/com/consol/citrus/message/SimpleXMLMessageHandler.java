package com.consol.citrus.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;

import com.consol.citrus.exceptions.TestSuiteException;

public class SimpleXMLMessageHandler implements BeanNameAware, MessageHandler {

    private String xmlData;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SimpleXMLMessageHandler.class);

    private String handlerName;

    public Message handleMessage(Message message) throws TestSuiteException {
        log.info("MessageHandler " + handlerName  + " handling message " + message.getMessagePayload());

        Message response = new XMLMessage();

        if (xmlData != null) {
            response.setMessagePayload(xmlData);
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
