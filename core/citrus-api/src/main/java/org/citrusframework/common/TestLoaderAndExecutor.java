/*
 * Copyright 2024 the original author or authors.
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
import org.citrusframework.spi.Resource;

import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

/**
 * Provides an interface for loading and executing test cases.
 * <p>
 * This interface extends both {@link InitializingPhase} and {@link TestLoader}, offering methods to discover, load, and
 * execute test cases. Test cases are resolved using resource path lookup and can be executed immediately upon loading
 * or loaded for later execution.
 */
public interface TestLoaderAndExecutor extends InitializingPhase, TestLoader {

    /**
     * Discovers and returns all available {@link TestLoaderAndExecutor} instances by scanning the classpath for test
     * loader meta-information. This method facilitates the dynamic discovery of test loader and executor components.
     *
     * @return A map of test loader and executor names to their respective {@link TestLoaderAndExecutor} instances.
     */
    static Map<String, TestLoaderAndExecutor> lookup() {
        Map<String, TestLoaderAndExecutor> testLoaderAndExecutors = TYPE_RESOLVER.resolveAll();
        testLoaderAndExecutors = testLoaderAndExecutors.entrySet()
                .stream()
                .filter(entry -> entry.getValue() instanceof TestLoaderAndExecutor)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (logger.isDebugEnabled()) {
            testLoaderAndExecutors.forEach((k, v) -> logger.debug(String.format("Found test loader and executor '%s' as %s", k, v.getClass())));
        }

        return testLoaderAndExecutors;
    }


    /**
     * Attempts to resolve a specific {@link TestLoaderAndExecutor} by its name. This method scans the classpath for
     * test loader meta-information matching the given name. It returns an Optional to avoid exceptions when a test
     * loader is not found.
     *
     * @param loader the name of the test loader to resolve.
     * @return An {@link Optional} containing the resolved {@link TestLoaderAndExecutor}, if found.
     */
    static Optional<TestLoaderAndExecutor> lookup(String loader) {
        try {
            return Optional.of(TYPE_RESOLVER.resolve(loader));
        } catch (CitrusRuntimeException e) {
            logger.warn(String.format("Failed to resolve test loader and executor from resource '%s/%s'", RESOURCE_PATH, loader));
        } catch (ClassCastException ignore) {
            // Ignore exception
        }

        return Optional.empty();
    }

    /**
     * Loads a new test case and immediately executes it. This method combines the process of loading a test case from
     * resources and executing it in one step.
     *
     * @return The executed {@link TestCase} instance.
     */
    default TestCase loadAndExecute() {
        load();
        return getTestCase();
    }

    /**
     * Loads a test case from the specified resource without executing it. This method allows for deferred execution
     * of the test case.
     *
     * @param resource the resource from which to load the test case.
     * @return The loaded {@link TestCase} instance.
     */
    TestCase loadTestCase(Resource resource);

    /**
     * Executes the provided test case. This method allows for the execution of a preloaded test case.
     *
     * @param testCase the test case to execute.
     * @return The executed {@link TestCase} instance.
     */
    TestCase executeTestCase(TestCase testCase);
}
