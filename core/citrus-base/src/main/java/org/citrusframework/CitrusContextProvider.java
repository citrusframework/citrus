/*
 * Copyright the original author or authors.
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

package org.citrusframework;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

@FunctionalInterface
public interface CitrusContextProvider {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(CitrusContextProvider.class);

    /** Endpoint parser resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/context/provider";

    /** Default Citrus context provider from classpath resource properties */
    ResourcePathTypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    String SPRING = "spring";

    /**
     * Create Citrus context with this provider.
     * @return
     */
    CitrusContext create();

    /**
     * Resolves context provider from resource path lookup. Scans classpath for provider meta information
     * and instantiates first found or default fallback provider.
     * @return
     */
    static CitrusContextProvider lookup() {
        Map<String, CitrusContextProvider> provider =
                TYPE_RESOLVER.resolveAll("", TypeResolver.TYPE_PROPERTY_WILDCARD);

        if (provider.isEmpty()) {
            logger.debug("Using default Citrus context provider");
            return new DefaultCitrusContextProvider();
        }

        if (provider.size() > 1) {
            logger.warn("Found {} Citrus context provider implementations. Please choose one of them.", provider.size());
        }

        if (logger.isDebugEnabled()) {
            provider.forEach((k, v) -> logger.debug("Found Citrus context provider '{}' as {}", k, v.getClass()));
        }

        CitrusContextProvider contextProvider = provider.values().iterator().next();
        logger.debug("Using Citrus context provider '{}' as {}", provider.keySet().iterator().next(), contextProvider);
        return contextProvider;
    }

    /**
     * Resolves context provider from resource path lookup with given resource name. Scans classpath for provider meta information
     * with given name and returns the instance. Returns optional instead of throwing exception when no context provider
     * could be found.
     *
     * @param name
     * @return
     */
    static Optional<CitrusContextProvider> lookup(String name) {
        try {
            CitrusContextProvider instance = TYPE_RESOLVER.resolve(name);
            return Optional.of(instance);
        } catch (CitrusRuntimeException e) {
            logger.warn("Failed to resolve Citrus context provider from resource '{}/{}'", RESOURCE_PATH, name);
        }

        return Optional.empty();
    }
}
