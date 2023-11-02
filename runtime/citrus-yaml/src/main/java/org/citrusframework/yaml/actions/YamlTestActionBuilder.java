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

package org.citrusframework.yaml.actions;

import java.util.Map;
import java.util.Optional;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 */
public interface YamlTestActionBuilder {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(YamlTestActionBuilder.class);

    /** Endpoint builder resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/yaml/builder";

    /** Default Citrus test action builders from classpath resource properties */
    ResourcePathTypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Resolves all available test action builder instances from resource path lookup. Scans classpath for test action builder meta information
     * and instantiates the components.
     * @return
     */
    static Map<String, TestActionBuilder<?>> lookup() {
        Map<String, TestActionBuilder<?>> builders = TYPE_RESOLVER.resolveAll();

        if (logger.isDebugEnabled()) {
            builders.forEach((k, v) -> logger.debug(String.format("Found YAML test action builder '%s' as %s", k, v.getClass())));
        }
        return builders;
    }

    /**
     * Resolves test action builder from resource path lookup with given resource name. Scans classpath for test action builder meta information
     * with given name and returns instance of the builder. Returns optional instead of throwing exception when no test action builder
     * could be found.
     *
     * Given builder name is a combination of resource file name and type property separated by '.' character.
     * @param name
     * @return
     */
    static Optional<TestActionBuilder<?>> lookup(String name) {
        try {
            return Optional.of(TYPE_RESOLVER.resolve(name));
        } catch (CitrusRuntimeException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Failed to resolve test action builder from resource '%s/%s'", RESOURCE_PATH, name), e);
            } else {
                logger.warn(String.format("Failed to resolve test action builder from resource '%s/%s'", RESOURCE_PATH, name));
            }
        }

        return Optional.empty();
    }
}
