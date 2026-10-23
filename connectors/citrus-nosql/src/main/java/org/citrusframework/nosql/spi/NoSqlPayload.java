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

import java.util.Optional;

import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.nosql.model.NoSqlValue;

/**
 * Provider independent operation payload. A provider decides how to interpret the canonical value,
 * for example as a JSON filter document or as a list of key/value pairs.
 */
public record NoSqlPayload(NoSqlValue value) {

    public NoSqlPayload {
        if (value == null) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "NoSQL payload value must not be null");
        }
    }

    /**
     * Create a payload from raw text, for example a JSON filter document.
     */
    public static NoSqlPayload text(String text) {
        return new NoSqlPayload(NoSqlValue.string(text));
    }

    public static NoSqlPayload of(NoSqlValue value) {
        return new NoSqlPayload(value);
    }

    /**
     * Raw text content when the payload is a text value.
     */
    public Optional<String> textContent() {
        if (value instanceof NoSqlValue.StringValue(String value1)) {
            return Optional.of(value1);
        }

        return Optional.empty();
    }

    /**
     * Text content of the payload. Structured values are rendered as JSON.
     */
    public String content() {
        return value.render();
    }

    public boolean isEmpty() {
        return textContent().map(String::isBlank).orElse(false);
    }
}
