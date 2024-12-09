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

package org.citrusframework.openapi;

import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Interface for processing OpenAPI specifications.
 * <p>
 * This interface is designed to be implemented by custom processors that handle OpenAPI specifications.
 * Implementations of this interface are discovered by the standard citrus SPI mechanism.
 */
public interface OpenApiSpecificationProcessor {

    /**
     * Logger
     */
    Logger logger = LoggerFactory.getLogger(OpenApiSpecificationProcessor.class);

    /**
     * OpenAPI processors resource lookup path
     */
    String RESOURCE_PATH = "META-INF/citrus/openapi/processor";

    /**
     * Type resolver to find OpenAPI processors on classpath via resource path lookup
     */
    TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Resolves all available processors from resource path lookup. Scans classpath for processors meta information
     * and instantiates those processors.
     */
    static Map<String, OpenApiSpecificationProcessor> lookup() {
        Map<String, OpenApiSpecificationProcessor> processors = TYPE_RESOLVER.resolveAll("", TypeResolver.DEFAULT_TYPE_PROPERTY, "name");

        if (logger.isDebugEnabled()) {
            processors.forEach((k, v) -> logger.debug("Found openapi specification processor '{}' as {}", k, v.getClass()));
        }

        return processors;
    }

    void process(OpenApiSpecification openApiSpecification);
}
