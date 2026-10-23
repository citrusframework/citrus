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
 * Extraction of a result value into a test variable.
 */
public record NoSqlExtraction(String path, String variable) {

    public NoSqlExtraction {
        Objects.requireNonNull(path, "Extraction path must not be null");
        Objects.requireNonNull(variable, "Target variable must not be null");
        if (path.isBlank()) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "NoSQL extraction path must not be blank");
        }
        if (variable.isBlank()) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "NoSQL extraction target variable must not be blank");
        }
    }
}
