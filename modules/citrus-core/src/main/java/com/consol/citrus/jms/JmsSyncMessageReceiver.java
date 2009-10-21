/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;

import org.springframework.integration.core.Message;
import org.springframework.integration.jms.JmsHeaders;

import com.consol.citrus.message.ReplyMessageCorrelator;


public class JmsSyncMessageReceiver extends JmsMessageReceiver implements JmsReplyDestinationHolder {
    
    private Map<String, Destination> replyDestinations = new HashMap<String, Destination>();
    
    private ReplyMessageCorrelator correlator = null;
    
    @Override
    public Message<?> receive(long timeout) {
        Message<?> receivedMessage = super.receive(timeout);
        
        saveReplyDestination(receivedMessage);
        
        return receivedMessage;
    }
    
    @Override
    public Message<?> receiveSelected(String selector, long timeout) {
        Message<?> receivedMessage = super.receiveSelected(selector, timeout);
        
        saveReplyDestination(receivedMessage);
        
        return receivedMessage;
    }

    /**
     * @param receivedMessage
     */
    private void saveReplyDestination(Message<?> receivedMessage) {
        if(correlator != null) {
            replyDestinations.put(correlator.getCorrelationKey(receivedMessage), (Destination)receivedMessage.getHeaders().get(JmsHeaders.REPLY_TO));
        } else {
            replyDestinations.put("", (Destination)receivedMessage.getHeaders().get(JmsHeaders.REPLY_TO));
        }
    }

    public Destination getReplyDestination(String correlationKey) {
        return replyDestinations.remove(correlationKey);
    }

    public Destination getReplyDestination() {
        return replyDestinations.remove("");
    }

    /**
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }

    
}
