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

package com.consol.citrus.groovy.dsl.configuration.endpoints;

import com.consol.citrus.Citrus;
import com.consol.citrus.common.InitializingPhase;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointBuilder;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Wrapper used as a helper to lookup client/server or asynchronous/synchronous
 * endpoint builder with resource path lookup.
 */
public class EndpointBuilderWrapper {

    private final Citrus citrus;
    private final String type;
    private final String endpointName;

    public EndpointBuilderWrapper(Citrus citrus, String type, String endpointName) {
        this.citrus = citrus;
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
        EndpointBuilder<?> builder = EndpointBuilder.lookup(name).orElseThrow(() ->
                new CitrusRuntimeException(String.format("Failed to resolve endpoint for type %s", name)));

        Endpoint endpoint = builder.build();
        endpoint.setName(endpointName);
        if (endpoint instanceof InitializingPhase) {
            ((InitializingPhase) endpoint).initialize();
        }
        citrus.getCitrusContext().bind(endpointName, endpoint);

        return builder;
    }
}
