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

package com.consol.citrus.ws.actions;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.server.WebServiceServer;
import org.springframework.util.Assert;

/**
 * Action executes soap client and server operations.
 *
 * @author Christoph Deppisch
 * @since 2.6
 */
public class SoapActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction> {

	/** Bean reference resolver */
	private ReferenceResolver referenceResolver;

	private TestActionBuilder<?> delegate;

	/**
	 * Initiate soap client action.
	 */
	public SoapClientActionBuilder client(WebServiceClient soapClient) {
		SoapClientActionBuilder clientActionBuilder = new SoapClientActionBuilder(soapClient)
				.withReferenceResolver(referenceResolver);
		this.delegate = clientActionBuilder;
		return clientActionBuilder;
	}

	/**
	 * Initiate soap client action.
	 */
	public SoapClientActionBuilder client(String soapClient) {
		SoapClientActionBuilder clientActionBuilder = new SoapClientActionBuilder(soapClient)
				.withReferenceResolver(referenceResolver);
		this.delegate = clientActionBuilder;
		return clientActionBuilder;
	}

	/**
	 * Initiate soap server action.
	 */
	public SoapServerActionBuilder server(WebServiceServer soapServer) {
		SoapServerActionBuilder serverActionBuilder = new SoapServerActionBuilder(soapServer)
				.withReferenceResolver(referenceResolver);
		this.delegate = serverActionBuilder;
		return serverActionBuilder;
	}

	/**
	 * Initiate soap server action.
	 */
	public SoapServerActionBuilder server(String soapServer) {
		Assert.notNull(referenceResolver, "Citrus bean reference resolver is not initialized!");
		SoapServerActionBuilder serverActionBuilder = new SoapServerActionBuilder(referenceResolver.resolve(soapServer, Endpoint.class))
				.withReferenceResolver(referenceResolver);
		this.delegate = serverActionBuilder;
		return serverActionBuilder;
	}

	/**
	 * Sets the bean reference resolver.
	 * @param referenceResolver
	 */
	public SoapActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
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
