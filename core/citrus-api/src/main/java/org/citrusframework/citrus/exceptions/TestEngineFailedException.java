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

package org.citrusframework.citrus.exceptions;

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
