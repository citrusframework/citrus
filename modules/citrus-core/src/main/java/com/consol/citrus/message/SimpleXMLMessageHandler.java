/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
