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
import java.util.stream.Collectors;

import org.citrusframework.message.MessagePayloadUtils;

/**
 * String builder able to generate proper Json Strings from pure String concatenation.
 */
public class JsonStringBuilder implements JsonNodeStringBuilder {

    private final StringBuilder builder = new StringBuilder();

    public JsonStringBuilder() {}

    @Override
    public JsonStringBuilder append(String json) {
        builder.append(json);
        return this;
    }

    @Override
    public JsonNodeStringBuilder withObject() {
        builder.append("{ ");
        return new JsonObjectBuilder(this);
    }

    @Override
    public JsonNodeStringBuilder closeObject() {
        builder.append(" }");
        return this;
    }

    @Override
    public JsonNodeStringBuilder withArray(List<String> items) {
        if (items.isEmpty()) {
            builder.append("[]");
        } else {
            builder.append("[ %s ]".formatted(items.stream().map("\"%s\""::formatted).collect(Collectors.joining(","))));
        }

        return this;
    }

    @Override
    public JsonNodeStringBuilder withArray() {
        builder.append("[ ");
        return this;
    }

    @Override
    public JsonNodeStringBuilder closeArray() {
        builder.append(" ]");
        return this;
    }

    @Override
    public JsonNodeStringBuilder withProperty(String name) {
        return this;
    }

    @Override
    public JsonNodeStringBuilder withProperty(String name, long value) {
        return this;
    }

    @Override
    public JsonNodeStringBuilder withProperty(String name, String value) {
        return this;
    }

    public String prettyPrint() {
        return MessagePayloadUtils.prettyPrintJson(builder.toString());
    }

    public class JsonObjectBuilder implements JsonNodeStringBuilder {
        boolean hasProperties = false;

        private final JsonNodeStringBuilder parent;

        public JsonObjectBuilder(JsonNodeStringBuilder parent) {
            this.parent = parent;
        }

        @Override
        public JsonNodeStringBuilder append(String json) {
            builder.append(json);
            return this;
        }

        @Override
        public JsonNodeStringBuilder withProperty(String name) {
            if (hasProperties) {
                builder.append(", ");
            }

            builder.append("\"%s\": ".formatted(name));
            hasProperties = true;
            return this;
        }

        @Override
        public JsonNodeStringBuilder withProperty(String name, String value) {
            if (hasProperties) {
                builder.append(", ");
            }

            builder.append("\"%s\": \"".formatted(name)).append(value).append("\"");
            hasProperties = true;
            return this;
        }

        @Override
        public JsonNodeStringBuilder withProperty(String name, long value) {
            if (hasProperties) {
                builder.append(", ");
            }

            builder.append("\"%s\": ".formatted(name)).append(value);
            hasProperties = true;
            return this;
        }

        @Override
        public JsonNodeStringBuilder withObject() {
            builder.append("{ ");
            return new JsonObjectBuilder(this);
        }

        @Override
        public JsonNodeStringBuilder closeObject() {
            builder.append(" }");
            return parent;
        }

        @Override
        public JsonNodeStringBuilder withArray(List<String> items) {
            if (items.isEmpty()) {
                builder.append("[]");
            } else {
                builder.append("[ %s ]".formatted(items.stream().map("\"%s\""::formatted).collect(Collectors.joining(","))));
            }

            return this;
        }

        @Override
        public JsonNodeStringBuilder withArray() {
            builder.append("[ ");
            return new JsonArrayBuilder(this);
        }

        @Override
        public JsonNodeStringBuilder closeArray() {
            builder.append(" ]");
            return parent;
        }
    }

    public class JsonArrayBuilder implements JsonNodeStringBuilder {
        boolean hasItems = false;

        private final JsonNodeStringBuilder parent;

        public JsonArrayBuilder(JsonNodeStringBuilder parent) {
            this.parent = parent;
        }

        @Override
        public JsonNodeStringBuilder append(String json) {
            builder.append(json);
            return this;
        }

        @Override
        public JsonNodeStringBuilder withProperty(String name) {
            return this;
        }

        @Override
        public JsonNodeStringBuilder withProperty(String name, String value) {
            return this;
        }

        @Override
        public JsonNodeStringBuilder withProperty(String name, long value) {
            return this;
        }

        @Override
        public JsonNodeStringBuilder withObject() {
            if (hasItems) {
                builder.append(", ");
            }

            builder.append("{ ");
            hasItems = true;
            return new JsonObjectBuilder(this);
        }

        @Override
        public JsonNodeStringBuilder closeObject() {
            builder.append(" }");
            return parent;
        }

        @Override
        public JsonNodeStringBuilder withArray(List<String> items) {
            if (hasItems) {
                builder.append(", ");
            }

            if (items.isEmpty()) {
                builder.append("[]");
            } else {
                builder.append("[ %s ]".formatted(items.stream().map("\"%s\""::formatted).collect(Collectors.joining(","))));
            }

            hasItems = true;
            return this;
        }

        @Override
        public JsonNodeStringBuilder withArray() {
            if (hasItems) {
                builder.append(", ");
            }

            builder.append("[ ");
            hasItems = true;
            return new JsonArrayBuilder(this);
        }

        @Override
        public JsonNodeStringBuilder closeArray() {
            builder.append(" ]");
            return parent;
        }
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
