package com.consol.citrus.dsl;

import com.consol.citrus.actions.ReceiveTimeoutAction;
import com.consol.citrus.message.MessageReceiver;

public class ReceiveTimeoutActionDefinition extends AbstractActionDefinition<ReceiveTimeoutAction> {

	public ReceiveTimeoutActionDefinition(ReceiveTimeoutAction action) {
	    super(action);
    }

	public ReceiveTimeoutActionDefinition timeout(long timeout) {
		action.setTimeout(timeout);
		return this;
	}
	
	public ReceiveTimeoutActionDefinition messageSelector(String messageSelector) {
		action.setMessageSelector(messageSelector);
		return this;
	}
	
	public ReceiveTimeoutActionDefinition messageReceiver(MessageReceiver messageReceiver) {
		action.setMessageReceiver(messageReceiver);
		return this;
	}
}
