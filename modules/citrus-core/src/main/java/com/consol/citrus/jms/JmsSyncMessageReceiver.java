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

package com.consol.citrus.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;

import org.springframework.integration.core.Message;
import org.springframework.integration.jms.JmsHeaders;

import com.consol.citrus.message.ReplyMessageCorrelator;

/**
 * Synchronous message receiver implementation for JMS. Class receives messages on a JMS destiantion
 * and saves the reply destination. As class implements the {@link JmsReplyDestinationHolder} interface
 * synchronous reply message sender implementations may ask for the reply destination later in 
 * the test execution.
 * 
 * In case a reply message correlator is set in this class the reply destiantions are stored with a given
 * correlation key. A synchronous reply message sender must ask with this specific correlation key in 
 * order to get the proper reply destiantion.
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
