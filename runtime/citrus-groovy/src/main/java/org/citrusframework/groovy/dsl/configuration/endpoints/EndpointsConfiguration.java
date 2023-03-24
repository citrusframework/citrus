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

package org.citrusframework.groovy.dsl.configuration.endpoints;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.citrusframework.endpoint.Endpoint;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.GroovyObjectSupport;

/**
 * @author Christoph Deppisch
 */
public class EndpointsConfiguration extends GroovyObjectSupport {

    private final Set<Supplier<Endpoint>> endpoints = new HashSet<>();

    public void endpoints(@DelegatesTo(EndpointsConfiguration.class) Closure<?> callable) {
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.setDelegate(this);
        callable.call();
    }

    public Endpoint endpoint(String type, Closure<?> callable) {
        Supplier<Endpoint> endpointSupplier = new EndpointConfiguration(type);
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.setDelegate(endpointSupplier);
        callable.call();

        endpoints.add(endpointSupplier);
        return endpointSupplier.get();
    }

    public Endpoint endpoint(String type, String endpointName, Closure<?> callable) {
        Supplier<Endpoint> endpointSupplier = new EndpointConfiguration(type);
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.setDelegate(endpointSupplier);
        callable.call();

        endpoints.add(endpointSupplier);
        Endpoint endpoint = endpointSupplier.get();
        endpoint.setName(endpointName);
        return endpoint;
    }

    public Object methodMissing(String name, Object argLine) {
        Object[] args = Optional.ofNullable(argLine).map(Object[].class::cast).orElseGet(() -> new Object[]{});
        if (args.length > 1) {
            String endpointName = args[0].toString();
            Object closure = args[1];

            if (closure instanceof Closure) {
                return endpoint(name, endpointName, (Closure<?>) closure);
            } else {
                EndpointBuilderWrapper wrapper = new EndpointBuilderWrapper(name, endpointName);
                endpoints.add(wrapper);
                return wrapper;
            }
        } else if (args.length == 1) {
            Object closureOrName = args[0];

            if (closureOrName instanceof Closure) {
                return endpoint(name, (Closure<?>) closureOrName);
            } else {
                EndpointBuilderWrapper wrapper = new EndpointBuilderWrapper(name, closureOrName.toString());
                endpoints.add(wrapper);
                return wrapper;
            }
        }

        EndpointBuilderWrapper wrapper = new EndpointBuilderWrapper(name, name);
        endpoints.add(wrapper);
        return wrapper;
    }

    public Set<Endpoint> getEndpoints() {
        return endpoints.stream()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
