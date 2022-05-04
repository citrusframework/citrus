/*
 * Copyright 2022 the original author or authors.
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

import java.util.function.Supplier;

import com.consol.citrus.Citrus;
import com.consol.citrus.common.InitializingPhase;
import com.consol.citrus.endpoint.Endpoint;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;

/**
 * @author Christoph Deppisch
 */
public class EndpointsConfiguration extends GroovyObjectSupport {

    private final Citrus citrus;

    public EndpointsConfiguration(Citrus citrus) {
        this.citrus = citrus;
    }

    public Endpoint endpoint(String type, Closure<?> callable) {
        Supplier<Endpoint> endpointSupplier = new EndpointConfiguration(type);
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.setDelegate(endpointSupplier);
        callable.call();

        Endpoint endpoint = endpointSupplier.get();
        if (endpoint instanceof InitializingPhase) {
            ((InitializingPhase) endpoint).initialize();
        }
        citrus.getCitrusContext().bind(endpoint.getName(), endpoint);

        return endpoint;
    }

    public Endpoint endpoint(String type, String endpointName, Closure<?> callable) {
        Supplier<Endpoint> endpointSupplier = new EndpointConfiguration(type);
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.setDelegate(endpointSupplier);
        callable.call();

        Endpoint endpoint = endpointSupplier.get();
        endpoint.setName(endpointName);
        if (endpoint instanceof InitializingPhase) {
            ((InitializingPhase) endpoint).initialize();
        }
        citrus.getCitrusContext().bind(endpointName, endpoint);

        return endpoint;
    }

    public Object methodMissing(String name, Object argLine) {
        if (argLine == null) {
            throw new MissingMethodException(name, EndpointsConfiguration.class, null);
        }

        Object[] args = (Object[]) argLine;
        if (args.length > 1) {
            String endpointName = args[0].toString();
            Object closure = args[1];

            if (closure instanceof Closure) {
                return endpoint(name, endpointName, (Closure<?>) closure);
            }
        } else if (args.length == 1) {
            Object closure = args[0];

            if (closure instanceof Closure) {
                return endpoint(name, (Closure<?>) closure);
            }
        }

        throw new MissingMethodException(name, EndpointsConfiguration.class, args);
    }
}
