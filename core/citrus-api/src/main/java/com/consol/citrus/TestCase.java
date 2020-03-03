/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus;

import java.util.List;
import java.util.Map;

import com.consol.citrus.container.AfterTest;
import com.consol.citrus.container.BeforeTest;
import com.consol.citrus.container.TestActionContainer;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.report.TestActionListenersAware;

/**
 * Test case executing a list of {@link TestAction} in sequence.
 *
 * @author Christoph Deppisch
 * @since 2006
 */
@SuppressWarnings({"unused", "JavaDoc"})
public interface TestCase extends TestActionContainer, TestActionListenersAware {

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
     * Sequence of test actions after test case. This operation does not raise andy errors - exceptions
     * will only be logged as warning. This is because we do not want to overwrite errors that may have occurred
     * before in test execution.
     */
    default void afterTest(final TestContext context) {
        // subclasses may add some logic after the test.
    }

    /**
     * Executes a single test action with given test context.
     * @param action
     * @param context
     */
    void executeAction(final TestAction action, final TestContext context);

    /**
     * Method that will be executed in any case of test case result (success, error)
     * Usually used for clean up tasks.
     */
    void finish(final TestContext context);

    /**
     * Get the test case meta information.
     * @return the metaInfo
     */
    TestCaseMetaInfo getMetaInfo();

    /**
     * Gets the value of the testClass property.
     * @return the testClass
     */
    Class<?> getTestClass();

    /**
     * Set the test class type.
     * @param type
     */
    void setTestClass(final Class<?> type);

    /**
     * Get the package name
     * @return the packageName
     */
    String getPackageName();

    /**
     * Set the package name
     * @param packageName the packageName to set
     */
    void setPackageName(final String packageName);

    /**
     * Get the test name.
     * @return the test name.
     */
    String getName();

    /**
     * Sets the test result from outside.
     * @param testResult
     */
    void setTestResult(final TestResult testResult);

    /**
     * Marks this test case as Java DSL test runner.
     * @return
     */
    default boolean isTestRunner() {
        return false;
    }

    /**
     * Gets the variables for this test case.
     * @return
     */
    Map<String, Object> getVariableDefinitions();

    /**
     * Sets the before test action sequence.
     * @param beforeTest
     */
    void setBeforeTest(final List<BeforeTest> beforeTest);

    /**
     * Sets the after test action sequence.
     * @param afterTest
     */
    void setAfterTest(final List<AfterTest> afterTest);

    /**
     * Adds action to finally action chain.
     * @param action
     */
    default void addFinalAction(TestAction action) {
        addFinalAction(() -> action);
    }

    /**
     * Adds action to finally action chain.
     * @param builder
     */
    void addFinalAction(TestActionBuilder<?> builder);
}
