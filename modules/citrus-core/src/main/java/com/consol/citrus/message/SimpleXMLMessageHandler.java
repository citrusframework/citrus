/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

/**
 * Simple implementation of a XML message handler. Handler receives and constructs simple
 * response messages with XML payload data.
 * 
 * @author Christoph Deppisch
 */
public class SimpleXMLMessageHandler implements BeanNameAware, MessageHandler {

    /** Message payload data */
    private String xmlData;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SimpleXMLMessageHandler.class);

    /** Name of the handler */
    private String handlerName;

    /**
     * @see com.consol.citrus.message.MessageHandler#handleMessage(org.springframework.integration.core.Message)
     */
    public Message<?> handleMessage(Message<?> message) {
        log.info("MessageHandler " + handlerName  + " handling message " + message.getPayload());

        Message<?> response;

        if (xmlData != null) {
            response = MessageBuilder.withPayload(xmlData).build();
        } else {
            response = MessageBuilder.withPayload("").build();
        }

        return response;
    }

    /**
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name) {
        this.handlerName = name;
    }

    /**
     * Set the XML payload data.
     * @param xmlData
     */
    public void setXmlData(String xmlData) {
        this.xmlData = xmlData;
    }
}
