/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.exceptions;

/**
 * Throw this exception in case a unknown variable is read from test context.
 * 
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 10.08.2009
 *
 */
public class NoSuchVariableException extends CitrusRuntimeException {

    private static final long serialVersionUID = 1L;

    public NoSuchVariableException() {
        super();
    }

    public NoSuchVariableException(String message) {
        super(message);
    }

    public NoSuchVariableException(Throwable cause) {
        super(cause);
    }

    public NoSuchVariableException(String message, Throwable cause) {
        super(message, cause);
    }
}
