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

package org.citrusframework.dsl.endpoint.ws;

import org.citrusframework.ws.client.WebServiceClientBuilder;
import org.citrusframework.ws.endpoint.builder.WebServiceEndpoints;
import org.citrusframework.ws.server.WebServiceServerBuilder;

/**
 * @author Christoph Deppisch
 */
public class WebServiceEndpointCatalog {

    /**
     * Private constructor setting the client and server builder implementation.
     */
    private WebServiceEndpointCatalog() {
        // prevent direct instantiation
    }

    public static WebServiceEndpointCatalog soap() {
        return new WebServiceEndpointCatalog();
    }

    /**
     * Gets the client builder.
     * @return
     */
    public WebServiceClientBuilder client() {
        return WebServiceEndpoints.soap().client();
    }

    /**
     * Gets the client builder.
     * @return
     */
    public WebServiceServerBuilder server() {
        return WebServiceEndpoints.soap().server();
    }
}
