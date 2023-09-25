/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.main;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public interface TestEngine {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(TestEngine.class);

    /** Endpoint parser resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/engine";

    /** Default Citrus engine from classpath resource properties */
    ResourcePathTypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Runs all tests with the given engine.
     */
    void run();

    /**
     * Resolves engine from resource path lookup. Scans classpath for engine meta information
     * and instantiates engine with respective name given in test run configuration.
     * @param configuration the test run configuration used to initialize the engine.
     * @return
     */
    static TestEngine lookup(TestRunConfiguration configuration) {
        try {
            TestEngine testEngine = TYPE_RESOLVER.resolve(configuration.getEngine(), configuration);
            logger.debug(String.format("Using Citrus engine '%s' as %s", configuration.getEngine(), testEngine));
            return testEngine;
        } catch (CitrusRuntimeException e) {
            throw new CitrusRuntimeException(String.format("Failed to resolve Citrus engine from resource '%s/%s'", RESOURCE_PATH, configuration.getEngine()), e);
        }
    }
}
