/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.actions.ReceiveTimeoutAction;
import com.consol.citrus.endpoint.Endpoint;

/**
 * Action expecting a timeout on a message destination, this means that no message 
 * should arrive on the destination.
 * 
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ReceiveTimeoutBuilder extends AbstractTestActionBuilder<ReceiveTimeoutAction> {

	/**
	 * Constructor using action field.
	 * @param action
	 */
	public ReceiveTimeoutBuilder(ReceiveTimeoutAction action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public ReceiveTimeoutBuilder() {
		super(new ReceiveTimeoutAction());
	}

	/**
	 * Sets the message endpoint to receive a timeout with.
	 * @param messageEndpoint
	 * @return
	 */
	public ReceiveTimeoutBuilder endpoint(Endpoint messageEndpoint) {
		action.setEndpoint(messageEndpoint);
		return this;
	}

	/**
	 * Sets the message endpoint uri to receive a timeout with.
	 * @param messageEndpointUri
	 * @return
	 */
	public ReceiveTimeoutBuilder endpoint(String messageEndpointUri) {
		action.setEndpointUri(messageEndpointUri);
		return this;
	}

	/**
     * Sets time to wait for messages on destination.
     * @param timeout
     */
	public ReceiveTimeoutBuilder timeout(long timeout) {
		action.setTimeout(timeout);
		return this;
	}
	
	/**
     * Adds message selector string for selective consumer.
     * @param messageSelector
     */
	public ReceiveTimeoutBuilder selector(String messageSelector) {
		action.setMessageSelector(messageSelector);
		return this;
	}
	
}
