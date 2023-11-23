/*
 * Copyright 2006-2024 the original author or authors.
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

package org.citrusframework.common;

import org.citrusframework.TestCase;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toMap;

/**
 * Test loader interface.
 * <p>
 * <b>Deprecation notice:</b> Implementations of this interface load <i>and</i> execute test cases in one breath. That
 * is very intransparent to end-users. It is therefore supersed by the {@link TestLoaderAndExecutor}, which splits
 * loading and execution of test cases into multiple methods, or at least has a more transparent API.
 *
 * @author Christoph Deppisch
 * @since 2.1
 * @deprecated use {@link TestLoaderAndExecutor} instead
 */
@Deprecated
public interface TestLoader {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(TestLoader.class);

    /** Test loader resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/test/loader";

    /** Default Citrus test loader from classpath resource properties */
    ResourcePathTypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    String XML = "xml";
    String YAML = "yaml";
    String SPRING = "spring";
    String GROOVY = "groovy";

    /**
     * Loads and creates new test case object. The test case is expected to be cached and returned by {@link TestLoader#getTestCase()}.
     */
    void load();

    /**
     * Adds test case handler that is called before test case gets executed.
     * @param configurer
     */
    void configureTestCase(Consumer<TestCase> configurer);

    /**
     * Adds test case handler that is called once the test case has been executed.
     * @param handler
     */
    void doWithTestCase(Consumer<TestCase> handler);

    void setTestClass(Class<?> testClass);

    void setTestName(String testName);

    void setPackageName(String packageName);

    /**
     * Gets the loaded test case or null if it has not been loaded yet.
     * @return
     */
    TestCase getTestCase();

    /**
     * Resolves all available test loader from resource path lookup. Scans classpath for test loader meta information
     * and instantiates the components.
     *
     * @return the available test loaders
     */
    static Map<String, TestLoader> lookup() {
        Map<String, TestLoader> loader = TYPE_RESOLVER.resolveAll()
                .entrySet().stream()
                .filter(entry -> entry.getValue() instanceof TestLoader)
                .collect(toMap(Map.Entry::getKey, e -> ((TestLoader) e.getValue())));

        if (logger.isDebugEnabled()) {
            loader.forEach((k, v) -> logger.debug(String.format("Found test loader '%s' as %s", k, v.getClass())));
        }

        return loader;
    }

    /**
     * Resolves test loader from resource path lookup with given resource name. Scans classpath for test loader meta information
     * with given name and returns instance of the loader. Returns optional instead of throwing exception when no test loader
     * could be found.
     *
     * @param loader the name of the test loader
     * @return the test loader, if present
     */
    static Optional<TestLoader> lookup(String loader) {
        try {
            return Optional.of(TYPE_RESOLVER.resolve(loader));
        } catch (CitrusRuntimeException e) {
            logger.warn(String.format("Failed to resolve test loader from resource '%s/%s'", RESOURCE_PATH, loader));
        } catch (ClassCastException ignore) {
            // Ignore exception
        }

        return Optional.empty();
    }
}
