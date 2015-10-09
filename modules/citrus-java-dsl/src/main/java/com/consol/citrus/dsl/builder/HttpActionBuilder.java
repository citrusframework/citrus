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

import com.consol.citrus.TestAction;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.server.HttpServer;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * Action executes docker commands.
 * 
 * @author Christoph Deppisch
 * @since 2.4
 */
public class HttpActionBuilder extends AbstractTestActionBuilder<DelegatingTestAction<TestAction>> {

	/** Spring application context */
	private ApplicationContext applicationContext;

	/**
	 * Default constructor.
	 */
	public HttpActionBuilder() {
		super(new DelegatingTestAction<>());
	}

	/**
	 * Initiate http client action.
	 */
	public HttpClientActionBuilder client(HttpClient httpClient) {
		HttpClientActionBuilder clientAction = new HttpClientActionBuilder(action, httpClient)
				.withApplicationContext(applicationContext);
		return clientAction;
	}

	/**
	 * Initiate http client action.
	 */
	public HttpClientActionBuilder client(String httpClient) {
		Assert.notNull(applicationContext, "Citrus application context is not initialized!");
		HttpClientActionBuilder clientAction = new HttpClientActionBuilder(action, applicationContext.getBean(httpClient, Endpoint.class))
				.withApplicationContext(applicationContext);
		return clientAction;
	}

	/**
	 * Initiate http server action.
	 */
	public HttpServerActionBuilder server(HttpServer httpServer) {
		HttpServerActionBuilder serverAction = new HttpServerActionBuilder(action, httpServer)
				.withApplicationContext(applicationContext);
		return serverAction;
	}

	/**
	 * Initiate http server action.
	 */
	public HttpServerActionBuilder server(String httpServer) {
		Assert.notNull(applicationContext, "Citrus application context is not initialized!");
		HttpServerActionBuilder serverAction = new HttpServerActionBuilder(action, applicationContext.getBean(httpServer, Endpoint.class))
				.withApplicationContext(applicationContext);
		return serverAction;
	}

	/**
	 * Sets the Spring bean application context.
	 * @param applicationContext
	 */
	public HttpActionBuilder withApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		return this;
	}
}
