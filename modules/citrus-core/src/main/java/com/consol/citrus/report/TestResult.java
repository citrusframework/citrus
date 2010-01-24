/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
        StringBuffer buf = new StringBuffer();

        buf.append("  " + testName);

        int spaces = 50 - testName.length();
        for (int i = 0; i < spaces; i++) {
            buf.append(" ");
        }
        
        switch (result) {
            case SUCCESS:
                buf.append(": successful");
                break;
            case SKIP:
                buf.append(": skipped - because excluded from test run");
                break;
            case FAILURE:
                if(cause != null && StringUtils.hasText(cause.getLocalizedMessage())) {
                    buf.append(": failed - with exception: " + cause.getLocalizedMessage());
                } else {
                    buf.append(": failed - No exception available");
                }
                break;
            default:
                break;
        }

        String resultString = buf.toString();
        
        if (resultString.length() > 100) {
            return resultString.substring(0, 100) + " ...";
        } else {
            return resultString;
        }
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
