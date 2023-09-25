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

package org.citrusframework.context.resolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Type resolver able to adapt an alias type to a given source type. Used in {@link org.citrusframework.spi.ReferenceResolver} to
 * auto resolve types that can act as an alias interchangeably to a given type.
 *
 * @author Christoph Deppisch
 */
public interface TypeAliasResolver<S, A> {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(TypeAliasResolver.class);

    /** Type alias resolver resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/context/resolver";

    /** Type resolver to find custom type alias resolvers on classpath via resource path lookup */
    TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    Map<String, TypeAliasResolver<?, ?>> resolvers = new HashMap<>();

    /**
     * Resolves all available type alias resolvers from resource path lookup. Scans classpath for type alias resolver meta information
     * and instantiates those resolvers.
     * @return
     */
    static Map<String, TypeAliasResolver<?, ?>> lookup() {
        if (resolvers.isEmpty()) {
            resolvers.putAll(TYPE_RESOLVER.resolveAll("", TypeResolver.DEFAULT_TYPE_PROPERTY, "name"));

            if (logger.isDebugEnabled()) {
                resolvers.forEach((k, v) -> logger.debug(String.format("Found type alias resolver '%s' as %s", k, v.getClass())));
            }
        }
        return resolvers;
    }

    /**
     * Resolves type alias resolver from resource path lookup with given resource name. Scans classpath for type alias resolver meta information
     * with given name and returns instance of type alias resolver. Returns optional instead of throwing exception when no type alias resolver
     * could be found.
     * @param resolver
     * @return
     */
    static Optional<TypeAliasResolver<?, ?>> lookup(String resolver) {
        try {
            return Optional.of(TYPE_RESOLVER.resolve(resolver));
        } catch (CitrusRuntimeException e) {
            logger.warn(String.format("Failed to resolve type alias resolver from resource '%s/%s'", RESOURCE_PATH, resolver));
        }

        return Optional.empty();
    }

    boolean isAliasFor(Class<?> sourceType);

    S adapt(Object alias);

    Class<A> getAliasType();

}
