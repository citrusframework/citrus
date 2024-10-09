/*
 * Copyright the original author or authors.
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

package org.citrusframework.ws.actions;

import org.citrusframework.TestAction;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.ObjectHelper;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.server.WebServiceServer;

/**
 * Action executes soap client and server operations.
 *
 * @since 2.6
 */
public class SoapActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<TestAction> {

	/**
	 * Static entrance method for the SOAP fluent action builder.
	 * @return
	 */
	public static SoapActionBuilder soap() {
		return new SoapActionBuilder();
	}

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
		SoapServerActionBuilder serverActionBuilder = new SoapServerActionBuilder(soapServer)
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
		ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
		return delegate.build();
	}
}
