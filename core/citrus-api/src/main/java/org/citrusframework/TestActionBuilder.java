/*
 * Copyright 2006-2015 the original author or authors.
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

import java.util.Map;
import java.util.Optional;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test action builder.
 * @author Christoph Deppisch
 * @since 2.3
 */
@FunctionalInterface
public interface TestActionBuilder<T extends TestAction> {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(TestActionBuilder.class);

    /** Endpoint builder resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/action/builder";

    /** Default Citrus test action builders from classpath resource properties */
    ResourcePathTypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Builds new test action instance.
     * @return the built test action.
     */
    T build();

    interface DelegatingTestActionBuilder<T extends TestAction> extends TestActionBuilder<T> {

        /**
         * Obtains the delegate test action builder.
         * @return
         */
        TestActionBuilder<?> getDelegate();
    }

    /**
     * Resolves all available test action builders from resource path lookup. Scans classpath for test action builder meta information
     * and instantiates those builders.
     * @return
     */
    static Map<String, TestActionBuilder<?>> lookup() {
        Map<String, TestActionBuilder<?>> builders = TYPE_RESOLVER.resolveAll();

        if (logger.isDebugEnabled()) {
            builders.forEach((k, v) -> logger.debug(String.format("Found test action builder '%s' as %s", k, v.getClass())));
        }
        return builders;
    }

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
            logger.warn(String.format("Failed to resolve test action builder from resource '%s/%s'", RESOURCE_PATH, builder));
        }

        return Optional.empty();
    }
}
