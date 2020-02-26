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

package com.consol.citrus.http.actions;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.server.HttpServer;
import org.springframework.util.Assert;

/**
 * Action executes http client and server operations.
 *
 * @author Christoph Deppisch
 * @since 2.4
 */
public class HttpActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction> {

	/** Bean reference resolver */
	private ReferenceResolver referenceResolver;

	private TestActionBuilder<?> delegate;

	/**
	 * Initiate http client action.
	 */
	public HttpClientActionBuilder client(HttpClient httpClient) {
		HttpClientActionBuilder clientActionBuilder = new HttpClientActionBuilder(httpClient)
				.withReferenceResolver(referenceResolver);
		this.delegate = clientActionBuilder;
		return clientActionBuilder;
	}

	/**
	 * Initiate http client action.
	 */
	public HttpClientActionBuilder client(String httpClient) {
		HttpClientActionBuilder clientActionBuilder = new HttpClientActionBuilder(httpClient)
				.withReferenceResolver(referenceResolver);
		this.delegate = clientActionBuilder;
		return clientActionBuilder;
	}

	/**
	 * Initiate http server action.
	 */
	public HttpServerActionBuilder server(HttpServer httpServer) {
		HttpServerActionBuilder serverActionBuilder = new HttpServerActionBuilder(httpServer)
				.withReferenceResolver(referenceResolver);
		this.delegate = serverActionBuilder;
		return serverActionBuilder;
	}

	/**
	 * Initiate http server action.
	 */
	public HttpServerActionBuilder server(String httpServer) {
		Assert.notNull(referenceResolver, "Citrus bean reference resolver is not initialized!");
		HttpServerActionBuilder serverActionBuilder = new HttpServerActionBuilder(referenceResolver.resolve(httpServer, Endpoint.class))
				.withReferenceResolver(referenceResolver);
		this.delegate = serverActionBuilder;
		return serverActionBuilder;
	}

	/**
	 * Sets the bean reference resolver.
	 * @param referenceResolver
	 */
	public HttpActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
		this.referenceResolver = referenceResolver;
		return this;
	}

	@Override
	public TestAction build() {
		Assert.notNull(delegate, "Missing delegate action to build");
		return delegate.build();
	}

	@Override
	public TestActionBuilder<?> getDelegate() {
		return delegate;
	}
}
