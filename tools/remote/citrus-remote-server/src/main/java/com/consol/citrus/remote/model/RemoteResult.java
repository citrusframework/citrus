/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.remote.model;

import com.consol.citrus.TestResult;
import com.consol.citrus.exceptions.CitrusRuntimeException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class RemoteResult {

    /** Name of the test */
    private String testName;

    /** Fully qualified test class name */
    private String testClass;

    /** Failure cause */
    private String cause;

    /** Failure message */
    private String errorMessage;

    /** Failure cause */
    private String failureStack;

    private boolean success;
    private boolean failed;
    private boolean skipped;

    /**
     * Convert traditional test result to remote result.
     * @param testResult
     * @return
     */
    public static RemoteResult fromTestResult(TestResult testResult) {
        RemoteResult remoteResult = new RemoteResult();
        remoteResult.setTestName(testResult.getTestName());
        remoteResult.setTestClass(testResult.getClassName());
        remoteResult.setSuccess(testResult.isSuccess());
        remoteResult.setFailed(testResult.isFailed());
        remoteResult.setSkipped(testResult.isSkipped());

        if (testResult.isFailed()) {
            Optional.ofNullable(testResult.getCause()).ifPresent(cause -> {
                remoteResult.setCause(cause.getClass().getName());
                remoteResult.setErrorMessage(cause.getMessage());

                StringWriter stackWriter = new StringWriter();
                cause.printStackTrace(new PrintWriter(stackWriter));
                remoteResult.setFailureStack(stackWriter.toString());
            });
        }
        return remoteResult;
    }

    /**
     * Convert remote result to traditional result.
     * @param remoteResult
     * @return
     */
    public static TestResult toTestResult(RemoteResult remoteResult) {
        if (remoteResult.isSuccess()) {
            return TestResult.success(remoteResult.getTestName(), remoteResult.getTestClass());
        } else if (remoteResult.isSkipped()) {
            return TestResult.skipped(remoteResult.getTestName(), remoteResult.getTestClass());
        } else if (remoteResult.isFailed()) {
            return TestResult.failed(remoteResult.getTestName(), remoteResult.getTestClass(), remoteResult.getErrorMessage())
                             .withFailureType(remoteResult.getCause())
                             .withFailureStack(remoteResult.getFailureStack());
        } else {
            throw new CitrusRuntimeException("Unexpected test result state " + remoteResult.getTestName());
        }
    }

    /**
     * Gets the testName.
     *
     * @return
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Sets the testName.
     *
     * @param testName
     */
    public void setTestName(String testName) {
        this.testName = testName;
    }

    /**
     * Gets the testClass.
     *
     * @return
     */
    public String getTestClass() {
        return testClass;
    }

    /**
     * Sets the testClass.
     *
     * @param testClass
     */
    public void setTestClass(String testClass) {
        this.testClass = testClass;
    }

    /**
     * Gets the cause.
     *
     * @return
     */
    public String getCause() {
        return cause;
    }

    /**
     * Sets the cause.
     *
     * @param cause
     */
    public void setCause(String cause) {
        this.cause = cause;
    }

    /**
     * Gets the errorMessage.
     *
     * @return
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the errorMessage.
     *
     * @param errorMessage
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Gets the failureStack.
     *
     * @return
     */
    public String getFailureStack() {
        return failureStack;
    }

    /**
     * Sets the failureStack.
     *
     * @param failureStack
     */
    public void setFailureStack(String failureStack) {
        this.failureStack = failureStack;
    }

    /**
     * Gets the success.
     *
     * @return
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets the success.
     *
     * @param success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Gets the failed.
     *
     * @return
     */
    public boolean isFailed() {
        return failed;
    }

    /**
     * Sets the failed.
     *
     * @param failed
     */
    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    /**
     * Gets the skipped.
     *
     * @return
     */
    public boolean isSkipped() {
        return skipped;
    }

    /**
     * Sets the skipped.
     *
     * @param skipped
     */
    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }
}
