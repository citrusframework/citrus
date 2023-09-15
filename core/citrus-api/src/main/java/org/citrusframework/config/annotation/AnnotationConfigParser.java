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

package org.citrusframework.config.annotation;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public interface AnnotationConfigParser<A extends Annotation, T extends Endpoint> {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(AnnotationConfigParser.class);

    /** Endpoint parser resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/endpoint/parser";

    /** Default Citrus annotation config parsers from classpath resource properties */
    ResourcePathTypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    Map<String, AnnotationConfigParser> parsers = new HashMap<>();

    /**
     * Parse given annotation to a proper endpoint instance.
     * @param annotation
     * @param referenceResolver
     * @return
     */
    T parse(A annotation, ReferenceResolver referenceResolver);

    /**
     * Resolves all available annotation config parsers from resource path lookup. Scans classpath for annotation config parser meta information
     * and instantiates those parsers.
     * @return
     */
    static Map<String, AnnotationConfigParser> lookup() {
        if (parsers.isEmpty()) {
            parsers.putAll(TYPE_RESOLVER.resolveAll("", TypeResolver.TYPE_PROPERTY_WILDCARD));

            if (logger.isDebugEnabled()) {
                parsers.forEach((k, v) -> logger.debug(String.format("Found annotation config parser '%s' as %s", k, v.getClass())));
            }
        }
        return parsers;
    }

    /**
     * Resolves annotation config parser from resource path lookup with given resource name. Scans classpath for annotation config parser meta information
     * with given name and returns instance of the parser. Returns optional instead of throwing exception when no annotation config parser
     * could be found.
     *
     * Given parser name is a combination of resource file name and type property separated by '.' character.
     * @param parser
     * @return
     */
    static Optional<AnnotationConfigParser> lookup(String parser) {
        try {
            AnnotationConfigParser instance;
            if (parser.contains(".")) {
                int separatorIndex = parser.lastIndexOf('.');
                instance = TYPE_RESOLVER.resolve(parser.substring(0, separatorIndex), parser.substring(separatorIndex + 1));
            } else {
                instance = TYPE_RESOLVER.resolve(parser);
            }

            return Optional.of(instance);
        } catch (CitrusRuntimeException e) {
            logger.warn(String.format("Failed to resolve annotation config parser from resource '%s/%s'", RESOURCE_PATH, parser));
        }

        return Optional.empty();
    }
}
