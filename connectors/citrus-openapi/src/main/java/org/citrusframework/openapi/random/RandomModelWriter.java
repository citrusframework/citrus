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

package org.citrusframework.openapi.random;

import static org.citrusframework.util.StringUtils.trimTrailingComma;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.citrusframework.openapi.random.RandomElement.RandomValue;

/**
 * Utility class for converting a {@link RandomModelBuilder} to its string representation.
 * This class provides static methods to serialize the model built by {@link RandomModelBuilder}.
 */
class RandomModelWriter {

    private RandomModelWriter() {
        // static access only
    }

    static String toString(RandomModelBuilder randomModelBuilder) {

        StringBuilder builder = new StringBuilder();
        appendObject(builder, randomModelBuilder.deque);
        return builder.toString();
    }

    private static void appendObject(StringBuilder builder, Object object) {

        if (object instanceof Deque<?> deque) {
            while (!deque.isEmpty()) {
                appendObject(builder, deque.pop());
            }
            return;
        }
        if (object instanceof Map<?, ?> map) {
            //noinspection unchecked
            appendMap(builder, (Map<String, Object>) map);
        } else if (object instanceof List<?> list) {
            appendArray(builder, list);
        } else if (object instanceof String string) {
            builder.append(string);
        } else if (object instanceof RandomValue randomValue) {
            appendObject(builder, randomValue.getValue());
        }
    }

    private static void appendArray(StringBuilder builder, List<?> list) {
        builder.append("[");
        list.forEach(listValue -> {
            appendObject(builder, listValue);
            builder.append(",");
        });
        trimTrailingComma(builder);
        builder.append("]");
    }

    private static void appendMap(StringBuilder builder, Map<String, Object> map) {
        if (map.size() == 1) {
            Entry<String, Object> entry = map.entrySet().iterator().next();
            String key = entry.getKey();
            Object value = entry.getValue();

            if ("ARRAY".equals(key)) {
                appendObject(builder, value);
            } else if ("NATIVE".equals(key)) {
                builder.append(value);
            } else {
                appendJsonObject(builder, map);
            }
        } else {
            appendJsonObject(builder, map);
        }
    }

    private static void appendJsonObject(StringBuilder builder, Map<String, Object> map) {
        builder.append("{");
        for (Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            builder.append("\"");
            builder.append(key);
            builder.append("\": ");

            if (value instanceof String) {
                builder.append(value);
            } else if (value instanceof Map<?, ?>) {
                appendObject(builder, value);
            } else if (value instanceof RandomValue randomValue) {
                appendObject(builder, randomValue.getValue());
            }

            builder.append(",");
        }
        trimTrailingComma(builder);

        builder.append("}");
    }
}
