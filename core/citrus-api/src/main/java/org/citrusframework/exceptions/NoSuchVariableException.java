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

package org.citrusframework.exceptions;

/**
 * Throw this exception in case an unknown variable is read from test context.
 * 
 * @author Christoph Deppisch
 * @since 2009
 *
 */
public class NoSuchVariableException extends CitrusRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public NoSuchVariableException() {
        super();
    }

    /**
     * Constructor using fields.
     * @param message
     */
    public NoSuchVariableException(String message) {
        super(message);
    }

    /**
     * Constructor using fields.
     * @param cause
     */
    public NoSuchVariableException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor using fields.
     * @param message
     * @param cause
     */
    public NoSuchVariableException(String message, Throwable cause) {
        super(message, cause);
    }
}
