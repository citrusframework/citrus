package com.consol.citrus.dsl;

import java.util.Arrays;
import java.util.List;

import org.springframework.integration.MessageChannel;
import org.springframework.integration.core.MessageSelector;

import com.consol.citrus.actions.PurgeMessageChannelAction;

public class PurgeMessageChannelActionDefinition extends AbstractActionDefinition<PurgeMessageChannelAction> {

	public PurgeMessageChannelActionDefinition(PurgeMessageChannelAction action) {
	    super(action);
    }
	
	public PurgeMessageChannelActionDefinition messageSelector(MessageSelector messageSelector) {
		action.setMessageSelector(messageSelector);
		return this;
	}
	
	public PurgeMessageChannelActionDefinition channelNames(List<String> channelNames) {
		action.setChannelNames(channelNames);
		return this;
	}
	
	public PurgeMessageChannelActionDefinition channelNames(String... channelNames) {
		return channelNames(Arrays.asList(channelNames));
	}
	
	public PurgeMessageChannelActionDefinition channels(List<MessageChannel> channels) {
		action.setChannels(channels);
		return this;
	}
	
	public PurgeMessageChannelActionDefinition channels(MessageChannel... channels) {
		return channels(Arrays.asList(channels));
	}
	
}
