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

package org.citrusframework.agent.plugin.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Optional;

import org.citrusframework.TestResult;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Test result that is able to serialize/deserialize from Json objects.
 */
public class RemoteResult {

    /** Result as String */
    private String result;

    /** Name of the test */
    private String testName;

    /** Fully qualified test class name */
    private String className;

    /** Duration of the test run */
    private Long duration;

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
        remoteResult.setResult(testResult.getResult());
        remoteResult.setTestName(testResult.getTestName());
        remoteResult.setClassName(testResult.getClassName());
        remoteResult.setDuration(Optional.of(testResult)
                .map(TestResult::getDuration)
                .orElse(Duration.ZERO)
                .toMillis());
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
        TestResult result;
        if (remoteResult.isSuccess()) {
            result = TestResult.success(remoteResult.getTestName(), remoteResult.getClassName());
        } else if (remoteResult.isSkipped()) {
            result = TestResult.skipped(remoteResult.getTestName(), remoteResult.getClassName());
        } else if (remoteResult.isFailed()) {
            // TODO: Check if this is fine, failure stack, failure type are never used in the new Citrus version
            result = TestResult
                    .failed(
                            remoteResult.getTestName(),
                            remoteResult.getClassName(),
                            remoteResult.getErrorMessage())
                    .withFailureType(remoteResult.getCause());
        } else {
            throw new CitrusRuntimeException(
                    "Unexpected test result state " + remoteResult.getTestName());
        }
        return result.withDuration(Duration.ofMillis(remoteResult.getDuration()));
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getFailureStack() {
        return failureStack;
    }

    public void setFailureStack(String failureStack) {
        this.failureStack = failureStack;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }
}
