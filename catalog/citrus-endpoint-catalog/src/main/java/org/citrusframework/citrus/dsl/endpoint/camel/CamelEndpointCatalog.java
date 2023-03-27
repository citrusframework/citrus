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

package org.citrusframework.citrus.dsl.endpoint.camel;

import org.citrusframework.citrus.camel.endpoint.CamelEndpointBuilder;
import org.citrusframework.citrus.camel.endpoint.CamelEndpoints;
import org.citrusframework.citrus.camel.endpoint.CamelSyncEndpointBuilder;

/**
 * @author Christoph Deppisch
 */
public class CamelEndpointCatalog {

    /**
     * Private constructor setting the sync and async builder implementation.
     */
    private CamelEndpointCatalog() {
        // prevent direct instantiation
    }

    public static CamelEndpointCatalog camel() {
        return new CamelEndpointCatalog();
    }

    /**
     * Gets the async endpoint builder.
     * @return
     */
    public CamelEndpointBuilder asynchronous() {
        return CamelEndpoints.camel().asynchronous();
    }

    /**
     * Gets the sync endpoint builder.
     * @return
     */
    public CamelSyncEndpointBuilder synchronous() {
        return CamelEndpoints.camel().synchronous();
    }

    /**
     * Gets the inOnly endpoint builder.
     * @return
     */
    public CamelEndpointBuilder inOnly() {
        return CamelEndpoints.camel().inOnly();
    }

    /**
     * Gets the inOut endpoint builder.
     * @return
     */
    public CamelSyncEndpointBuilder inOut() {
        return CamelEndpoints.camel().inOut();
    }
}
