/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.admin.model;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class TestResult {

    private TestCaseData testCase;
    private boolean success;
    private String stackTrace;
    private String failureStack;

    /**
     * @return
     */
    public TestCaseData getTestCase() {
        return testCase;
    }

    /**
     * @param value
     */
    public void setTestCase(TestCaseData value) {
        this.testCase = value;
    }

    /**
     * @return
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param value
     */
    public void setSuccess(boolean value) {
        this.success = value;
    }

    /**
     * @return
     */
    public String getStackTrace() {
        return stackTrace;
    }

    /**
     * @param value
     */
    public void setStackTrace(String value) {
        this.stackTrace = value;
    }

    /**
     * @return
     */
    public String getFailureStack() {
        return failureStack;
    }

    /**
     * @param value
     */
    public void setFailureStack(String value) {
        this.failureStack = value;
    }

}
