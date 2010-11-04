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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.integration.channel.*;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageSender;

/**
 * Publish message to a {@link MessageChannel}.
 * 
 * @author Christoph Christoph
 */
public class MessageChannelSender implements MessageSender, ApplicationContextAware {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(MessageChannelSender.class);
    
    /** Destination channel */
    private MessageChannel channel;
    
    /** Message channel template */
    private MessageChannelTemplate messageChannelTemplate = new MessageChannelTemplate();
    
    /** The parent application context used for channel name resolving */
    private ApplicationContext applicationContext;
    
    /** Channel resolver instance */
    private ChannelResolver channelResolver;
    
    /**
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.core.Message)
     * @throws CitrusRuntimeException
     */
    public void send(Message<?> message) {
        String channelName = channel.getName();
        
        log.info("Sending message to channel: '" + channelName + "'");

        if (log.isDebugEnabled()) {
            log.debug("Message to send is:\n" + message.toString());
        }
        
        if (!messageChannelTemplate.send(message, channel)) {
            throw new CitrusRuntimeException("Failed to send message to channel: '" + channelName + "'");
        }
        
        log.info("Message was successfully sent to channel: '" + channelName + "'");
    }
    
    /**
     * Resolve the channel by name.
     * @param channelName the name to resolve
     * @return the MessageChannel object
     */
    private MessageChannel resolveChannelName(String channelName) {
        if (channelResolver == null) {
            channelResolver = new BeanFactoryChannelResolver(applicationContext);
        }
        
        return channelResolver.resolveChannelName(channelName);
    }

    /**
     * Set the message channel.
     * @param channel the channel to set
     */
    public void setChannel(MessageChannel channel) {
        this.channel = channel;
    }

    /**
     * Sets the message channel template.
     * @param messageChannelTemplate the messageChannelTemplate to set
     */
    public void setMessageChannelTemplate(
            MessageChannelTemplate messageChannelTemplate) {
        this.messageChannelTemplate = messageChannelTemplate;
    }

    /**
     * Set the Spring application context.
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Set the channel resolver.
     * @param channelResolver the channelResolver to set
     */
    public void setChannelResolver(ChannelResolver channelResolver) {
        this.channelResolver = channelResolver;
    }
}
