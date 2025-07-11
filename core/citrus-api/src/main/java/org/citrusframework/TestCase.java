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

package org.citrusframework;

import java.util.List;
import java.util.Map;

import org.citrusframework.common.Described;
import org.citrusframework.common.Named;
import org.citrusframework.container.TestActionContainer;
import org.citrusframework.context.TestContext;

/**
 * Test case executing a list of {@link TestAction} in sequence.
 *
 * @since 2006
 */
@SuppressWarnings({"unused", "JavaDoc"})
public interface TestCase extends TestActionContainer, Named, Described {

    /**
     * Starts the test case.
     */
    void start(final TestContext context);

    /**
     * Sequence of test actions before the test case.
     */
    default void beforeTest(final TestContext context) {
        // subclasses may add some logic before the test.
    }

    /**
     * Sequence of test actions after test case. This operation does not raise andy errors - exceptions will only be
     * logged as warning. This is because we do not want to overwrite errors that may have occurred before in test
     * execution.
     */
    default void afterTest(final TestContext context) {
        // subclasses may add some logic after the test.
    }

    /**
     * Executes a single test action with given test context.
     */
    void executeAction(final TestAction action, final TestContext context);

    /**
     * Method that will be executed in any case of test case result (success, error). Usually used for clean up tasks.
     */
    void finish(final TestContext context);

    /**
     * Get the test case meta information.
     */
    TestCaseMetaInfo getMetaInfo();

    /**
     * Gets the value of the testClass property.
     */
    Class<?> getTestClass();

    /**
     * Set the test class type.
     */
    void setTestClass(final Class<?> type);

    /**
     * Get the package name.
     */
    String getPackageName();

    /**
     * Set the package name.
     */
    void setPackageName(final String packageName);

    @Override
    String getName();

    /**
     * Sets the test result from outside.
     */
    void setTestResult(final TestResult testResult);

    /**
     * Retrieve test result.
     */
    TestResult getTestResult();

    /**
     * Marks that this test case runs its actions one by one growing over time. This is usually the case when using the
     * Java DSL that adds test actions as the Java method is processed. XML test cases usually build and load all
     * actions beforehand and run all actions in one step.
     */
    default boolean isIncremental() {
        return false;
    }

    /**
     * Sets the test runner flag.
     */
    void setIncremental(boolean incremental);

    /**
     * Gets the variables for this test case.
     */
    Map<String, Object> getVariableDefinitions();

    /**
     * Gets the adhoc endpoints for this test case.
     */
    List<String> getEndpointDefinitions();

    /**
     * Adds action to finally action chain.
     */
    default void addFinalAction(TestAction action) {
        addFinalAction(() -> action);
    }

    /**
     * Adds action to finally action chain.
     */
    void addFinalAction(TestActionBuilder<?> builder);

    /**
     * Provides access to the raw test action builders used to construct the list of actions in this test case.
     */
    List<TestActionBuilder<?>> getActionBuilders();

    /**
     * Immediately fails the {@link TestCase}, meaning the underlying {@link TestResult} will contain the
     * given {@link Throwable} exception.
     *
     * @param throwable the exception to attach to the {@link TestResult}
     */
    void fail(Throwable throwable);
}
