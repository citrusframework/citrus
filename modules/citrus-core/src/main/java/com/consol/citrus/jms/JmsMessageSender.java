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

package com.consol.citrus.jms;

import javax.jms.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.integration.jms.HeaderMappingMessageConverter;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.util.Assert;

import com.consol.citrus.message.MessageSender;

/**
 * {@link MessageSender} implementation publishes message to a JMS destination.
 *  
 * @author Christoph Deppisch
 */
public class JmsMessageSender extends AbstractJmsAdapter implements MessageSender {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(JmsMessageSender.class);
    
    /**
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.core.Message)
     */
    public void send(Message<?> message) {
        Assert.notNull(message, "Can not send empty message");
        
        log.info("Sending message to: " + getDestinationName());

        if (log.isDebugEnabled()) {
            log.debug("Message to be sent:");
            log.debug(message.toString());
        }
        
        getJmsTemplate().convertAndSend(message);
    }
    
    /**
     * Retrieve the destination name (either a queue name or a topic name).
     * @return the destinationName
     */
    protected String getDestinationName() {
        try {
            if(getJmsTemplate().getDefaultDestination() != null) {
                if(getJmsTemplate().getDefaultDestination() instanceof Queue) {
                    return ((Queue)getJmsTemplate().getDefaultDestination()).getQueueName();
                } else if(getJmsTemplate().getDefaultDestination() instanceof Topic) {
                    return ((Topic)getJmsTemplate().getDefaultDestination()).getTopicName();
                } else {
                    return getJmsTemplate().getDefaultDestination().toString();
                }
            } else {
                return getJmsTemplate().getDefaultDestinationName();
            }
        } catch (JMSException e) {
            log.error("Error while getting destination name", e);
            return "";
        }
    }
    
    /**
     * @see org.springframework.integration.jms.AbstractJmsTemplateBasedAdapter#configureMessageConverter(org.springframework.jms.core.JmsTemplate, org.springframework.integration.jms.JmsHeaderMapper)
     */
    @Override
    protected void configureMessageConverter(JmsTemplate jmsTemplate, JmsHeaderMapper headerMapper) {
        MessageConverter converter = jmsTemplate.getMessageConverter();
        if (converter == null || !(converter instanceof HeaderMappingMessageConverter)) {
            HeaderMappingMessageConverter hmmc = new HeaderMappingMessageConverter(converter, headerMapper);
            hmmc.setExtractIntegrationMessagePayload(true);
            jmsTemplate.setMessageConverter(hmmc);
        }
    }
}
