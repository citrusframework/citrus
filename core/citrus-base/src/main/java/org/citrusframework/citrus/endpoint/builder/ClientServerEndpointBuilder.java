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

package org.citrusframework.citrus.endpoint.builder;

import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.endpoint.EndpointBuilder;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class ClientServerEndpointBuilder<C extends EndpointBuilder<? extends Endpoint>, S extends EndpointBuilder<? extends Endpoint>> {

    private final C clientBuilder;
    private final S serverBuilder;

    /**
     * Default constructor setting the client and server builder implementation.
     * @param clientBuilder
     * @param serverBuilder
     */
    public ClientServerEndpointBuilder(C clientBuilder, S serverBuilder) {
        this.clientBuilder = clientBuilder;
        this.serverBuilder = serverBuilder;
    }

    /**
     * Gets the client builder.
     * @return
     */
    public C client() {
        return clientBuilder;
    }

    /**
     * Gets the client builder.
     * @return
     */
    public S server() {
        return serverBuilder;
    }
}
