/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.camel.endpoint;

import org.citrusframework.endpoint.builder.AsyncSyncEndpointBuilder;

/**
 * @author Christoph Deppisch
 */
public final class CamelEndpoints extends AsyncSyncEndpointBuilder<CamelEndpointBuilder, CamelSyncEndpointBuilder> {
    /**
     * Private constructor setting the sync and async builder implementation.
     */
    private CamelEndpoints() {
        super(new CamelEndpointBuilder(), new CamelSyncEndpointBuilder());
    }

    /**
     * Static entry method for Camel endpoint builders.
     * @return
     */
    public static CamelEndpoints camel() {
        return new CamelEndpoints();
    }

    /**
     * Gets the endpoint builder using inOnly pattern.
     * @return
     */
    public CamelEndpointBuilder inOnly() {
        return asynchronous();
    }

    /**
     * Gets the endpoint builder using inOut pattern.
     * @return
     */
    public CamelSyncEndpointBuilder inOut() {
        return synchronous();
    }
}
