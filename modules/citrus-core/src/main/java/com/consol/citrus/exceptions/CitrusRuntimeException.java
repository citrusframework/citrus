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
