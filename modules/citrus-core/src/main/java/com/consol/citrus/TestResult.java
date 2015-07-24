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

import org.springframework.util.StringUtils;

import java.util.*;


/**
 * Class representing test results (failed, successful, skipped)
 * 
 * @author Christoph Deppisch
 */
public final class TestResult {

    /** Possible test results */
    private static enum RESULT {SUCCESS, FAILURE, SKIP};

    /** Actual result */
    private RESULT result;
    
    /** Name of the test */
    private String testName;
    
    /** Optional test parameters */
    private Map<String, Object> parameters;
    
    /** Failure cause */
    private Throwable cause;

    /**
     * Create new test result for successful execution.
     * @param name
     * @return
     */
    public static TestResult success(String name) {
        return new TestResult(name, RESULT.SUCCESS);
    }

    /**
     * Create new test result with parameters for successful execution.
     * @param name
     * @param parameters
     * @return
     */
    public static TestResult success(String name, Map<String, Object> parameters) {
        return new TestResult(name, RESULT.SUCCESS, parameters);
    }

    /**
     * Create new test result for skipped test.
     * @param name
     * @return
     */
    public static TestResult skipped(String name) {
        return new TestResult(name, RESULT.SKIP);
    }

    /**
     * Create new test result with parameters for skipped test.
     * @param name
     * @param parameters
     * @return
     */
    public static TestResult skipped(String name, Map<String, Object> parameters) {
        return new TestResult(name, RESULT.SKIP, parameters);
    }

    /**
     * Create new test result for failed execution.
     * @param name
     * @param cause
     * @return
     */
    public static TestResult failed(String name, Throwable cause) {
        return new TestResult(name, RESULT.FAILURE, cause);
    }

    /**
     * Create new test result with parameters for failed execution.
     * @param name
     * @param cause
     * @param parameters
     * @return
     */
    public static TestResult failed(String name, Throwable cause, Map<String, Object> parameters) {
        return new TestResult(name, RESULT.FAILURE, cause, parameters);
    }

    /**
     * Constructor using fields.
     * @param name
     * @param result
     */
    private TestResult(String name, RESULT result) {
        this(name, result, new HashMap<String, Object>());
    }
    
    /**
     * Constructor using fields.
     * @param name
     * @param result
     * @param parameters
     */
    private TestResult(String name, RESULT result, Map<String, Object> parameters) {
        this(name, result, null, parameters);
    }

    /**
     * Constructor using fields.
     * @param name
     * @param result
     * @param cause
     */
    private TestResult(String name, RESULT result, Throwable cause) {
        this(name, result, cause, new HashMap<String, Object>());
    }

    /**
     * Constructor using fields.
     * @param name
     * @param result
     * @param cause
     */
    private TestResult(String name, RESULT result, Throwable cause, Map<String, Object> parameters) {
        this.testName = name;
        this.result = result;
        this.cause = cause;
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (parameters != null && parameters.size() > 0) {
            builder.append(" " + testName + "(" + StringUtils.collectionToCommaDelimitedString(Arrays.asList(parameters.values())) + ") ");
        } else {
            builder.append(" " + testName + " ");
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
     * Provide failure cause message for test results.
     * @return
     */
    public String getFailureCause() {
        if (cause != null && StringUtils.hasText(cause.getLocalizedMessage())) {
            return " FAILURE: Caused by: " + cause.getClass().getSimpleName() + ": " +  cause.getLocalizedMessage();
        } else {
            return " FAILURE: Caused by: Unknown error";
        }
    }

    /**
     * Checks successful result state.
     * @return
     */
    public boolean isSuccess() {
        return !isSkipped() && result.equals(RESULT.SUCCESS);
    }

    /**
     * Checks failed result state.
     * @return
     */
    public boolean isFailed() {
        return !isSkipped() && result.equals(RESULT.FAILURE);
    }

    /**
     * Checks skipped result state.
     * @return
     */
    public boolean isSkipped() {
        return result.equals(RESULT.SKIP);
    }

    /**
     * Setter for the failure cause.
     * @param cause the cause to set
     */
    public void setCause(Throwable cause) {
        this.cause = cause;
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
     * Setter for the test name.
     * @param testName the testName to set
     */
    public void setTestName(String testName) {
        this.testName = testName;
    }

    /**
     * Getter for test result.
     * @return the result
     */
    public String getResult() {
        return result.name();
    }

    /**
     * Setter for the test result.
     * @param result the result to set
     */
    public void setResult(RESULT result) {
        this.result = result;
    }

    /**
     * Sets the parameters.
     * @param parameters the parameters to set
     */
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * Gets the parameters.
     * @return the parameters
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }
}
