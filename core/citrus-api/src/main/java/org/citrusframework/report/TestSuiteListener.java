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

/**
 * Listener for events regarding a test suite (start, finish, failure, success)
 * @author Christoph Deppisch
 */
public interface TestSuiteListener {

    /**
     * Invoked on test suite start.
     */
    void onStart();

    /**
     * Invoked after successful test suite start.
     */
    void onStartSuccess();

    /**
     * Invoked after failed test suite start.
     * @param cause
     */
    void onStartFailure(Throwable cause);

    /**
     * Invoked on test suite finish.
     */
    void onFinish();

    /**
     * Invoked after successful test suite finish.
     */
    void onFinishSuccess();

    /**
     * Invoked after failed test suite finish.
     * @param cause
     */
    void onFinishFailure(Throwable cause);
}
