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

package com.consol.citrus.xml.actions;

import java.util.Optional;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.spi.ResourcePathTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 */
public interface XmlTestActionBuilder {

    /** Logger */
    Logger LOG = LoggerFactory.getLogger(XmlTestActionBuilder.class);

    /** Endpoint builder resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/xml/builder";

    /** Default Citrus test action builders from classpath resource properties */
    ResourcePathTypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Resolves test action builder from resource path lookup with given resource name. Scans classpath for test action builder meta information
     * with given name and returns instance of the builder. Returns optional instead of throwing exception when no test action builder
     * could be found.
     *
     * Given builder name is a combination of resource file name and type property separated by '.' character.
     * @param builder
     * @return
     */
    static Optional<TestActionBuilder<?>> lookup(String builder) {
        try {
            return Optional.of(TYPE_RESOLVER.resolve(builder));
        } catch (CitrusRuntimeException e) {
            LOG.warn(String.format("Failed to resolve test action builder from resource '%s/%s'", RESOURCE_PATH, builder));
        }

        return Optional.empty();
    }
}
