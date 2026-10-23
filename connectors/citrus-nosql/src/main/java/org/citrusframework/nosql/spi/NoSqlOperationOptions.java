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

package org.citrusframework.nosql.spi;

import java.util.OptionalInt;

import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;

/**
 * Options of a single operation.
 */
public record NoSqlOperationOptions(OptionalInt maxResults) {

    public NoSqlOperationOptions {
        if (maxResults == null) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "NoSQL operation options must not be null");
        }
    }

    public static NoSqlOperationOptions defaults() {
        return new NoSqlOperationOptions(OptionalInt.empty());
    }

    public static NoSqlOperationOptions maxResults(int maxResults) {
        return new NoSqlOperationOptions(OptionalInt.of(maxResults));
    }
}
