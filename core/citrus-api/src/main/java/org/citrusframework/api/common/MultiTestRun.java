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

package org.citrusframework.api.common;

import java.util.List;

import org.citrusframework.TestCase;

/**
 * One execution of a multi-test source. The beforeAll hook runs before the first
 * enabled test case, afterAll and the file-level cleanups run on {@link #close()}. The run executes one
 * test case at a time and is not reusable.
 *
 * @since 5.3
 */
public interface MultiTestRun extends AutoCloseable {

    /**
     * Gets the display name of the source: its declared name, else the test name of the loader.
     * @return the display name.
     */
    String getName();

    /**
     * Gets the names of all test cases in declaration order.
     * @return the test case names.
     */
    List<String> getTestNames();

    /**
     * Runs the test case with the given index together with its beforeEach and afterEach actions.
     * Runs beforeAll first when no enabled test case has run yet.
     * @param index the position of the test case in declaration order.
     * @return the finished test case.
     * @throws org.citrusframework.exceptions.TestCaseFailedException when the test case failed,
     * also when beforeAll failed.
     */
    TestCase runTest(int index);

    /**
     * Gets the test cases run so far, in execution order, followed by the failed afterAll result when
     * afterAll or a file-level cleanup failed.
     * @return the test cases.
     */
    List<TestCase> getTestCases();

    /**
     * Runs afterAll and the file-level cleanups once, if beforeAll was started. Calling it again has
     * no effect.
     * @throws org.citrusframework.exceptions.CitrusRuntimeException when afterAll or a cleanup failed.
     */
    @Override
    void close();
}
