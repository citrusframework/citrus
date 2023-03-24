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

import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 */
public class EndpointBuilderHelper {

    /**
     * Prevent instantiation of utility class
     */
    private EndpointBuilderHelper() {
        // prevent instantiation
    }

    /**
     * Map synchronous and asynchronous endpoint builder names.
     * @param name
     * @return
     */
    public static String sanitizeEndpointBuilderName(String name) {
        if (name.equals("synchronous")) {
            return "sync";
        }

        if (name.equals("asynchronous")) {
            return "async";
        }

        return name;
    }

    /**
     * Finds endpoint builder with given name via resource path lookup.
     * @param name
     * @return
     */
    public static EndpointBuilder<?> find(String name) {
        try {
            EndpointBuilder<?> builder = EndpointBuilder.lookup().get(name);

            if (builder == null) {
                throw new CitrusRuntimeException(String .format("Unable to find endpoint builder for resource path '%s'", name));
            }

            // Remove when bug in EndpointBuilder.lookup() with cached endpoint builders is fixed
            return builder.getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CitrusRuntimeException("Unable to instantiate endpoint builder", e);
        }
    }
}
