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

package org.citrusframework.report;

import org.citrusframework.TestCase;

/**
 * Test listener interface. Listeners invoked on test start, finish, failure, skip, success.
 *
 * @author Christoph Deppisch
 */
public interface TestListener {
    /**
     * Invoked when test gets started
     * @param test
     */
    void onTestStart(TestCase test);

    /**
     * Invoked when test gets finished
     * @param test
     */
    void onTestFinish(TestCase test);

    /**
     * Invoked when test finished with success
     * @param test
     */
    void onTestSuccess(TestCase test);

    /**
     * Invoked when test finished with failure
     * @param test
     */
    void onTestFailure(TestCase test, Throwable cause);

    /**
     * Invoked when test is skipped
     * @param test
     */
    void onTestSkipped(TestCase test);
}
