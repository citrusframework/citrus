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

package org.citrusframework.endpoint.builder;

import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointBuilder;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class AsyncSyncEndpointBuilder<A extends EndpointBuilder<? extends Endpoint>, S extends EndpointBuilder<? extends Endpoint>> {

    private final A asyncEndpointBuilder;
    private final S syncEndpointBuilder;

    /**
     * Default constructor setting the sync and async builder implementation.
     * @param asyncEndpointBuilder
     * @param syncEndpointBuilder
     */
    public AsyncSyncEndpointBuilder(A asyncEndpointBuilder, S syncEndpointBuilder) {
        this.asyncEndpointBuilder = asyncEndpointBuilder;
        this.syncEndpointBuilder = syncEndpointBuilder;
    }

    /**
     * Gets the async endpoint builder.
     * @return
     */
    public A asynchronous() {
        return asyncEndpointBuilder;
    }

    /**
     * Gets the sync endpoint builder.
     * @return
     */
    public S synchronous() {
        return syncEndpointBuilder;
    }
}
