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

package org.citrusframework.nosql.error;

import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Exception raised by the NoSQL connector. Carries a stable {@link NoSqlErrorClass} and, for
 * failed writes, whether the write outcome is unknown because the operation timed out.
 */
public class NoSqlException extends CitrusRuntimeException {

    private final NoSqlErrorClass errorClass;
    private final boolean writeOutcomeUnknown;

    public NoSqlException(NoSqlErrorClass errorClass, String message) {
        this(errorClass, message, null);
    }

    public NoSqlException(NoSqlErrorClass errorClass, String message, Throwable cause) {
        this(errorClass, message, cause, false);
    }

    public NoSqlException(NoSqlErrorClass errorClass, String message, Throwable cause, boolean writeOutcomeUnknown) {
        super(message, cause);
        this.errorClass = errorClass;
        this.writeOutcomeUnknown = writeOutcomeUnknown;
    }

    /**
     * A timeout of a potentially non-idempotent write. The write may or may not have been applied.
     */
    public static NoSqlException writeTimeout(String message, Throwable cause) {
        return new NoSqlException(NoSqlErrorClass.TIMEOUT, message, cause, true);
    }

    public NoSqlErrorClass getErrorClass() {
        return errorClass;
    }

    public boolean isWriteOutcomeUnknown() {
        return writeOutcomeUnknown;
    }
}
