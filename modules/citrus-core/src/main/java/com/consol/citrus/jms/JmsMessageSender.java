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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.util.Assert;

import com.consol.citrus.message.MessageSender;
import com.consol.citrus.report.MessageTracingTestListener;

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
    
    @Autowired(required=false)
    private MessageTracingTestListener messageTracingTestListener;
    
    /**
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.Message)
     */
    public void send(Message<?> message) {
        Assert.notNull(message, "Message is empty - unable to send empty message");
        
        String defaultDestinationName = getDefaultDestinationName();
        
        log.info("Sending JMS message to destination: '" + defaultDestinationName + "'");

        if (log.isDebugEnabled()) {
            log.debug("Message to send is:\n" + message.toString());
        }
        
        getJmsTemplate().convertAndSend(message);
        
        if (messageTracingTestListener != null) {
            messageTracingTestListener.traceMessage("Send JMS message:\n" + message.toString());
        }
        
        log.info("Message was successfully sent to destination: '" + defaultDestinationName + "'");
    }
}
