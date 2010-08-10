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

import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;

import org.springframework.integration.core.Message;
import org.springframework.integration.jms.JmsHeaders;

import com.consol.citrus.message.ReplyMessageCorrelator;

/**
 * Synchronous message receiver implementation for JMS. Class receives messages on a JMS destination
 * and saves the reply destination. As class implements the {@link JmsReplyDestinationHolder} interface
 * synchronous reply message sender implementations may ask for the reply destination later in 
 * the test execution.
 * 
 * In case a reply message correlator is set in this class the reply destinations are stored with a given
 * correlation key. A synchronous reply message sender must ask with this specific correlation key in 
 * order to get the proper reply destination.
 * 
 * @author Christoph Deppisch
 */
public class JmsSyncMessageReceiver extends JmsMessageReceiver implements JmsReplyDestinationHolder {
    /** Map of reply destinations */
    private Map<String, Destination> replyDestinations = new HashMap<String, Destination>();
    
    /** Reply message correlator */
    private ReplyMessageCorrelator correlator = null;
    
    /**
     * @see com.consol.citrus.jms.JmsMessageReceiver#receive(long)
     */
    @Override
    public Message<?> receive(long timeout) {
        Message<?> receivedMessage = super.receive(timeout);
        
        saveReplyDestination(receivedMessage);
        
        return receivedMessage;
    }
    
    /**
     * @see com.consol.citrus.jms.JmsMessageReceiver#receiveSelected(java.lang.String, long)
     */
    @Override
    public Message<?> receiveSelected(String selector, long timeout) {
        Message<?> receivedMessage = super.receiveSelected(selector, timeout);
        
        saveReplyDestination(receivedMessage);
        
        return receivedMessage;
    }

    /**
     * Store the reply destination either straight forward or with a given
     * message correlation key.
     * 
     * @param receivedMessage
     */
    private void saveReplyDestination(Message<?> receivedMessage) {
        if(correlator != null) {
            replyDestinations.put(correlator.getCorrelationKey(receivedMessage), (Destination)receivedMessage.getHeaders().get(JmsHeaders.REPLY_TO));
        } else {
            replyDestinations.put("", (Destination)receivedMessage.getHeaders().get(JmsHeaders.REPLY_TO));
        }
    }

    /**
     * @see com.consol.citrus.jms.JmsReplyDestinationHolder#getReplyDestination(java.lang.String)
     */
    public Destination getReplyDestination(String correlationKey) {
        return replyDestinations.remove(correlationKey);
    }

    /**
     * @see com.consol.citrus.jms.JmsReplyDestinationHolder#getReplyDestination()
     */
    public Destination getReplyDestination() {
        return replyDestinations.remove("");
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }
}
