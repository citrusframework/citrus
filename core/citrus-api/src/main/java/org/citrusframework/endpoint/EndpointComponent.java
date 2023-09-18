/*
 * Copyright 2006-2014 the original author or authors.
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

import java.util.Map;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endpoint component registers with bean name in Spring application context and is then responsible to create proper endpoints dynamically from
 * endpoint uri values. Creates endpoint instance by parsing the dynamic endpoint uri with special properties and parameters. Creates proper endpoint
 * configuration instance on the fly.
 *
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public interface EndpointComponent {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(EndpointComponent.class);

    String ENDPOINT_NAME = "endpointName";

    /** Endpoint component resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/endpoint/component";

    /** Default Citrus endpoint components from classpath resource properties */
    ResourcePathTypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Creates proper endpoint instance from endpoint uri.
     * @param endpointUri
     * @param context
     * @return
     */
    Endpoint createEndpoint(String endpointUri, TestContext context);

    /**
     * Gets the name of this endpoint component.
     * @return
     */
    String getName();

    /**
     * Construct endpoint name from endpoint uri.
     * @param endpointUri
     * @return
     */
    Map<String, String> getParameters(String endpointUri);

    /**
     * Resolves all available endpoint components from resource path lookup. Scans classpath for endpoint component meta information
     * and instantiates those components.
     * @return
     */
    static Map<String, EndpointComponent> lookup() {
        Map<String, EndpointComponent> components = TYPE_RESOLVER.resolveAll();

        if (logger.isDebugEnabled()) {
            components.forEach((k, v) -> logger.debug(String.format("Found endpoint component '%s' as %s", k, v.getClass())));
        }
        return components;
    }

    /**
     * Resolves endpoint component from resource path lookup with given resource name. Scans classpath for endpoint component meta information
     * with given name and returns instance of the component. Returns optional instead of throwing exception when no endpoint component
     * could be found.
     * @param component
     * @return
     */
    static Optional<EndpointComponent> lookup(String component) {
        try {
            EndpointComponent instance = TYPE_RESOLVER.resolve(component);
            return Optional.of(instance);
        } catch (CitrusRuntimeException e) {
            logger.warn(String.format("Failed to resolve endpoint component from resource '%s/%s'", RESOURCE_PATH, component));
        }

        return Optional.empty();
    }
}
