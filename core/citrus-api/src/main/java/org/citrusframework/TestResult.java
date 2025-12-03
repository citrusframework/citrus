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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.citrusframework.json.JsonStringBuilder;
import org.citrusframework.yaml.YamlStringBuilder;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.citrusframework.TestResult.RESULT.FAILURE;
import static org.citrusframework.TestResult.RESULT.SKIP;
import static org.citrusframework.TestResult.RESULT.SUCCESS;

/**
 * Class representing test results (failed, successful, skipped)
 *
 */
public final class TestResult {

    /**
     * Actual result
     */
    private final RESULT result;

    /**
     * Name of the test
     */
    private final String testName;

    /**
     * Fully qualified class name of the test
     */
    private final String className;

    /**
     * Optional test parameters
     */
    private final Map<String, Object> parameters = new HashMap<>();

    /**
     * Failure cause
     */
    private @Nullable Throwable cause;

    /**
     * Error message
     */
    private @Nullable String errorMessage;

    /**
     * Failure stack trace
     */
    private @Nullable String failureStack;

    /**
     * Failure type information
     */
    private @Nullable String failureType;

    /**
     * Execution duration
     */
    private @Nullable Duration duration;

    /**
     * Create new test result for successful execution.
     */
    public static TestResult success(String name, String className) {
        return new TestResult(SUCCESS, name, className);
    }

    /**
     * Create new test result with parameters for successful execution.
     */
    public static TestResult success(String name, String className, Map<String, Object> parameters) {
        return new TestResult(SUCCESS, name, className)
                .withParameters(parameters);
    }

    /**
     * Create new test result for skipped test.
     */
    public static TestResult skipped(String name, String className) {
        return new TestResult(SKIP, name, className);
    }

    /**
     * Create new test result with parameters for skipped test.
     */
    public static TestResult skipped(String name, String className, @Nonnull Map<String, Object> parameters) {
        return new TestResult(SKIP, name, className)
                .withParameters(parameters);
    }

    /**
     * Create new test result for failed execution.
     */
    public static TestResult failed(String name, String className, @Nullable Throwable cause) {
        return new TestResult(FAILURE, name, className)
                .withCause(cause)
                .withErrorMessage(ofNullable(cause).map(Throwable::getMessage).orElse(""));
    }

    /**
     * Create new test result for failed execution.
     */
    public static TestResult failed(String name, String className, @Nonnull String errorMessage) {
        return new TestResult(FAILURE, name, className)
                .withErrorMessage(errorMessage);
    }

    /**
     * Create new test result with parameters for failed execution.
     */
    public static TestResult failed(String name, String className, @Nullable Throwable cause, @Nonnull Map<String, Object> parameters) {
        return new TestResult(FAILURE, name, className)
                .withParameters(parameters)
                .withCause(cause)
                .withErrorMessage(ofNullable(cause).map(Throwable::getMessage).orElse(""));
    }

    /**
     * Constructor with only the required arguments.
     */
    TestResult(RESULT result, String testName, String className) {
        this.result = result;
        this.testName = testName;
        this.className = className;
    }

    public boolean isSuccess() {
        return SUCCESS.equals(result);
    }

    public boolean isFailed() {
        return FAILURE.equals(result);
    }

    public boolean isSkipped() {
        return SKIP.equals(result);
    }

    public String getResult() {
        return result.name();
    }

    public String getTestName() {
        return testName;
    }

    public String getClassName() {
        return className;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Nullable
    public Throwable getCause() {
        return cause;
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    @Nullable
    public String getFailureStack() {
        return failureStack;
    }

    @Nullable
    public String getFailureType() {
        return failureType;
    }

    public TestResult withFailureType(String failureType) {
        this.failureType = failureType;
        return this;
    }

    @Nullable
    public Duration getDuration() {
        return duration;
    }

    public TestResult withDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    private TestResult withParameters(Map<String, Object> parameters) {
        this.parameters.putAll(parameters);
        return this;
    }

    private TestResult withCause(Throwable cause) {
        this.cause = cause;
        return this;
    }

    private TestResult withErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder()
                .append(getClass().getSimpleName())
                .append("[")
                .append("testName=").append(testName);

        if (className != null) {
            builder.append(", className=").append(className);
        }

        if (!parameters.isEmpty()) {
            builder.append(", parameters=[")
                    .append(parameters.entrySet().stream()
                            .map(entry -> entry.getKey() + "=" + entry.getValue())
                            .collect(joining(", ")))
                    .append("]");
        }

        builder.append(", result=").append(result);

        if (cause != null) {
            builder.append(", cause=").append(cause);
        }

        if (errorMessage != null) {
            builder.append(", errorMessage=").append(errorMessage);
        }

        if (failureStack != null) {
            builder.append(", failureStack=").append(failureStack);
        }

        if (failureType != null) {
            builder.append(", failureType=").append(failureType);
        }

        if (nonNull(duration)) {
            builder.append(", duration=").append(duration.toMillis()).append("ms");
        }

        return builder.append("]")
                .toString();
    }

    public String toJson() {
        var builder = new JsonStringBuilder()
                .withObject()
                .withProperty("name", testName);

        if (className != null) {
            builder.withProperty("className", className);
        }

        if (!parameters.isEmpty()) {
            builder.withProperty("parameters")
                   .withArray(parameters);
        }

        builder.withProperty("result", result.name());

        if (cause != null) {
            builder.withPropertyEscaped("cause", cause.toString());
        }

        if (errorMessage != null) {
            builder.withPropertyEscaped("errorMessage", errorMessage);
        }

        if (failureStack != null) {
            builder.withProperty("failureStack", failureStack);
        }

        if (failureType != null) {
            builder.withProperty("failureType", failureType);
        }

        if (nonNull(duration)) {
            builder.withProperty("duration", duration.toMillis());
        }

        return builder.closeObject().toString();
    }

    public String toYaml() {
        var builder = new YamlStringBuilder()
                .withProperty("name", testName);

        if (className != null) {
            builder.withProperty("className", className);
        }

        if (!parameters.isEmpty()) {
            builder.withObject("parameters").withProperties(parameters).closeObject();
        }

        builder.withProperty("result", result.name());

        if (cause != null) {
            builder.withPropertyBlockStyle("cause", cause.toString());
        }

        if (errorMessage != null) {
            builder.withPropertyBlockStyle("errorMessage", errorMessage);
        }

        if (failureStack != null) {
            builder.withProperty("failureStack", failureStack);
        }

        if (failureType != null) {
            builder.withProperty("failureType", failureType);
        }

        if (nonNull(duration)) {
            builder.withProperty("duration", duration.toMillis());
        }

        return builder.toString();
    }

    /**
     * Possible test results
     */
    public enum RESULT {SUCCESS, FAILURE, SKIP}
}
