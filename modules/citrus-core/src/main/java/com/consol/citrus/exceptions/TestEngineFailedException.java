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

/**
 * Custom RuntimeException thrown if test run failed, because some tests were not successful.
 *
 * @author Christoph Deppisch
 * @since 2007
 *
 */
public class TestEngineFailedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public TestEngineFailedException() {
    }

    /**
     * Constructor using fields.
     * @param message
     */
    public TestEngineFailedException(String message) {
        super(message);
    }

    /**
     * Constructor using fields.
     * @param cause
     */
    public TestEngineFailedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor using fields.
     * @param message
     * @param cause
     */
    public TestEngineFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
