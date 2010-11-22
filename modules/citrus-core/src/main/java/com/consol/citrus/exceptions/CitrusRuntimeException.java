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

package com.consol.citrus.exceptions;

import java.util.Stack;

/**
 * Basic custom runtime exception for all errors in Citrus
 * 
 * @author Christoph Deppisch
 */
public class CitrusRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Stack<String> failureStack = new Stack<String>();
    
    /**
     * Default constructor.
     */
    public CitrusRuntimeException() {
    }

    /**
     * Constructor using fields.
     * @param message
     */
    public CitrusRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructor using fields.
     * @param cause
     */
    public CitrusRuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor using fields.
     * @param message
     * @param cause
     */
    public CitrusRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        return super.getMessage() + getFailureStackAsString();
    }

    /**
     * Get formatted string representation of failure stack information.
     * @return
     */
    public String getFailureStackAsString() {
        StringBuilder builder = new StringBuilder();
        
        for (String failureStackElement : getFailureStack()) {
            builder.append("\n\t" + failureStackElement);
        }
        
        return builder.toString();
    }

    /**
     * Sets the custom failure stack holding line number information inside test case.
     * @param failureStack
     */
    public void setFailureStack(Stack<String> failureStack) {
        this.failureStack = failureStack;
    }

    /**
     * Gets the custom failure stack with line number information where the testcase failed.
     * @return the failureStack
     */
    public Stack<String> getFailureStack() {
        return failureStack;
    }

}
