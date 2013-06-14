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

import com.consol.citrus.report.MessageListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
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
    private static Logger log = LoggerFactory.getLogger(JmsMessageSender.class);
    
    @Autowired(required = false)
    private MessageListeners messageListener;
    
    /**
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.Message)
     */
    public void send(Message<?> message) {
        Assert.notNull(message, "Message is empty - unable to send empty message");
        
        String defaultDestinationName = getDefaultDestinationName();
        
        log.info("Sending JMS message to destination: '" + defaultDestinationName + "'");

        getJmsTemplate().convertAndSend(message);

        if (messageListener != null) {
            messageListener.onOutboundMessage(message.toString());
        } else {
            log.info("Sent message is:" + System.getProperty("line.separator") + message.toString());
        }

        log.info("Message was successfully sent to destination: '" + defaultDestinationName + "'");
    }
}
