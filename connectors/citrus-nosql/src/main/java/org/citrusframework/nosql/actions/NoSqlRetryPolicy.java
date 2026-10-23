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

package org.citrusframework.nosql.actions;

import java.time.Duration;
import java.time.format.DateTimeParseException;

import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;

/**
 * Explicit opt-in retry policy for read operations. Writes are never retried implicitly because a
 * retried write may apply twice.
 */
public record NoSqlRetryPolicy(int maxAttempts, Duration interval) {

    public NoSqlRetryPolicy {
        if (maxAttempts < 1) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "NoSQL retry policy requires at least one attempt");
        }
        if (interval == null || interval.isNegative()) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "NoSQL retry interval must not be negative");
        }

        try {
            interval.toMillis();
        } catch (ArithmeticException e) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "NoSQL retry interval is too large", e);
        }
    }

    public static NoSqlRetryPolicy none() {
        return new NoSqlRetryPolicy(1, Duration.ZERO);
    }

    public static NoSqlRetryPolicy of(int maxAttempts, Duration interval) {
        return new NoSqlRetryPolicy(maxAttempts, interval);
    }

    /**
     * Create a retry policy from a text interval. Accepts an ISO-8601 duration such as
     * {@code PT0.5S} or a plain number of milliseconds.
     */
    public static NoSqlRetryPolicy of(int maxAttempts, String interval) {
        return new NoSqlRetryPolicy(maxAttempts, parseInterval(interval));
    }

    static Duration parseInterval(String value) {
        if (value == null || value.isBlank()) {
            return Duration.ZERO;
        }

        try {
            if (value.chars().allMatch(Character::isDigit)) {
                return Duration.ofMillis(Long.parseLong(value));
            }

            return Duration.parse(value);
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    String.format("Invalid NoSQL retry interval '%s'", value), e);
        }
    }
}
