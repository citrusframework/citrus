package com.consol.citrus.dsl;

import com.consol.citrus.actions.ReceiveTimeoutAction;
import com.consol.citrus.message.MessageReceiver;

/**
 * Action expecting a timeout on a message destination, this means that no message 
 * should arrive on the destination.
 */
public class ReceiveTimeoutActionDefinition extends AbstractActionDefinition<ReceiveTimeoutAction> {

	public ReceiveTimeoutActionDefinition(ReceiveTimeoutAction action) {
	    super(action);
    }

	/**
     * Setter for receive timeout.
     * @param timeout
     */
	public ReceiveTimeoutActionDefinition timeout(long timeout) {
		action.setTimeout(timeout);
		return this;
	}
	
	/**
     * Set message selector string.
     * @param messageSelector
     */
	public ReceiveTimeoutActionDefinition messageSelector(String messageSelector) {
		action.setMessageSelector(messageSelector);
		return this;
	}
	
	/**
     * Set the message receiver instance.
     * @param messageReceiver the messageReceiver to set
     */
	public ReceiveTimeoutActionDefinition messageReceiver(MessageReceiver messageReceiver) {
		action.setMessageReceiver(messageReceiver);
		return this;
	}
}
