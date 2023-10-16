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

package org.citrusframework;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Class representing test results (failed, successful, skipped)
 *
 * @author Christoph Deppisch
 */
public final class TestResult {

    /** Possible test results */
    private enum RESULT {SUCCESS, FAILURE, SKIP}

    /** Actual result */
    private final RESULT result;

    /** Name of the test */
    private final String testName;

    /** Fully qualified class name of the test */
    private final String className;

    /** Optional test parameters */
    private final Map<String, Object> parameters;

    /** Failure cause */
    private final Throwable cause;

    /** Error message */
    private final String errorMessage;

    /** Failure stack trace */
    private String failureStack;

    /** Failure type information */
    private String failureType;

    /**
     * Create new test result for successful execution.
     * @param name
     * @param className
     * @return
     */
    public static TestResult success(String name, String className) {
        return new TestResult(name, className, RESULT.SUCCESS);
    }

    /**
     * Create new test result with parameters for successful execution.
     * @param name
     * @param className
     * @param parameters
     * @return
     */
    public static TestResult success(String name, String className, Map<String, Object> parameters) {
        return new TestResult(name, className, RESULT.SUCCESS, parameters);
    }

    /**
     * Create new test result for skipped test.
     * @param name
     * @param className
     * @return
     */
    public static TestResult skipped(String name, String className) {
        return new TestResult(name, className, RESULT.SKIP);
    }

    /**
     * Create new test result with parameters for skipped test.
     * @param name
     * @param className
     * @param parameters
     * @return
     */
    public static TestResult skipped(String name, String className, Map<String, Object> parameters) {
        return new TestResult(name, className, RESULT.SKIP, parameters);
    }

    /**
     * Create new test result for failed execution.
     * @param name
     * @param className
     * @param cause
     * @return
     */
    public static TestResult failed(String name, String className, Throwable cause) {
        return new TestResult(name, className, RESULT.FAILURE, cause);
    }

    /**
     * Create new test result for failed execution.
     * @param name
     * @param className
     * @param errorMessage
     * @return
     */
    public static TestResult failed(String name, String className, String errorMessage) {
        return new TestResult(name, className, RESULT.FAILURE, null, errorMessage);
    }

    /**
     * Create new test result with parameters for failed execution.
     * @param name
     * @param className
     * @param cause
     * @param parameters
     * @return
     */
    public static TestResult failed(String name, String className, Throwable cause, Map<String, Object> parameters) {
        return new TestResult(name, className, RESULT.FAILURE, cause, parameters);
    }

    /**
     * Constructor using fields.
     * @param name
     * @param className
     * @param result
     */
    private TestResult(String name, String className, RESULT result) {
        this(name, className, result, new HashMap<>());
    }

    /**
     * Constructor using fields.
     * @param name
     * @param className
     * @param result
     * @param parameters
     */
    private TestResult(String name, String className, RESULT result, Map<String, Object> parameters) {
        this(name, className, result, null, parameters);
    }

    /**
     * Constructor using fields.
     * @param name
     * @param className
     * @param result
     * @param cause
     */
    private TestResult(String name, String className, RESULT result, Throwable cause) {
        this(name, className, result, cause, new HashMap<>());
    }

    /**
     * Constructor using fields.
     * @param name
     * @param className
     * @param result
     * @param errorMessage
     */
    private TestResult(String name, String className, RESULT result, Throwable cause, String errorMessage) {
        this(name, className, result, cause, errorMessage, new HashMap<>());
    }

    /**
     * Constructor using fields.
     * @param name
     * @param className
     * @param result
     * @param parameters
     */
    private TestResult(String name, String className, RESULT result, Throwable cause, Map<String, Object> parameters) {
        this(name, className, result, cause, Optional.ofNullable(cause).map(Throwable::getMessage).orElse(""), parameters);
    }

    /**
     * Constructor using fields.
     * @param name
     * @param className
     * @param result
     * @param cause
     * @param parameters
     */
    private TestResult(String name, String className, RESULT result, Throwable cause, String errorMessage, Map<String, Object> parameters) {
        this.testName = name;
        this.className = className;
        this.result = result;
        this.cause = cause;
        this.parameters = parameters;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (parameters != null && parameters.size() > 0) {
            builder.append(" ")
                    .append(testName)
                    .append("(")
                    .append(parameters.values().stream().map(Object::toString).collect(Collectors.joining(",")))
                    .append(") ");
        } else {
            builder.append(" ").append(testName).append(" ");
        }

        int spaces = 65 - builder.length();
        for (int i = 0; i < spaces; i++) {
            builder.append(".");
        }

        switch (result) {
            case SUCCESS:
                builder.append(" SUCCESS");
                break;
            case SKIP:
                builder.append(" SKIPPED");
                break;
            case FAILURE:
                builder.append(" FAILED");
                break;
            default:
                break;
        }

        return builder.toString();
    }

    /**
     * Checks successful result state.
     * @return
     */
    public boolean isSuccess() {
        return !isSkipped() && result != null && result.equals(RESULT.SUCCESS);
    }

    /**
     * Checks failed result state.
     * @return
     */
    public boolean isFailed() {
        return !isSkipped() && result != null && result.equals(RESULT.FAILURE);
    }

    /**
     * Checks skipped result state.
     * @return
     */
    public boolean isSkipped() {
        return result != null && result.equals(RESULT.SKIP);
    }

    /**
     * Getter for the failure cause.
     * @return the cause
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Getter for the test name.
     * @return the testName
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Gets the className.
     *
     * @return
     */
    public String getClassName() {
        return className;
    }

    /**
     * Getter for test result.
     * @return the result
     */
    public String getResult() {
        return result.name();
    }

    /**
     * Gets the parameters.
     * @return the parameters
     */
    public Map<String, Object> getParameters() {
        return parameters;
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
     * Gets the failureType.
     *
     * @return
     */
    public String getFailureType() {
        return failureType;
    }

    /**
     * Sets the failureType.
     *
     * @param failureType
     */
    public void setFailureType(String failureType) {
        this.failureType = failureType;
    }

    /**
     * Sets failure type information in fluent API.
     * @return
     */
    public TestResult withFailureType(String failureType) {
        setFailureType(failureType);
        return this;
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
     * Sets failure stack trace information in fluent API.
     * @return
     */
    public TestResult withFailureStack(String failureStack) {
        setFailureStack(failureStack);
        return this;
    }
}
