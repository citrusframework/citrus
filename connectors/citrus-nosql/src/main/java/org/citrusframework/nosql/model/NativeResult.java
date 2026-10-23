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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Result of a provider specific native operation. The provider id identifies the origin of the
 * value and metadata carries provider supplied labels such as the command or target.
 */
public record NativeResult(String providerId, NoSqlValue value, Map<String, NoSqlValue> metadata) implements NoSqlResult {

    public NativeResult {
        Objects.requireNonNull(providerId, "Provider id must not be null");
        Objects.requireNonNull(value, "Native value must not be null");
        Objects.requireNonNull(metadata, "Metadata must not be null");
        metadata = new LinkedHashMap<>(metadata);
        metadata.values().forEach(Objects::requireNonNull);
    }

    @Override
    public Map<String, NoSqlValue> metadata() {
        return java.util.Collections.unmodifiableMap(metadata);
    }

    public static NativeResult of(String providerId, NoSqlValue value) {
        return new NativeResult(providerId, value, Map.of());
    }
}
