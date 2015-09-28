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

import com.consol.citrus.actions.PurgeEndpointAction;
import com.consol.citrus.endpoint.Endpoint;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 * Action purges all messages from a message endpoint instance. Message endpoint must be
 * of type {@link com.consol.citrus.endpoint.Endpoint}. Action receives a
 * list of endpoint objects or a list of endpoint names that are resolved dynamically at runtime.
 * 
 * @author Christoph Deppisch
 * @since 2.4
 */
public class PurgeEndpointsBuilder extends AbstractTestActionBuilder<PurgeEndpointAction> {

    /**
     * Default constructor using test action and application context
     * @param action
     */
	public PurgeEndpointsBuilder(PurgeEndpointAction action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public PurgeEndpointsBuilder() {
		super(new PurgeEndpointAction());
	}

	/**
     * Sets the messageSelector.
     * @param messageSelector the messageSelector to set
     */
	public PurgeEndpointsBuilder selector(String messageSelector) {
		action.setMessageSelectorString(messageSelector);
		return this;
	}

	/**
	 * Sets the messageSelector.
	 * @param messageSelector the messageSelector to set
	 */
	public PurgeEndpointsBuilder selector(Map<String, Object> messageSelector) {
		action.setMessageSelector(messageSelector);
		return this;
	}

	/**
     * Adds list of endpoint names to purge in this action.
     * @param endpointNames the endpointNames to set
     */
	public PurgeEndpointsBuilder endpointNames(List<String> endpointNames) {
		action.getEndpointNames().addAll(endpointNames);
		return this;
	}
	
	/**
	 * Adds several endpoint names to the list of endpoints to purge in this action.
	 * @param endpointNames
	 * @return
	 */
	public PurgeEndpointsBuilder endpointNames(String... endpointNames) {
		return endpointNames(Arrays.asList(endpointNames));
	}
	
	/**
     * Adds a endpoint name to the list of endpoints to purge in this action.
     * @param name
     * @return
     */
    public PurgeEndpointsBuilder endpoint(String name) {
        action.getEndpointNames().add(name);
        return this;
    }
	
	/**
     * Adds list of endpoints to purge in this action.
     * @param endpoints the endpoints to set
     */
	public PurgeEndpointsBuilder endpoints(List<Endpoint> endpoints) {
		action.getEndpoints().addAll(endpoints);
		return this;
	}
	
	/**
	 * Sets several endpoints to purge in this action.
	 * @param endpoints
	 * @return
	 */
	public PurgeEndpointsBuilder endpoints(Endpoint... endpoints) {
		return endpoints(Arrays.asList(endpoints));
	}
	
	/**
     * Adds a endpoint to the list of endpoints to purge in this action.
     * @param endpoint
     * @return
     */
    public PurgeEndpointsBuilder endpoint(Endpoint endpoint) {
        action.getEndpoints().add(endpoint);
        return this;
    }

	/**
	 * Receive timeout for reading message from a destination.
	 * @param receiveTimeout the receiveTimeout to set
	 */
	public PurgeEndpointsBuilder timeout(long receiveTimeout) {
		action.setReceiveTimeout(receiveTimeout);
		return this;
	}

	/**
	 * Sets the sleepTime.
	 * @param millis the sleepTime to set
	 */
	public PurgeEndpointsBuilder sleep(long millis) {
		action.setSleepTime(millis);
		return this;
	}

	/**
	 * Sets the Spring bean factory for using endpoint names.
	 * @param applicationContext
	 */
	public PurgeEndpointsBuilder withApplicationContext(ApplicationContext applicationContext) {
		action.setBeanFactory(applicationContext);
		return this;
	}

}
