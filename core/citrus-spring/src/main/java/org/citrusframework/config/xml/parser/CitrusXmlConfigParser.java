/*
 * Copyright 2021 the original author or authors.
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

package org.citrusframework.config.xml.parser;

import java.util.Map;
import java.util.Optional;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 */
public interface CitrusXmlConfigParser {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(CitrusXmlConfigParser.class);

    /** Bean definition parser resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/config/parser";

    /** Type resolver to find custom message Xml config parsers on classpath via resource path lookup */
    TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Resolves all available config parsers from resource path lookup. Scans classpath for config parser meta information
     * and instantiates those parsers.
     * @return
     */
    static <T>  Map<String, T> lookup(String category) {
        Map<String, T> parser = TYPE_RESOLVER.resolveAll(category, TypeResolver.DEFAULT_TYPE_PROPERTY, null);

        if (logger.isDebugEnabled()) {
            parser.forEach((k, v) -> logger.debug(String.format("Found XML config parser '%s/%s' as %s", category, k, v.getClass())));
        }

        return parser;
    }

    /**
     * Resolves XML config parser from resource path lookup with given category and name. Scans classpath for parser meta information
     * with given name and returns instance of parser. Returns optional instead of throwing exception when no parser
     * could be found.
     * @param category
     * @param name
     * @return
     */
    static <T> Optional<T> lookup(String category, String name) {
        try {
            T instance = TYPE_RESOLVER.resolve(category + "/" + name);
            return Optional.of(instance);
        } catch (CitrusRuntimeException e) {
            logger.warn(String.format("Failed to resolve XML config parser from resource '%s/%s/%s'", RESOURCE_PATH, category, name));
        }

        return Optional.empty();
    }
}
