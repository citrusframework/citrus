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

/**
 * Stable error taxonomy of the NoSQL connector. Every failure surfaces exactly one of these
 * classes so that tests and tooling can react to failures without parsing messages.
 */
public enum NoSqlErrorClass {

    /** Missing, unknown or ambiguous client and provider configuration. */
    CONFIGURATION,

    /** The provider does not support the requested operation or native command. */
    UNSUPPORTED_OPERATION,

    /** The payload, target or operation options are invalid. */
    INVALID_PAYLOAD,

    /** The provider could not connect to the database. */
    CONNECTION,

    /** The operation timed out. For writes the outcome is unknown. */
    TIMEOUT,

    /** The provider rejected or failed the operation. */
    EXECUTION,

    /** A result validation failed. */
    VALIDATION,

    /** A result extraction failed. */
    EXTRACTION
}
