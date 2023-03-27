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

package org.citrusframework.citrus.dsl.endpoint.vertx;

import org.citrusframework.citrus.vertx.endpoint.VertxEndpointBuilder;
import org.citrusframework.citrus.vertx.endpoint.VertxSyncEndpointBuilder;
import org.citrusframework.citrus.vertx.endpoint.builder.VertxEndpoints;

/**
 * @author Christoph Deppisch
 */
public class VertxEndpointCatalog {

    /**
     * Private constructor setting the sync and async builder implementation.
     */
    private VertxEndpointCatalog() {
        // prevent direct instantiation
    }

    public static VertxEndpointCatalog vertx() {
        return new VertxEndpointCatalog();
    }

    /**
     * Gets the async endpoint builder.
     * @return
     */
    public VertxEndpointBuilder asynchronous() {
        return VertxEndpoints.vertx().asynchronous();
    }

    /**
     * Gets the sync endpoint builder.
     * @return
     */
    public VertxSyncEndpointBuilder synchronous() {
        return VertxEndpoints.vertx().synchronous();
    }
}
