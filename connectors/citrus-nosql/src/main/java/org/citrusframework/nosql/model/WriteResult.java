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
 * Result of a write operation. Counters are optional: a counter that the provider does not report
 * stays absent and is never defaulted to zero.
 */
public record WriteResult(OptionalLong matched,
                          OptionalLong modified,
                          OptionalLong inserted,
                          OptionalLong deleted,
                          List<NoSqlValue> generatedIds) implements NoSqlResult {

    public WriteResult {
        Objects.requireNonNull(matched, "Matched count must not be null");
        Objects.requireNonNull(modified, "Modified count must not be null");
        Objects.requireNonNull(inserted, "Inserted count must not be null");
        Objects.requireNonNull(deleted, "Deleted count must not be null");
        Objects.requireNonNull(generatedIds, "Generated ids must not be null");
        generatedIds = List.copyOf(generatedIds);
    }

    public static WriteResult inserted(long inserted, List<NoSqlValue> generatedIds) {
        return new WriteResult(OptionalLong.empty(), OptionalLong.empty(), OptionalLong.of(inserted),
                OptionalLong.empty(), generatedIds);
    }

    public static WriteResult updated(long matched, long modified) {
        return new WriteResult(OptionalLong.of(matched), OptionalLong.of(modified),
                OptionalLong.empty(), OptionalLong.empty(), List.of());
    }

    public static WriteResult updated(long matched, long modified, List<NoSqlValue> generatedIds) {
        return new WriteResult(OptionalLong.of(matched), OptionalLong.of(modified),
                OptionalLong.empty(), OptionalLong.empty(), generatedIds);
    }

    public static WriteResult deleted(long deleted) {
        return new WriteResult(OptionalLong.empty(), OptionalLong.empty(),
                OptionalLong.empty(), OptionalLong.of(deleted), List.of());
    }

    public static WriteResult empty() {
        return new WriteResult(OptionalLong.empty(), OptionalLong.empty(), OptionalLong.empty(),
                OptionalLong.empty(), List.of());
    }
}
