/*
 * Copyright the original author or authors.
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
 * In case no function library exists for a given prefix this exception is thrown.
 *
 */
public class NoSuchValidationMatcherLibraryException extends CitrusRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public NoSuchValidationMatcherLibraryException() {
        super();
    }

    /**
     * Constructor using fields.
     * @param message
     */
    public NoSuchValidationMatcherLibraryException(String message) {
        super(message);
    }

    /**
     * Constructor using fields.
     * @param cause
     */
    public NoSuchValidationMatcherLibraryException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor using fields.
     * @param message
     * @param cause
     */
    public NoSuchValidationMatcherLibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
