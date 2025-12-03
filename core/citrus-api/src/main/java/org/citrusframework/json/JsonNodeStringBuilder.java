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

package org.citrusframework.json;

import java.util.List;
import java.util.Map;

/**
 * Interface defines all string building methods that a Json String builder should implement.
 */
public interface JsonNodeStringBuilder {

    JsonNodeStringBuilder append(String json);

    JsonNodeStringBuilder withProperty(String name);

    JsonNodeStringBuilder withProperty(String name, String value);

    default JsonNodeStringBuilder withPropertyEscaped(String name, String value) {
        return withProperty(name, value.replace("\"", "\\\""));
    }

    JsonNodeStringBuilder withProperty(String name, long value);

    JsonNodeStringBuilder withObject();
    JsonNodeStringBuilder closeObject();

    JsonNodeStringBuilder withArray(List<String> items);

    default JsonNodeStringBuilder withArray(Map<String, Object> items) {
        JsonNodeStringBuilder builder = withArray();
        items.forEach((key, value) -> {
            builder.withObject()
                    .withProperty("name", key)
                    .withPropertyEscaped("value", String.valueOf(value))
                .closeObject();
        });
        builder.closeArray();
        return this;
    }

    JsonNodeStringBuilder withArray();
    JsonNodeStringBuilder closeArray();
}
