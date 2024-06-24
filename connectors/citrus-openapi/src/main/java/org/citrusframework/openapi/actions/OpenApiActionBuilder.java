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

package org.citrusframework.openapi.actions;

import org.citrusframework.TestAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.ObjectHelper;

import java.net.URL;

/**
 * Action executes client and server operations using given OpenApi specification.
 * Action creates proper request and response data from given specification rules.
 *
 * @since 4.1
 */
public class OpenApiActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<TestAction> {

	private OpenApiSpecification specification;

	public OpenApiActionBuilder() {
	}

	public OpenApiActionBuilder(OpenApiSpecification specification) {
		this.specification = specification;
	}

	/**
	 * Static entrance method for the OpenApi fluent action builder.
	 */
	public static OpenApiActionBuilder openapi() {
		return new OpenApiActionBuilder();
	}

	public static OpenApiActionBuilder openapi(OpenApiSpecification specification) {
		return new OpenApiActionBuilder(specification);
	}

	public OpenApiActionBuilder specification(OpenApiSpecification specification) {
		this.specification = specification;
		return this;
	}

	public OpenApiActionBuilder specification(URL specUrl) {
		return specification(OpenApiSpecification.from(specUrl));
	}

	public OpenApiActionBuilder specification(String specUrl) {
		return specification(OpenApiSpecification.from(specUrl));
	}

	public OpenApiClientActionBuilder client() {
		assertSpecification();
		return client(specification.getRequestUrl());
	}

	/**
	 * Initiate http client action.
	 */
	public OpenApiClientActionBuilder client(HttpClient httpClient) {
		assertSpecification();

		if (httpClient.getEndpointConfiguration().getRequestUrl() != null) {
			specification.setRequestUrl(httpClient.getEndpointConfiguration().getRequestUrl());
		}

		OpenApiClientActionBuilder clientActionBuilder = new OpenApiClientActionBuilder(httpClient, specification)
				.withReferenceResolver(referenceResolver);
		this.delegate = clientActionBuilder;
		return clientActionBuilder;
	}

	/**
	 * Initiate http client action.
	 */
	public OpenApiClientActionBuilder client(String httpClient) {
		assertSpecification();

		specification.setHttpClient(httpClient);

		OpenApiClientActionBuilder clientActionBuilder = new OpenApiClientActionBuilder(httpClient, specification)
				.withReferenceResolver(referenceResolver);
		this.delegate = clientActionBuilder;
		return clientActionBuilder;
	}

	/**
	 * Initiate http server action.
	 */
	public OpenApiServerActionBuilder server(Endpoint endpoint) {
		assertSpecification();

		OpenApiServerActionBuilder serverActionBuilder = new OpenApiServerActionBuilder(endpoint, specification)
				.withReferenceResolver(referenceResolver);
		this.delegate = serverActionBuilder;
		return serverActionBuilder;
	}

	private void assertSpecification() {
		if (specification == null) {
			throw new CitrusRuntimeException("Invalid OpenApi specification - please set specification first");
		}
	}

	/**
	 * Initiate http server action.
	 */
	public OpenApiServerActionBuilder server(String httpServer) {
		assertSpecification();

		OpenApiServerActionBuilder serverActionBuilder = new OpenApiServerActionBuilder(httpServer, specification)
				.withReferenceResolver(referenceResolver);
		this.delegate = serverActionBuilder;
		return serverActionBuilder;
	}

	/**
	 * Sets the bean reference resolver.
	 */
	public OpenApiActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
		this.referenceResolver = referenceResolver;
		return this;
	}

	@Override
	public TestAction build() {
		ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
		return delegate.build();
	}
}
