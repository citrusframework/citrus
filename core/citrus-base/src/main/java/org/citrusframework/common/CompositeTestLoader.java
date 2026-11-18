/*
 * Copyright the original author or authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.citrusframework.TestCase;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;

/**
 * Test loader running several resolved test sources in given order as part of one test. Each test source runs
 * with its own test loader instance. The composite fails when at least one test source fails, naming all failed
 * sources in the failure message.
 *
 * The composite does not hold any injected framework fields (e.g. test runner or test context), so injections
 * performed by host frameworks are ignored. Each child loader initializes its own test runner and test context.
 *
 * @since 5.3
 */
public class CompositeTestLoader implements TestLoader {

    private final List<String> names;
    private final List<TestLoader> delegates;

    private final List<Consumer<TestCase>> handlers = new ArrayList<>();

    private TestCase testCase;

    /**
     * Constructor with resolved source names and the test loaders to run.
     * @param names the test source names used to identify failed sources.
     * @param delegates the test loaders to run in given order.
     */
    public CompositeTestLoader(List<String> names, List<TestLoader> delegates) {
        this.names = names;
        this.delegates = delegates;
    }

    @Override
    public void load() {
        List<String> failed = new ArrayList<>();

        for (int i = 0; i < delegates.size(); i++) {
            TestLoader delegate = delegates.get(i);
            try {
                delegate.load();
            } catch (TestCaseFailedException e) {
                if (testCase == null) {
                    testCase = delegate.getTestCase();
                }

                failed.add(names.get(i));
            }
        }

        if (!failed.isEmpty()) {
            throw new TestCaseFailedException(new CitrusRuntimeException(
                    "%d of %d test sources failed: %s".formatted(failed.size(), delegates.size(), String.join(", ", failed))));
        }

        testCase = delegates.get(delegates.size() - 1).getTestCase();
        handlers.forEach(handler -> handler.accept(testCase));
    }

    @Override
    public void configureTestCase(Consumer<TestCase> configurer) {
        delegates.forEach(delegate -> delegate.configureTestCase(configurer));
    }

    @Override
    public void doWithTestCase(Consumer<TestCase> handler) {
        handlers.add(handler);
    }

    @Override
    public void setTestClass(Class<?> testClass) {
        delegates.forEach(delegate -> delegate.setTestClass(testClass));
    }

    @Override
    public void setTestName(String testName) {
        delegates.forEach(delegate -> delegate.setTestName(testName));
    }

    @Override
    public void setPackageName(String packageName) {
        delegates.forEach(delegate -> delegate.setPackageName(packageName));
    }

    @Override
    public TestCase getTestCase() {
        return testCase;
    }

    /**
     * Gets all test cases of the child loaders, in execution order. In case of failures this holds the test
     * cases of all children, the first failed one being the one reported by {@link #getTestCase()}.
     * @return the list of test cases.
     */
    public List<TestCase> getTestCases() {
        List<TestCase> testCases = new ArrayList<>();
        delegates.forEach(delegate -> testCases.add(delegate.getTestCase()));
        return Collections.unmodifiableList(testCases);
    }
}
