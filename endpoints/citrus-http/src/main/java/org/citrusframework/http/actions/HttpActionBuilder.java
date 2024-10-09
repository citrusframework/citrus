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

package org.citrusframework.http.actions;

import org.citrusframework.TestAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.ObjectHelper;

/**
 * Action executes http client and server operations.
 *
 * @since 2.4
 */
public class HttpActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<TestAction> {

	/**
	 * Static entrance method for the Http fluent action builder.
	 * @return
	 */
	public static HttpActionBuilder http() {
		return new HttpActionBuilder();
	}

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
	public HttpServerActionBuilder server(Endpoint endpoint) {
		HttpServerActionBuilder serverActionBuilder = new HttpServerActionBuilder(endpoint)
				.withReferenceResolver(referenceResolver);
		this.delegate = serverActionBuilder;
		return serverActionBuilder;
	}

	/**
	 * Initiate http server action.
	 */
	public HttpServerActionBuilder server(String httpServer) {
		HttpServerActionBuilder serverActionBuilder = new HttpServerActionBuilder(httpServer)
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
		ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
		return delegate.build();
	}
}
