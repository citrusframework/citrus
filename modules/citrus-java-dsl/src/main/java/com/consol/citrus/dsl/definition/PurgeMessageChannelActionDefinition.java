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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.actions.PurgeMessageChannelAction;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;

import java.util.Arrays;
import java.util.List;

/**
 * Action purges all messages from a message channel instance. Message channel must be
 * of type {@link org.springframework.integration.channel.QueueChannel}. Action receives a
 * list of channel objects or a list of channel names that are resolved dynamically at runtime.
 * 
 * @author Max Argyo, Giulia DelBravo
 * @since 1.3
 * @deprecated since 2.3 in favor of using {@link com.consol.citrus.dsl.builder.PurgeChannelsBuilder}
 */
public class PurgeMessageChannelActionDefinition extends AbstractActionDefinition<PurgeMessageChannelAction> {

    /**
     * Default constructor using test action and application context
     * @param action
     */
	public PurgeMessageChannelActionDefinition(PurgeMessageChannelAction action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public PurgeMessageChannelActionDefinition() {
		super(new PurgeMessageChannelAction());
	}

	/**
     * Sets the messageSelector.
     * @param messageSelector the messageSelector to set
     */
	public PurgeMessageChannelActionDefinition selector(MessageSelector messageSelector) {
		action.setMessageSelector(messageSelector);
		return this;
	}

    /**
     * Sets the Spring bean factory channel resolver for using channel names.
     * @param applicationContext
     */
    public PurgeMessageChannelActionDefinition channelResolver(ApplicationContext applicationContext) {
        action.setChannelResolver(new BeanFactoryChannelResolver(applicationContext));
        return this;
    }

	/**
     * Sets the channelResolver for using channel names.
     * @param channelResolver the channelResolver to set
     */
    public PurgeMessageChannelActionDefinition channelResolver(DestinationResolver<MessageChannel> channelResolver) {
        action.setChannelResolver(channelResolver);
        return this;
    }

	/**
     * Adds list of channel names to purge in this action.
     * @param channelNames the channelNames to set
     */
	public PurgeMessageChannelActionDefinition channelNames(List<String> channelNames) {
		action.getChannelNames().addAll(channelNames);
		return this;
	}

	/**
	 * Adds several channel names to the list of channels to purge in this action.
	 * @param channelNames
	 * @return
	 */
	public PurgeMessageChannelActionDefinition channelNames(String... channelNames) {
		return channelNames(Arrays.asList(channelNames));
	}

	/**
     * Adds a channel name to the list of channels to purge in this action.
     * @param name
     * @return
     */
    public PurgeMessageChannelActionDefinition channel(String name) {
        action.getChannelNames().add(name);
        return this;
    }

	/**
     * Adds list of channels to purge in this action.
     * @param channels the channels to set
     */
	public PurgeMessageChannelActionDefinition channels(List<MessageChannel> channels) {
		action.getChannels().addAll(channels);
		return this;
	}

	/**
	 * Sets several channels to purge in this action.
	 * @param channels
	 * @return
	 */
	public PurgeMessageChannelActionDefinition channels(MessageChannel... channels) {
		return channels(Arrays.asList(channels));
	}

	/**
     * Adds a channel to the list of channels to purge in this action.
     * @param channel
     * @return
     */
    public PurgeMessageChannelActionDefinition channel(MessageChannel channel) {
        action.getChannels().add(channel);
        return this;
	}

}
