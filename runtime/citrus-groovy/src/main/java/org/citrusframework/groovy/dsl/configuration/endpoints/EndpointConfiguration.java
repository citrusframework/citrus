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

import java.util.function.Supplier;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointBuilder;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;

/**
 * @author Christoph Deppisch
 */
public class EndpointConfiguration extends GroovyObjectSupport implements Supplier<Endpoint> {

    private final String type;
    private EndpointBuilderConfiguration<?> delegate;

    public EndpointConfiguration(String type) {
        this.type = type;
    }

    private void delegateTo(EndpointBuilder<?> builder, Closure<?> callable) {
        this.delegate = new EndpointBuilderConfiguration<>(builder);
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.setDelegate(delegate);
        callable.call();
    }

    public Object methodMissing(String name, Object argLine) {
        if (argLine == null) {
            throw new MissingMethodException(name, EndpointConfiguration.class, null);
        }

        Object[] args = (Object[]) argLine;
        EndpointBuilder<?> builder = EndpointBuilderHelper.find(type + "." + EndpointBuilderHelper.sanitizeEndpointBuilderName(name));
        Object closure = null;
        if (args.length > 1) {
            String endpointName = args[0].toString();
            if (builder instanceof AbstractEndpointBuilder) {
                ((AbstractEndpointBuilder<?>) builder).name(endpointName);
            }
            closure = args[1];
        } else if (args.length == 1) {
            closure = args[0];
        }

        if (closure instanceof Closure) {
            delegateTo(builder, (Closure<?>) closure);
            return null;
        } else {
            this.delegate = new EndpointBuilderConfiguration<>(builder);
            return builder;
        }
    }

    @Override
    public Endpoint get() {
        return delegate.get();
    }
}
