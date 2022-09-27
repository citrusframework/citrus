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

package com.consol.citrus.endpoint;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusEndpointProperty;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ResourcePathTypeResolver;
import com.consol.citrus.spi.TypeResolver;
import com.consol.citrus.util.TypeConversionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

/**
 * Endpoint builder interface. All endpoint builder implementations do implement this interface
 * in order to build endpoints using a fluent Java API.
 *
 * @author Christoph Deppisch
 * @since 2.5
 */
public interface EndpointBuilder<T extends Endpoint> {

    /** Logger */
    Logger LOG = LoggerFactory.getLogger(EndpointBuilder.class);

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
        Method nameSetter = ReflectionUtils.findMethod(this.getClass(), "name", String.class);
        if (nameSetter != null) {
            ReflectionUtils.invokeMethod(nameSetter, this, endpointAnnotation.name());
        }

        for (CitrusEndpointProperty endpointProperty : endpointAnnotation.properties()) {
            Method propertyMethod = ReflectionUtils.findMethod(this.getClass(), endpointProperty.name(), endpointProperty.type());
            if (propertyMethod != null) {
                if (!endpointProperty.type().equals(String.class)
                        && referenceResolver.isResolvable(endpointProperty.value())) {
                    ReflectionUtils.invokeMethod(propertyMethod, this, referenceResolver.resolve(endpointProperty.value(), endpointProperty.type()));
                } else {
                    ReflectionUtils.invokeMethod(propertyMethod, this, TypeConversionUtils.convertStringToType(endpointProperty.value(), endpointProperty.type()));
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
            Method propertyMethod = ReflectionUtils.findMethod(this.getClass(), endpointProperty.getKey().toString(), endpointProperty.getValue().getClass());
            if (propertyMethod != null) {
                ReflectionUtils.invokeMethod(propertyMethod, this, endpointProperty.getValue());
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

        if (LOG.isDebugEnabled()) {
            builders.forEach((k, v) -> LOG.debug(String.format("Found endpoint builder '%s' as %s", k, v.getClass())));
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
            LOG.warn(String.format("Failed to resolve endpoint builder from resource '%s/%s'", RESOURCE_PATH, builder));
        }

        return Optional.empty();
    }
}
