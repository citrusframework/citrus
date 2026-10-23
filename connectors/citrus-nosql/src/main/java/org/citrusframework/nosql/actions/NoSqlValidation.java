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

import java.util.Objects;

import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;

/**
 * A single result validation. The expected value may be a Citrus validation matcher expression.
 */
public record NoSqlValidation(String path, String expected) {

    public NoSqlValidation {
        Objects.requireNonNull(path, "Validation path must not be null");
        Objects.requireNonNull(expected, "Expected value must not be null");
        if (path.isBlank()) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "NoSQL validation path must not be blank");
        }
    }
}
