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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;

import com.consol.citrus.TestActor;

/**
 * Generic implementation for reply message receiver implementations. In addition to the usual
 * {@link MessageReceiver} functionality this class implements the {@link ReplyMessageHandler} interface.
 * 
 *  This means that synchronous message senders may invoke this receiver as soon as synchronous reply
 *  has arrived. Once invoked with a reply message the class saves the reply message to a local storage.
 *  When invoked by a receiving action inside a test the store reply message is returned.
 *  
 * @author Christoph Deppisch
 */
public class ReplyMessageReceiver implements MessageReceiver, ReplyMessageHandler {

    /** Store of reply messages */
    private Map<String, Message<?>> replyMessages = new HashMap<String, Message<?>>();
    
    /** Polling interval when waiting for synchronous reply message to arrive */
    private long pollingInterval = 500;
    
    /** The test actor linked with this reply message receiver */
    private TestActor actor;
    
    /** Logger */
    private final Logger retry_log = LoggerFactory.getLogger("com.consol.citrus.MessageRetryLogger");
    
    /**
     * @see com.consol.citrus.message.MessageReceiver#receive()
     */
    public Message<?> receive() {
        return receiveSelected("");
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receive(long)
     */
    public Message<?> receive(long timeout) {
        return receiveSelected("", timeout);
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
        long timeLeft = timeout;
        Message<?> message = receiveSelected(selector);

        while (message == null && timeLeft > 0) {
            timeLeft -= pollingInterval;
            
            if (retry_log.isDebugEnabled()) {
                retry_log.debug("Reply message did not arrive yet - retrying in " + (timeLeft > 0 ? pollingInterval : pollingInterval + timeLeft) + "ms");
            }
            
            try {
                Thread.sleep(timeLeft > 0 ? pollingInterval : pollingInterval + timeLeft);
            } catch (InterruptedException e) {
                retry_log.warn("Thread interrupted while waiting for retry", e);
            }
            
            message = receiveSelected(selector);
        }
        
        return message;
    }

    /**
     * @see com.consol.citrus.message.ReplyMessageHandler#onReplyMessage(org.springframework.integration.Message, java.lang.String)
     */
    public void onReplyMessage(Message<?> replyMessage, String correlationKey) {
        replyMessages.put(correlationKey, replyMessage);
    }
    
    /**
     * @see com.consol.citrus.message.ReplyMessageHandler#onReplyMessage(org.springframework.integration.Message)
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

    /**
     * Gets the actor.
     * @return the actor the actor to get.
     */
    public TestActor getActor() {
        return actor;
    }

    /**
     * Sets the actor.
     * @param actor the actor to set
     */
    public void setActor(TestActor actor) {
        this.actor = actor;
    }

    /**
     * Gets the pollingInterval.
     * @return the pollingInterval the pollingInterval to get.
     */
    public long getPollingInterval() {
        return pollingInterval;
    }

    /**
     * Sets the pollingInterval.
     * @param pollingInterval the pollingInterval to set
     */
    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }
}
