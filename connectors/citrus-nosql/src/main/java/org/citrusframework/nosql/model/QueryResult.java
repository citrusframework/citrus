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

package org.citrusframework.nosql.model;

import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;

/**
 * Fully materialized result of a query operation. Document order is the provider order and is
 * preserved as is.
 */
public record QueryResult(List<NoSqlValue> documents, OptionalLong totalCount) implements NoSqlResult {

    public QueryResult {
        Objects.requireNonNull(documents, "Documents must not be null");
        Objects.requireNonNull(totalCount, "Total count must not be null");
        documents = List.copyOf(documents);
    }

    public static QueryResult of(List<NoSqlValue> documents) {
        return new QueryResult(documents, OptionalLong.empty());
    }

    public static QueryResult of(List<NoSqlValue> documents, long totalCount) {
        return new QueryResult(documents, OptionalLong.of(totalCount));
    }
}
