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

package org.citrusframework.report;

import org.citrusframework.TestCase;

/**
 * Test listener interface. Listeners invoked on test start, finish, failure, skip, success.
 */
public interface TestListener {
    /**
     * Invoked when test when a test starts execution
     */
    void onTestStart(TestCase test);

    /**
     * @deprecated use on {@link #onTestExecutionEnd(TestCase)}
     */
    @Deprecated(forRemoval = true)
    default void onTestFinish(TestCase test) {
        // Do nothing
    }

    /**
     * Invoked when test execution starts
     * (after {@link org.citrusframework.container.BeforeTest} execution} execution)
     */
    default void onTestExecutionStart(TestCase test) {
        // Default implementation does nothing
    }


    /**
     * Invoked when test execution has ended (after final actions execution and
     * before {@link org.citrusframework.container.AfterTest} execution)
     *
     * @see #onTestEnd(TestCase)
     */
    default void onTestExecutionEnd(TestCase test) {
        onTestFinish(test);
    }

    /**
     * Invoked at the very end of test execution
     *
     * @see #onTestFinish(TestCase)
     */
    default void onTestEnd(TestCase test) {
        // Default implementation does nothing
    }

    /**
     * Invoked when a test finishes successfully
     */
    void onTestSuccess(TestCase test);

    /**
     * Invoked when a test finishes with failure
     */
    void onTestFailure(TestCase test, Throwable cause);

    /**
     * Invoked when a test is skipped
     */
    void onTestSkipped(TestCase test);

    /**
     * Invoked when final actions start, only if any exist
     */
    default void onFinalActionsStart(TestCase test) {
        // Default implementation does nothing
    }

    /**
     * Invoked after final actions have completely finished, only if any exist
     */
    default void onFinalActionsEnd(TestCase test) {
        // Default implementation does nothing
    }

    /**
     * Invoked when {@link org.citrusframework.container.BeforeTest} execution starts, only if any exist
     */
    default void onBeforeTestStart(TestCase test) {
        // Default implementation does nothing
    }

    /**
     * Invoked when {@link org.citrusframework.container.BeforeTest} execution ends, only if any exist
     */
    default void onBeforeTestEnd(TestCase test) {
        // Default implementation does nothing
    }

    /**
     * Invoked when {@link org.citrusframework.container.AfterTest} execution starts, only if any exist
     */
    default void onAfterTestStart(TestCase test) {
        // Default implementation does nothing
    }

    /**
     * Invoked when {@link org.citrusframework.container.AfterTest} execution ends, only if any exist
     */
    default void onAfterTestEnd(TestCase test) {
        // Default implementation does nothing
    }
}
