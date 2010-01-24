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
 * Thrown in case an element is not found. Usually used in during validation.
 * 
 * @author Christoph Deppisch
 */
public class UnknownElementException extends CitrusRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public UnknownElementException() {
        super();
    }

    /**
     * Constructor using fields.
     * @param message
     */
    public UnknownElementException(String message) {
        super(message);
    }

    /**
     * Constructor using fields.
     * @param cause
     */
    public UnknownElementException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor using fields.
     * @param message
     * @param cause
     */
    public UnknownElementException(String message, Throwable cause) {
        super(message, cause);
    }
}
