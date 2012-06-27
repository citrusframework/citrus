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
import org.springframework.integration.message.GenericMessage;

import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.report.MessageTracingTestListener;

/**
 * {@link MessageReceiver} implementation consumes messages from aJMS destination. Destination
 * is given by injected instance or destination name.
 *  
 * @author Christoph Deppisch
 */
public class JmsMessageReceiver extends AbstractJmsAdapter implements MessageReceiver {
    /** Receive timeout */
    private long receiveTimeout = 5000L;
    
    @Autowired(required=false)
    private MessageTracingTestListener messageTracingTestListener;
    
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(JmsMessageReceiver.class);
    
    /**
     * @see com.consol.citrus.message.MessageReceiver#receive(long)
     * @throws ActionTimeoutException
     */
    public Message<?> receive(long timeout) {
        log.info("Waiting for JMS message on destination: '" + getDefaultDestinationName() + "'");
        
        getJmsTemplate().setReceiveTimeout(timeout);
        Object receivedObject = getJmsTemplate().receiveAndConvert();
        
        if (receivedObject == null) {
            throw new ActionTimeoutException("Action timed out while receiving JMS message on '" + getDefaultDestinationName() + "'");
        }
        
        log.info("Received JMS message on destination: '" + getDefaultDestinationName() + "'");
        
        Message<?> receivedMessage;
        if (receivedObject instanceof Message<?>) {
            receivedMessage = (Message<?>)receivedObject;
        } else {
            receivedMessage = new GenericMessage<Object>(receivedObject);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Received message is:\n" + receivedMessage.toString());
        }
        
        if (messageTracingTestListener != null) {
            messageTracingTestListener.traceMessage("Received JMS message:\n" + receivedMessage.toString());
        }
        
        return receivedMessage;
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String, long)
     * @throws ActionTimeoutException
     */
    public Message<?> receiveSelected(String selector, long timeout) {
        log.info("Waiting for JMS message on destination: '" + getDefaultDestinationName() + "(" + selector + ")'");
        
        getJmsTemplate().setReceiveTimeout(timeout);
        Object receivedObject = getJmsTemplate().receiveSelectedAndConvert(selector);
        
        if (receivedObject == null) {
            throw new ActionTimeoutException("Action timed out while receiving JMS message on '" + getDefaultDestinationName()  + "(" + selector + ")'");
        }
        
        log.info("Received JMS message on destination: '" + getDefaultDestinationName()  + "(" + selector + ")'");
        
        Message<?> receivedMessage;
        if (receivedObject instanceof Message<?>) {
            receivedMessage = (Message<?>)receivedObject;
        } else {
            receivedMessage = new GenericMessage<Object>(receivedObject);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Received message is:\n" + receivedMessage.toString());
        }
        
        return receivedMessage;
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receive()
     */
    public Message<?> receive() {
        return receive(receiveTimeout);
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String)
     */
    public Message<?> receiveSelected(String selector) {
        return receiveSelected(selector, receiveTimeout);
    }

    /**
     * Sets the receive timeout.
     * @param receiveTimeout the receiveTimeout to set
     */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    /**
     * Gets the receiveTimeout.
     * @return the receiveTimeout
     */
    public long getReceiveTimeout() {
        return receiveTimeout;
    }
}
