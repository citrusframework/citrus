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

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.citrusframework.openapi.random.RandomElement.RandomList;
import org.citrusframework.openapi.random.RandomElement.RandomObject;
import org.citrusframework.openapi.random.RandomElement.RandomValue;

import static org.citrusframework.util.StringUtils.trimTrailingComma;

/**
 * Utility class for converting a {@link RandomModelBuilder} to a JSON string representation.
 */
final class RandomModelJsonWriter {

    private RandomModelJsonWriter() {
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
        } else if (object instanceof RandomObject randomObject) {
            appendJsonObject(builder, randomObject);
        } else if (object instanceof RandomList randomList) {
            appendArray(builder, randomList);
        } else if (object instanceof String string) {
            builder.append(string);
        } else if (object instanceof RandomValue randomValue) {
            appendObject(builder, randomValue.getValue());
        }
    }

    private static void appendArray(StringBuilder builder, List<RandomElement> list) {
        builder.append("[");
        list.forEach(listValue -> {
            appendObject(builder, listValue);
            builder.append(",");
        });
        trimTrailingComma(builder);
        builder.append("]");
    }

    private static void appendJsonObject(StringBuilder builder, Map<String, RandomElement> map) {
        builder.append("{");
        for (Entry<String, RandomElement> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            builder.append("\"");
            builder.append(key);
            builder.append("\": ");

            appendObject(builder, value);

            builder.append(",");
        }
        trimTrailingComma(builder);

        builder.append("}");
    }
}
