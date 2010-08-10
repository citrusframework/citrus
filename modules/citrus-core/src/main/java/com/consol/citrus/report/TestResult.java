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

package com.consol.citrus.report;

import org.springframework.util.StringUtils;


/**
 * Class representing test results (failed, successful, skipped)
 * 
 * @author Christoph Deppisch
 */
public class TestResult {

    /** Possible test results */
    public static enum RESULT {SUCCESS, FAILURE, SKIP};

    /** Actual result */
    private RESULT result;
    
    /** Name of the test */
    private String testName;
    
    /** Failure cause */
    private Throwable cause;
    
    /**
     * Constructor using fields.
     * @param name
     * @param result
     */
    public TestResult(String name, RESULT result) {
        this.testName = name;
        this.result = result;
    }

    /**
     * Constructor using fields.
     * @param name
     * @param result
     * @param cause
     */
    public TestResult(String name, RESULT result, Throwable cause) {
        this.testName = name;
        this.result = result;
        this.cause = cause;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(" " + testName + " ");

        int spaces = 62 - testName.length();
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
                
                if(cause!= null && StringUtils.hasText(cause.getLocalizedMessage())) {
                    builder.append("\n FAILED! Caused by: \n " + cause.getClass().getName() + ": " +  cause.getLocalizedMessage());
                } else {
                    builder.append("\n FAILED! Caused by: Unknown error");
                }
                
                builder.append("\n");
                
                break;
            default:
                break;
        }

        return builder.toString();
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
    public RESULT getResult() {
        return result;
    }

    /**
     * Setter for the test result.
     * @param result the result to set
     */
    public void setResult(RESULT result) {
        this.result = result;
    }
}
