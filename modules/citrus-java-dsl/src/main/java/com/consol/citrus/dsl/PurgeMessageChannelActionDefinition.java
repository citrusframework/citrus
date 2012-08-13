package com.consol.citrus.dsl;

import java.util.Arrays;
import java.util.List;

import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageSelector;

import com.consol.citrus.actions.PurgeMessageChannelAction;

/**
 * Action purges all messages from a message channel instance. Message channel must be
 * of type {@link QueueChannel}. Action receives a list of channel objects or a list of channel names
 * that are resolved dynamically at runtime.
 */
public class PurgeMessageChannelActionDefinition extends AbstractActionDefinition<PurgeMessageChannelAction> {

	public PurgeMessageChannelActionDefinition(PurgeMessageChannelAction action) {
	    super(action);
    }
	
	/**
     * Sets the messageSelector.
     * @param messageSelector the messageSelector to set
     */
	public PurgeMessageChannelActionDefinition messageSelector(MessageSelector messageSelector) {
		action.setMessageSelector(messageSelector);
		return this;
	}
	
	 /**
     * Sets the channelNames.
     * @param channelNames the channelNames to set
     */
	public PurgeMessageChannelActionDefinition channelNames(List<String> channelNames) {
		action.setChannelNames(channelNames);
		return this;
	}
	
	public PurgeMessageChannelActionDefinition channelNames(String... channelNames) {
		return channelNames(Arrays.asList(channelNames));
	}
	
	/**
     * Sets the channels.
     * @param channels the channels to set
     */
	public PurgeMessageChannelActionDefinition channels(List<MessageChannel> channels) {
		action.setChannels(channels);
		return this;
	}
	
	public PurgeMessageChannelActionDefinition channels(MessageChannel... channels) {
		return channels(Arrays.asList(channels));
	}
	
}
