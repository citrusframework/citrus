/*
 * Copyright 2006-2016 the original author or authors.
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

import com.consol.citrus.TestAction;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.server.WebServiceServer;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * Action executes soap client and server operations.
 * 
 * @author Christoph Deppisch
 * @since 2.6
 */
public class SoapActionBuilder extends AbstractTestActionBuilder<DelegatingTestAction<TestAction>> {

	/** Spring application context */
	private ApplicationContext applicationContext;

	/**
	 * Default constructor.
	 */
	public SoapActionBuilder() {
		super(new DelegatingTestAction<>());
	}

	/**
	 * Initiate soap client action.
	 */
	public SoapClientActionBuilder client(WebServiceClient soapClient) {
		SoapClientActionBuilder clientAction = new SoapClientActionBuilder(action, soapClient)
				.withApplicationContext(applicationContext);
		return clientAction;
	}

	/**
	 * Initiate soap client action.
	 */
	public SoapClientActionBuilder client(String soapClient) {
		SoapClientActionBuilder clientAction = new SoapClientActionBuilder(action, soapClient)
				.withApplicationContext(applicationContext);
		return clientAction;
	}

	/**
	 * Initiate soap server action.
	 */
	public SoapServerActionBuilder server(WebServiceServer soapServer) {
		SoapServerActionBuilder serverAction = new SoapServerActionBuilder(action, soapServer)
				.withApplicationContext(applicationContext);
		return serverAction;
	}

	/**
	 * Initiate soap server action.
	 */
	public SoapServerActionBuilder server(String soapServer) {
		Assert.notNull(applicationContext, "Citrus application context is not initialized!");
		SoapServerActionBuilder serverAction = new SoapServerActionBuilder(action, applicationContext.getBean(soapServer, Endpoint.class))
				.withApplicationContext(applicationContext);
		return serverAction;
	}

	/**
	 * Sets the Spring bean application context.
	 * @param applicationContext
	 */
	public SoapActionBuilder withApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		return this;
	}
}
