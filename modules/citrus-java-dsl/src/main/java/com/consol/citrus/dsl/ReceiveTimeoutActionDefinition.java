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

package com.consol.citrus.dsl;

import com.consol.citrus.actions.ReceiveTimeoutAction;

/**
 * Action expecting a timeout on a message destination, this means that no message 
 * should arrive on the destination.
 * 
 * @author Max Argyo, Giulia DelBravo
 * @since 1.3
 */
public class ReceiveTimeoutActionDefinition extends AbstractActionDefinition<ReceiveTimeoutAction> {

	public ReceiveTimeoutActionDefinition(ReceiveTimeoutAction action) {
	    super(action);
    }

	/**
     * Sets time to wait for messages on destination.
     * @param timeout
     */
	public ReceiveTimeoutActionDefinition timeout(long timeout) {
		action.setTimeout(timeout);
		return this;
	}
	
	/**
     * Adds message selector string for selective consumer.
     * @param messageSelector
     */
	public ReceiveTimeoutActionDefinition selector(String messageSelector) {
		action.setMessageSelector(messageSelector);
		return this;
	}
	
}
