/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.actions;

import com.consol.citrus.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Action purges all messages from a message channel instance. Message channel must be
 * of type {@link org.springframework.integration.channel.QueueChannel}. Action receives
 * a list of channel objects or a list of channel names that are resolved dynamically at runtime.
 * 
 * @author Christoph Deppisch
 */
public class PurgeMessageChannelAction extends AbstractTestAction implements InitializingBean, BeanFactoryAware {
    /** List of channel names to be purged */
    private List<String> channelNames = new ArrayList<String>();

    /** List of channels to be purged */
    private List<MessageChannel> channels = new ArrayList<MessageChannel>();
    
    /** The parent bean factory used for channel name resolving */
    private BeanFactory beanFactory;
    
    /** Channel resolver instance */
    private DestinationResolver<MessageChannel> channelResolver;
    
    /** Selector filter messages to be purged on channels */
    private MessageSelector messageSelector;
    
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(PurgeMessageChannelAction.class);

    /**
     * Default constructor.
     */
    public PurgeMessageChannelAction() {
        setName("purge-channel");
    }

    @Override
    public void doExecute(TestContext context) {
        log.info("Purging message channels ...");
        
        for (MessageChannel channel : channels) {
            purgeChannel(channel);
        }
        
        for (String channelName : channelNames) {
            purgeChannel(resolveChannelName(channelName));
        }

        log.info("Message channel purged successfully");
    }

    /**
     * Purges all messages from a message channel. Prerequisit is that channel is
     * of type {@link QueueChannel}.
     * 
     * @param channel
     */
    private void purgeChannel(MessageChannel channel) {
        if (channel instanceof QueueChannel) {
            if (log.isDebugEnabled()) {
                log.debug("Try to purge message channel " + ((QueueChannel)channel).getComponentName());
            }
            
            List<Message<?>> messages = ((QueueChannel)channel).purge(messageSelector);
            
            if (log.isDebugEnabled()) {
                log.debug("Purged " + messages.size() + " messages from channel");
            }
        }
    }
    
    /**
     * Resolve the channel by name.
     * @param channelName the name to resolve
     * @return the MessageChannel object
     */
    protected MessageChannel resolveChannelName(String channelName) {
        if (channelResolver == null) {
            channelResolver = new BeanFactoryChannelResolver(beanFactory);
        }
        
        return channelResolver.resolveDestination(channelName);
    }
    
    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        if (messageSelector == null) {
            messageSelector = new AllAcceptingMessageSelector();
        }
    }
    
    /**
     * Special message selector accepts all messages on queue channel.
     */
    private static final class AllAcceptingMessageSelector implements MessageSelector {
        public boolean accept(Message<?> message) {
            return false; // use "false" in order to include/accept all messages on queue channel
        }
    }

    /**
     * Sets the bean factory for channel resolver.
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * Gets the channelNames.
     * @return the channelNames the channelNames to get.
     */
    public List<String> getChannelNames() {
        return channelNames;
    }

    /**
     * Sets the channelNames.
     * @param channelNames the channelNames to set
     */
    public PurgeMessageChannelAction setChannelNames(List<String> channelNames) {
        this.channelNames = channelNames;
        return this;
    }

    /**
     * Gets the channels.
     * @return the channels the channels to get.
     */
    public List<MessageChannel> getChannels() {
        return channels;
    }

    /**
     * Sets the channels.
     * @param channels the channels to set
     */
    public PurgeMessageChannelAction setChannels(List<MessageChannel> channels) {
        this.channels = channels;
        return this;
    }

    /**
     * Gets the messageSelector.
     * @return the messageSelector the messageSelector to get.
     */
    public MessageSelector getMessageSelector() {
        return messageSelector;
    }

    /**
     * Sets the messageSelector.
     * @param messageSelector the messageSelector to set
     */
    public PurgeMessageChannelAction setMessageSelector(MessageSelector messageSelector) {
        this.messageSelector = messageSelector;
        return this;
    }

    /**
     * Gets the channelResolver.
     * @return the channelResolver the channelResolver to get.
     */
    public DestinationResolver<MessageChannel> getChannelResolver() {
        return channelResolver;
    }

    /**
     * Sets the channelResolver.
     * @param channelResolver the channelResolver to set
     */
    public PurgeMessageChannelAction setChannelResolver(DestinationResolver<MessageChannel> channelResolver) {
        this.channelResolver = channelResolver;
        return this;
    }

}
