package com.consol.citrus.dsl;

import java.util.Arrays;
import java.util.List;

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
	
	public PurgeMessageChannelActionDefinition channelNames(String... channelNames) {
		List<String> channels = Arrays.asList(channelNames);
		action.setChannelNames(channels);
		return this;
	}
}
