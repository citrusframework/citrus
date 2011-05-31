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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.integration.support.channel.ChannelResolver;
import org.springframework.util.StringUtils;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;

/**
 * Synchronous message channel sender. After sending message action will listen for reply message. A
 * {@link ReplyMessageHandler} may ask for this reply message and continue with message validation.
 * 
 * @author Christoph Deppisch
 */
public class SyncMessageChannelSender implements MessageSender, BeanFactoryAware {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SyncMessageChannelSender.class);
    
    /** Message channel */
    private MessageChannel channel;
    
    /** Destination channel name */
    private String channelName;
    
    /** Message channel template */
    private MessagingTemplate messagingTemplate = new MessagingTemplate();
    
    /** Reply message handler */
    private ReplyMessageHandler replyMessageHandler;
    
    /** Time to wait for reply message to arrive */
    private long replyTimeout = 5000L;
    
    /** Reply message correlator */
    private ReplyMessageCorrelator correlator = null;
    
    /** The parent bean factory used for channel name resolving */
    private BeanFactory beanFactory;
    
    /** Channel resolver instance */
    private ChannelResolver channelResolver;
    
    /**
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.Message)
     * @throws CitrusRuntimeException
     */
    public void send(Message<?> message) {
        String destinationChannelName = getDestinationChannelName();
        
        log.info("Sending message to channel: '" + destinationChannelName + "'");

        if (log.isDebugEnabled()) {
            log.debug("Message to sent is:\n" + message.toString());
        }

        messagingTemplate.setReceiveTimeout(replyTimeout);
        Message<?> replyMessage;
        
        replyMessage = messagingTemplate.sendAndReceive(getDestinationChannel(), message);
        
        if(replyMessage == null) {
            throw new CitrusRuntimeException("Reply timed out after " + 
                    replyTimeout + "ms. Did not receive reply message on reply channel");
        }
        
        log.info("Message was successfully sent to channel: '" + destinationChannelName + "'");
        
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
     * Get the destination channel depending on settings in this message sender.
     * Either a direct channel object is set or a channel name which will be resolved 
     * to a channel.
     * 
     * @return the destination channel object.
     */
    private MessageChannel getDestinationChannel() {
        if (channel != null) {
            return channel;
        } else if (StringUtils.hasText(channelName)) {
            return resolveChannelName(channelName);
        } else {
            throw new CitrusRuntimeException("Neither channel name nor channel object is set - " +
                    "please specify destination channel");
        }
    }

    /**
     * Gets the channel name depending on what is set in this message sender. 
     * Either channel name is set directly or channel object is consulted for channel name.
     * 
     * @return the channel name.
     */
    private String getDestinationChannelName() {
        if (channel != null) {
            return channel.toString();
        } else if (StringUtils.hasText(channelName)) {
            return channelName;
        } else {
            throw new CitrusRuntimeException("Neither channel name nor channel object is set - " +
                    "please specify destination channel");
        }
    }

    /**
     * Resolve the channel by name.
     * @param channelName the name to resolve
     * @return the MessageChannel object
     */
    private MessageChannel resolveChannelName(String channelName) {
        if (channelResolver == null) {
            channelResolver = new BeanFactoryChannelResolver(beanFactory);
        }
        
        return channelResolver.resolveChannelName(channelName);
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
     * Set the messaging template.
     * @param messagingTemplate the messagingTemplate to set
     */
    public void setMessagingTemplate(MessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Sets the bean factory for channel resolver.
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * Set the channel resolver.
     * @param channelResolver the channelResolver to set
     */
    public void setChannelResolver(ChannelResolver channelResolver) {
        this.channelResolver = channelResolver;
    }
    
    /**
     * Sets the destination channel name.
     * @param channelName the channelName to set
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
}
