/*
 * Copyright 2006-2011 the original author or authors.
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
 * Failure stack element provides access to the detailed failure stack message and 
 * the location in the test case XML where error happened.
 * 
 * @author Christoph Deppisch
 */
public class FailureStackElement {
    /** The name of the failed action */
    private String actionName;
    
    /** Path to XML test file */
    private String testFilePath;
    
    /** Line number in XML test case where error happened */
    private Long lineNumberStart = 0L;
    
    /** Failing action in XML test case ends in this line */
    private Long lineNumberEnd = 0L;

    /**
     * Default constructor using fields.
     * @param testFilePath file path of failed test.
     * @param actionName the failed action name.
     * @param lineNumberStart the line number where error happened.
     */
    public FailureStackElement(String testFilePath, String actionName, Long lineNumberStart) {
        this.testFilePath = testFilePath;
        this.actionName = actionName;
        this.lineNumberStart = lineNumberStart;
    }
    
    /**
     * Constructs the stack trace message.
     * @return the stack trace message.
     */
    public String getStackMessage() {
        if (lineNumberEnd.longValue() > 0 && !lineNumberStart.equals(lineNumberEnd)) {
            return "at " + testFilePath + "(" + actionName + ":" + lineNumberStart + "-" + lineNumberEnd + ")";
        } else {
            return "at " + testFilePath + "(" + actionName + ":" + lineNumberStart + ")";
        }
    }
    
    /**
     * Gets the line number where error happened.
     * @return the line number
     */
    public Long getLineNumberStart() {
        return lineNumberStart;
    }

    /**
     * Sets the line number where failing action ends.
     * @param toLineNumber the toLineNumber to set
     */
    public void setLineNumberEnd(Long toLineNumber) {
        this.lineNumberEnd = toLineNumber;
    }

    /**
     * Gets the line number where failing action ends.
     * @return the toLineNumber
     */
    public Long getLineNumberEnd() {
        return lineNumberEnd;
    }

    /**
     * Gets the test file path for the failed test.
     * @return the testFilePath
     */
    public String getTestFilePath() {
        return testFilePath;
    }
}
