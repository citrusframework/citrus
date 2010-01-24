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

import java.util.HashMap;
import java.util.Map;

import org.springframework.integration.core.Message;

/**
 * Abstract base class for reply message receiver implementations. In addition to the usual
 * {@link MessageReceiver} functionality this class implements the {@link ReplyMessageHandler} interface.
 * 
 *  This means that synchronous message senders may invoke this receiver as soon as synchronous reply
 *  has arrived. Once invoked with a reply message the class saves the reply message to a local storage.
 *  When invoked by a receiving action inside a test the store reply message is returned.
 *  
 * @author Christoph Deppisch
 */
public abstract class AbstractReplyMessageReceiver implements MessageReceiver, ReplyMessageHandler {

    /** Store of reply messages */
    private Map<String, Message<?>> replyMessages = new HashMap<String, Message<?>>();
    
    /**
     * @see com.consol.citrus.message.MessageReceiver#receive()
     */
    public Message<?> receive() {
        return getReplyMessage("");
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receive(long)
     */
    public Message<?> receive(long timeout) {
        return receive();
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String)
     */
    public Message<?> receiveSelected(String selector) {
        return getReplyMessage(selector);
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String, long)
     */
    public Message<?> receiveSelected(String selector, long timeout) {
        return receiveSelected(selector);
    }

    /**
     * @see com.consol.citrus.message.ReplyMessageHandler#onReplyMessage(org.springframework.integration.core.Message, java.lang.String)
     */
    public void onReplyMessage(Message<?> replyMessage, String correlationKey) {
        replyMessages.put(correlationKey, replyMessage);
    }
    
    /**
     * @see com.consol.citrus.message.ReplyMessageHandler#onReplyMessage(org.springframework.integration.core.Message)
     */
    public void onReplyMessage(Message<?> replyMessage) {
        onReplyMessage(replyMessage, "");
    }

    /**
     * Tries to return a reply message from the local storage. A correlation key
     * correlates messages in a multi threaded environment.
     * @param correlationKey
     * @return the reply message.
     */
    public Message<?> getReplyMessage(String correlationKey) {
        return replyMessages.remove(correlationKey);
    }
}
