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

package org.citrusframework.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;

/**
 * Action purges all messages from a message channel instance. Message channel must be
 * of type {@link org.springframework.integration.channel.QueueChannel}. Action receives
 * a list of channel objects or a list of channel names that are resolved dynamically at runtime.
 *
 * @author Christoph Deppisch
 */
public class PurgeMessageChannelAction extends AbstractTestAction {
    /** List of channel names to be purged */
    private final List<String> channelNames;

    /** List of channels to be purged */
    private final List<MessageChannel> channels;

    /** Channel resolver instance */
    private final DestinationResolver<MessageChannel> channelResolver;

    /** Selector filter messages to be purged on channels */
    private final MessageSelector messageSelector;

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(PurgeMessageChannelAction.class);

    /**
     * Default constructor.
     */
    public PurgeMessageChannelAction(Builder builder) {
        super("purge-channel", builder);

        this.channelNames = builder.channelNames;
        this.channels = builder.channels;
        this.channelResolver = builder.channelResolver;
        this.messageSelector = builder.messageSelector;
    }

    @Override
    public void doExecute(TestContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug("Purging message channels ...");
        }

        for (MessageChannel channel : channels) {
            purgeChannel(channel);
        }

        for (String channelName : channelNames) {
            purgeChannel(resolveChannelName(channelName));
        }

        logger.info("Purged message channels");
    }

    /**
     * Purges all messages from a message channel. Prerequisite is that channel is
     * of type {@link QueueChannel}.
     *
     * @param channel
     */
    private void purgeChannel(MessageChannel channel) {
        if (channel instanceof QueueChannel) {
            List<Message<?>> messages = ((QueueChannel)channel).purge(messageSelector);

            if (logger.isDebugEnabled()) {
                logger.debug("Purged channel " + ((QueueChannel)channel).getComponentName() + " - removed " + messages.size() + " messages");
            }
        }
    }

    /**
     * Resolve the channel by name.
     * @param channelName the name to resolve
     * @return the MessageChannel object
     */
    protected MessageChannel resolveChannelName(String channelName) {
        return channelResolver.resolveDestination(channelName);
    }

    /**
     * Special message selector accepts all messages on queue channel.
     */
    public static final class AllAcceptingMessageSelector implements MessageSelector {
        public boolean accept(Message<?> message) {
            return false; // use "false" in order to include/accept all messages on queue channel
        }
    }

    /**
     * Gets the channelNames.
     * @return the channelNames to get.
     */
    public List<String> getChannelNames() {
        return channelNames;
    }

    /**
     * Gets the channels.
     * @return the channels to get.
     */
    public List<MessageChannel> getChannels() {
        return channels;
    }

    /**
     * Gets the messageSelector.
     * @return the messageSelector to get.
     */
    public MessageSelector getMessageSelector() {
        return messageSelector;
    }

    /**
     * Gets the channelResolver.
     * @return the channelResolver to get.
     */
    public DestinationResolver<MessageChannel> getChannelResolver() {
        return channelResolver;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<PurgeMessageChannelAction, Builder> implements ReferenceResolverAware {

        private final List<String> channelNames = new ArrayList<>();
        private final List<MessageChannel> channels = new ArrayList<>();
        private BeanFactory beanFactory;
        private DestinationResolver<MessageChannel> channelResolver;
        private MessageSelector messageSelector = new AllAcceptingMessageSelector();

        private ReferenceResolver referenceResolver;

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder purgeChannels() {
            return new Builder();
        }

        /**
         * Sets the messageSelector.
         * @param messageSelector the messageSelector to set
         */
        public Builder selector(MessageSelector messageSelector) {
            this.messageSelector = messageSelector;
            return this;
        }

        /**
         * Sets the bean reference resolver channel resolver for using channel names.
         * @param referenceResolver
         */
        public Builder channelResolver(ReferenceResolver referenceResolver) {
            this.channelResolver = channelName -> referenceResolver.resolve(channelName, MessageChannel.class);
            return this;
        }

        /**
         * Sets the channelResolver for using channel names.
         * @param channelResolver the channelResolver to set
         */
        public Builder channelResolver(DestinationResolver<MessageChannel> channelResolver) {
            this.channelResolver = channelResolver;
            return this;
        }

        /**
         * Adds list of channel names to purge in this action.
         * @param channelNames the channelNames to set
         */
        public Builder channelNames(List<String> channelNames) {
            this.channelNames.addAll(channelNames);
            return this;
        }

        /**
         * Adds several channel names to the list of channels to purge in this action.
         * @param channelNames
         * @return
         */
        public Builder channelNames(String... channelNames) {
            return channelNames(Arrays.asList(channelNames));
        }

        /**
         * Adds a channel name to the list of channels to purge in this action.
         * @param name
         * @return
         */
        public Builder channel(String name) {
            this.channelNames.add(name);
            return this;
        }

        /**
         * Adds list of channels to purge in this action.
         * @param channels the channels to set
         */
        public Builder channels(List<MessageChannel> channels) {
            this.channels.addAll(channels);
            return this;
        }

        /**
         * Sets several channels to purge in this action.
         * @param channels
         * @return
         */
        public Builder channels(MessageChannel... channels) {
            return channels(Arrays.asList(channels));
        }

        /**
         * Adds a channel to the list of channels to purge in this action.
         * @param channel
         * @return
         */
        public Builder channel(MessageChannel channel) {
            this.channels.add(channel);
            return this;
        }

        /**
         * Sets the Spring bean factory for using endpoint names.
         * @param applicationContext
         */
        public Builder withApplicationContext(ApplicationContext applicationContext) {
            this.beanFactory = applicationContext;
            return this;
        }

        public Builder beanFactory(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
            return this;
        }

        public Builder withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return this;
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }

        @Override
        public PurgeMessageChannelAction build() {
            if (channelResolver == null) {
                if (beanFactory != null) {
                    channelResolver = new BeanFactoryChannelResolver(beanFactory);
                } else if (!channelNames.isEmpty() && referenceResolver != null) {
                    channelResolver = channelName -> referenceResolver.resolve(channelName, MessageChannel.class);
                }
            }

            return new PurgeMessageChannelAction(this);
        }
    }

}
