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

package com.consol.citrus.message;

import java.util.Optional;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.spi.ResourcePathTypeResolver;
import com.consol.citrus.spi.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * @author Christoph Deppisch
 */
public interface ScriptPayloadBuilder extends MessagePayloadBuilder {

    /** Logger */
    Logger LOG = LoggerFactory.getLogger(ScriptPayloadBuilder.class);

    /** Message processor resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/script/message/builder";

    /** Type resolver to find custom message processors on classpath via resource path lookup */
    TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Resolves processor from resource path lookup with given processor resource name. Scans classpath for processor meta information
     * with given name and returns instance of processor. Returns optional instead of throwing exception when no processor
     * could be found.
     * @param type
     * @return
     */
    static <T extends ScriptPayloadBuilder> Optional<T> lookup(String type) {
        try {
            T instance = TYPE_RESOLVER.resolve(type);
            return Optional.of(instance);
        } catch (CitrusRuntimeException e) {
            LOG.warn(String.format("Failed to resolve script payload builder from resource '%s/%s'", RESOURCE_PATH, type));
        }

        return Optional.empty();
    }

    void setScript(String script);

    void setFile(Resource file);
}
