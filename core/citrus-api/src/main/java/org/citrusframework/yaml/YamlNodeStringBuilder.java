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

package org.citrusframework.yaml;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interface defines all string building methods that a YAML String builder should implement.
 */
public interface YamlNodeStringBuilder {

    StringBuilder getBuilder();

    YamlNodeStringBuilder applyIndent();

    YamlNodeStringBuilder append(String yaml);

    default YamlNodeStringBuilder append(List<String> items) {
        items.forEach(this::append);
        return this;
    }

    YamlNodeStringBuilder withObject(String name);
    YamlNodeStringBuilder closeObject();

    default YamlNodeStringBuilder withProperty(String name, String value) {
        applyIndent();
        if (value.contains("\"")) {
            getBuilder().append(name).append(": |\n");
            applyIndent();
            getBuilder().append("  ").append(value).append("\n");
        } else {
            getBuilder().append(name).append(": \"").append(value).append("\"\n");
        }
        return this;
    }

    default YamlNodeStringBuilder withProperty(String name, long value) {
        applyIndent();
        getBuilder().append(name).append(": ").append(value).append("\n");
        return this;
    }

    default YamlNodeStringBuilder withPropertyBlockStyle(String name, String value) {
        applyIndent();
        getBuilder().append(name).append(": |\n");
        applyIndent();
        getBuilder().append("  ").append(value).append("\n");
        return this;
    }

    default YamlNodeStringBuilder withProperties(Map<String, Object> items) {
        items.forEach((key, value) -> {
            withProperty("name", key);
            withProperty("value", String.valueOf(value));
        });
        return this;
    }

    default YamlNodeStringBuilder withArray(String name, List<String> items) {
        applyIndent();

        if (items.isEmpty()) {
            getBuilder().append(name).append(": []\n");
        } else {
            getBuilder().append(name).append(": ").append("[ %s ]".formatted(items.stream().map("'%s'"::formatted).collect(Collectors.joining(",")))).append("\n");
        }
        return this;
    }

    YamlNodeStringBuilder withArray();
    YamlNodeStringBuilder closeArray();
}
