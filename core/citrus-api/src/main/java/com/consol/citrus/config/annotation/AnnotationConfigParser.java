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

package com.consol.citrus.config.annotation;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ResourcePathTypeResolver;
import com.consol.citrus.spi.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public interface AnnotationConfigParser<A extends Annotation, T extends Endpoint> {

    /** Logger */
    Logger LOG = LoggerFactory.getLogger(AnnotationConfigParser.class);

    /** Endpoint parser resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/endpoint/parser";

    /** Default Citrus annotation config parsers from classpath resource properties */
    ResourcePathTypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Resolves all available annotation config parsers from resource path lookup. Scans classpath for annotation config parser meta information
     * and instantiates those parsers.
     * @return
     */
    static Map<String, AnnotationConfigParser> lookup() {
        Map<String, AnnotationConfigParser> parsers =
                TYPE_RESOLVER.resolveAll("", TypeResolver.TYPE_PROPERTY_WILDCARD);

        if (LOG.isDebugEnabled()) {
            parsers.forEach((k, v) -> LOG.debug(String.format("Found annotation config parser '%s' as %s", k, v.getClass())));
        }
        return parsers;
    }

    /**
     * Resolves annotation config parser from resource path lookup with given resource name. Scans classpath for annotation config parser meta information
     * with given name and returns instance of the parser. Returns optional instead of throwing exception when no annotation config parser
     * could be found.
     * @param parser
     * @return
     */
    static Optional<AnnotationConfigParser> lookup(String parser) {
        try {
            AnnotationConfigParser instance;
            if (parser.contains(".")) {
                StringTokenizer stringTokenizer = new StringTokenizer(parser, ".");
                instance = TYPE_RESOLVER.resolve(stringTokenizer.nextToken(), stringTokenizer.nextToken());
            } else {
                instance = TYPE_RESOLVER.resolve(parser);
            }

            return Optional.of(instance);
        } catch (CitrusRuntimeException e) {
            LOG.warn(String.format("Failed to resolve annotation config parser from resource '%s/%s'", RESOURCE_PATH, parser));
        }

        return Optional.empty();
    }

    /**
     * Parse given annotation to a proper endpoint instance.
     * @param annotation
     * @param referenceResolver
     * @return
     */
    T parse(A annotation, ReferenceResolver referenceResolver);
}
