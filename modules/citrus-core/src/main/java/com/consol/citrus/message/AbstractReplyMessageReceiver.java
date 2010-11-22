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
