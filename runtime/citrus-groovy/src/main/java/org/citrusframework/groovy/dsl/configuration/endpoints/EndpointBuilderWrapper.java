/*
 * Copyright 2023 the original author or authors.
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

package org.citrusframework.groovy.dsl.configuration.endpoints;

import java.util.function.Supplier;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Wrapper used as a helper to lookup client/server or asynchronous/synchronous
 * endpoint builder with resource path lookup.
 */
public class EndpointBuilderWrapper implements Supplier<Endpoint> {

    private EndpointBuilder<?> builder;
    private final String type;
    private final String endpointName;

    public EndpointBuilderWrapper(String type, String endpointName) {
        this.type = type;
        this.endpointName = endpointName;
    }

    public EndpointBuilder<?> client() {
        return resolve(type + ".client");
    }

    public EndpointBuilder<?> server() {
        return resolve(type + ".server");
    }

    public EndpointBuilder<?> synchronous() {
        return resolve(type + ".sync");
    }

    public EndpointBuilder<?> asynchronous() {
        return resolve(type + ".async");
    }

    private EndpointBuilder<?> resolve(String name) {
        builder = EndpointBuilder.lookup(name).orElseThrow(() ->
                new CitrusRuntimeException(String.format("Failed to resolve endpoint for type %s", name)));

        if (builder instanceof AbstractEndpointBuilder) {
            ((AbstractEndpointBuilder<?>) builder).name(endpointName);
        }

        return builder;
    }

    @Override
    public Endpoint get() {
        if (builder != null) {
            return builder.build();
        }

        return null;
    }
}
