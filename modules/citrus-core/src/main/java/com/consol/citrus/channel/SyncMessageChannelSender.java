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

package com.consol.citrus.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.channel.MessageChannelTemplate;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;

/**
 * Synchronous message channel sender. After sending message action will listen for reply message. A
 * {@link ReplyMessageHandler} may ask for this reply message and continue with message validation.
 * 
 * @author Christoph Deppisch
 */
public class SyncMessageChannelSender implements MessageSender {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SyncMessageChannelSender.class);
    
    /** Message channel */
    private MessageChannel channel;
    
    /** Message channel template */
    private MessageChannelTemplate messageChannelTemplate = new MessageChannelTemplate();
    
    /** Reply message handler */
    private ReplyMessageHandler replyMessageHandler;
    
    /** Time to wait for reply message to arrive */
    private long replyTimeout = 5000L;
    
    /** Reply message correlator */
    private ReplyMessageCorrelator correlator = null;
    
    /**
     * @see MessageSender#send(Message)
     * @throws CitrusRuntimeException
     */
    public void send(Message<?> message) {
        log.info("Sending message to: " + channel.getName());

        if (log.isDebugEnabled()) {
            log.debug("Message to be sent:");
            log.debug(message.toString());
        }

        messageChannelTemplate.setReceiveTimeout(replyTimeout);
        Message<?> replyMessage = messageChannelTemplate.sendAndReceive(message, channel);
        
        if(replyMessage == null) {
            throw new CitrusRuntimeException("Reply timed out after " + replyTimeout + "ms. Did not receive reply message on channel");
        }
        
        if(replyMessageHandler != null) {
            if(correlator != null) {
                replyMessageHandler.onReplyMessage(replyMessage,
                    correlator.getCorrelationKey(message));
            } else {
                replyMessageHandler.onReplyMessage(replyMessage);
            }
        }
    }

    /**
     * Set the reply message handler
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;
    }

    /**
     * Get the reply message handler.
     * @return the replyMessageHandler
     */
    public ReplyMessageHandler getReplyMessageHandler() {
        return replyMessageHandler;
    }

    /**
     * Set the reply timeout.
     * @param replyTimeout the replyTimeout to set
     */
    public void setReplyTimeout(long replyTimeout) {
        this.replyTimeout = replyTimeout;
    }

    /**
     * Get the reply timeout.
     * @return the replyTimeout
     */
    public long getReplyTimeout() {
        return replyTimeout;
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }

    /**
     * Get the reply message correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return correlator;
    }

    /**
     * Set the message channel to publish messages on.
     * @param channel the channel to set
     */
    public void setChannel(MessageChannel channel) {
        this.channel = channel;
    }

    /**
     * Set the message channel template.
     * @param messageChannelTemplate the messageChannelTemplate to set
     */
    public void setMessageChannelTemplate(
            MessageChannelTemplate messageChannelTemplate) {
        this.messageChannelTemplate = messageChannelTemplate;
    }
}
