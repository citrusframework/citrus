/*
 * Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.endpoint;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusEndpointProperty;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.citrusframework.util.ReflectionHelper;
import org.citrusframework.util.TypeConversionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endpoint builder interface. All endpoint builder implementations do implement this interface
 * in order to build endpoints using a fluent Java API.
 *
 * @author Christoph Deppisch
 * @since 2.5
 */
public interface EndpointBuilder<T extends Endpoint> {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(EndpointBuilder.class);

    /** Endpoint builder resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/endpoint/builder";

    /** Default Citrus endpoint builders from classpath resource properties */
    ResourcePathTypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Evaluate if this builder supports the given type.
     * @param endpointType type to check.
     * @return true when the builder is able to build the endpoint type, false otherwise.
     */
    boolean supports(Class<?> endpointType);

    /**
     * Builds the endpoint.
     * @return
     */
    T build();

    /**
     * Builds the endpoint from given endpoint annotations.
     * @param endpointAnnotation
     * @param referenceResolver
     * @return
     */
    default T build(CitrusEndpoint endpointAnnotation, ReferenceResolver referenceResolver) {
        Method nameSetter = ReflectionHelper.findMethod(this.getClass(), "name", String.class);
        if (nameSetter != null) {
            ReflectionHelper.invokeMethod(nameSetter, this, endpointAnnotation.name());
        }

        for (CitrusEndpointProperty endpointProperty : endpointAnnotation.properties()) {
            Method propertyMethod = ReflectionHelper.findMethod(this.getClass(), endpointProperty.name(), endpointProperty.type());
            if (propertyMethod != null) {
                if (!endpointProperty.type().equals(String.class)
                        && referenceResolver.isResolvable(endpointProperty.value())) {
                    ReflectionHelper.invokeMethod(propertyMethod, this, referenceResolver.resolve(endpointProperty.value(), endpointProperty.type()));
                } else {
                    ReflectionHelper.invokeMethod(propertyMethod, this, TypeConversionUtils.convertStringToType(endpointProperty.value(), endpointProperty.type()));
                }
            }
        }

        return build();
    }

    /**
     * Builds the endpoint from given endpoint properties.
     * @param endpointProperties
     * @param referenceResolver
     * @return
     */
    default T build(Properties endpointProperties, ReferenceResolver referenceResolver) {
        for (Map.Entry<Object, Object> endpointProperty : endpointProperties.entrySet()) {
            Method propertyMethod = ReflectionHelper.findMethod(this.getClass(), endpointProperty.getKey().toString(), endpointProperty.getValue().getClass());
            if (propertyMethod != null) {
                ReflectionHelper.invokeMethod(propertyMethod, this, endpointProperty.getValue());
            }
        }

        return build();
    }

    /**
     * Resolves all available endpoint builders from resource path lookup. Scans classpath for endpoint builder meta information
     * and instantiates those builders.
     * @return
     */
    static Map<String, EndpointBuilder<?>> lookup() {
        Map<String, EndpointBuilder<?>> builders = new HashMap<>(TYPE_RESOLVER.resolveAll("", TypeResolver.TYPE_PROPERTY_WILDCARD));

        if (logger.isDebugEnabled()) {
            builders.forEach((k, v) -> logger.debug(String.format("Found endpoint builder '%s' as %s", k, v.getClass())));
        }
        return builders;
    }

    /**
     * Resolves endpoint builder from resource path lookup with given resource name. Scans classpath for endpoint builder meta information
     * with given name and returns instance of the builder. Returns optional instead of throwing exception when no endpoint builder
     * could be found.
     *
     * Given builder name is a combination of resource file name and type property separated by '.' character.
     * @param builder
     * @return
     */
    static Optional<EndpointBuilder<?>> lookup(String builder) {
        try {
            EndpointBuilder<?> instance;
            if (builder.contains(".")) {
                int separatorIndex = builder.lastIndexOf('.');
                instance = TYPE_RESOLVER.resolve(builder.substring(0, separatorIndex), builder.substring(separatorIndex + 1));
            } else {
                instance = TYPE_RESOLVER.resolve(builder);
            }

            return Optional.of(instance);
        } catch (CitrusRuntimeException e) {
            logger.warn(String.format("Failed to resolve endpoint builder from resource '%s/%s'", RESOURCE_PATH, builder));
        }

        return Optional.empty();
    }
}
