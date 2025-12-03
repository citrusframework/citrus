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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class YamlStringBuilder  implements YamlNodeStringBuilder {

    private final int initialIndent;
    private final StringBuilder builder = new StringBuilder();

    public YamlStringBuilder(int initialIndent) {
        this.initialIndent = initialIndent;
    }

    public YamlStringBuilder() {
        initialIndent = 0;
    }

    @Override
    public StringBuilder getBuilder() {
        return builder;
    }

    @Override
    public YamlStringBuilder append(String yaml) {
        builder.append(yaml);
        return this;
    }

    @Override
    public YamlNodeStringBuilder applyIndent() {
        builder.append(" ".repeat(initialIndent));
        return this;
    }

    @Override
    public YamlNodeStringBuilder withArray() {
        return new YamlArrayBuilder(this);
    }

    @Override
    public YamlNodeStringBuilder closeArray() {
        return this;
    }

    @Override
    public YamlNodeStringBuilder withObject(String name) {
        applyIndent();
        builder.append(name).append(":\n");
        return new YamlObjectBuilder(this);
    }

    @Override
    public YamlNodeStringBuilder closeObject() {
        return this;
    }

    public String prettyPrint() {
        return builder.toString();
    }

    public class YamlObjectBuilder implements YamlNodeStringBuilder {
        private final YamlNodeStringBuilder parent;

        public YamlObjectBuilder(YamlNodeStringBuilder parent) {
            this.parent = parent;
        }

        @Override
        public StringBuilder getBuilder() {
            return parent.getBuilder();
        }

        @Override
        public YamlNodeStringBuilder applyIndent() {
            parent.applyIndent();
            builder.append("  ");
            return this;
        }

        @Override
        public YamlNodeStringBuilder append(String yaml) {
            List<String> lines = Arrays.asList(yaml.split("\n"));
            lines.forEach(line -> {
                applyIndent();
                builder.append(line).append("\n");
            });
            return this;
        }

        @Override
        public YamlNodeStringBuilder withObject(String name) {
            applyIndent();
            builder.append(name).append(":\n");
            return new YamlObjectBuilder(this);
        }

        @Override
        public YamlNodeStringBuilder closeObject() {
            return parent;
        }

        @Override
        public YamlNodeStringBuilder withArray() {
            return new YamlStringBuilder.YamlArrayBuilder(this);
        }

        @Override
        public YamlNodeStringBuilder closeArray() {
            return parent;
        }
    }

    public class YamlArrayBuilder implements YamlNodeStringBuilder {
        private final YamlNodeStringBuilder parent;

        public YamlArrayBuilder(YamlNodeStringBuilder parent) {
            this.parent = parent;
        }

        @Override
        public StringBuilder getBuilder() {
            return parent.getBuilder();
        }

        @Override
        public YamlArrayBuilder applyIndent() {
            parent.applyIndent();
            builder.append("  ");
            return this;
        }

        @Override
        public YamlArrayBuilder append(String yaml) {
            builder.append("- ");
            List<String> lines = Arrays.asList(yaml.split("\n"));
            builder.append(lines.get(0)).append("\n");
            lines.stream().skip(1).forEach(line -> {
                parent.applyIndent();
                builder.append(line).append("\n");
            });
            return this;
        }

        @Override
        public YamlNodeStringBuilder withObject(String name) {
            applyIndent();
            builder.append("- ").append(name).append(":\n");
            return new YamlObjectBuilder(this);
        }

        @Override
        public YamlNodeStringBuilder closeObject() {
            return parent;
        }

        @Override
        public YamlNodeStringBuilder withProperties(Map<String, Object> items) {
            items.forEach((key, value) -> {
                parent.applyIndent();
                builder.append("- name: \"").append(key).append("\"\n");
                applyIndent();
                if (value instanceof String stringValue && stringValue.contains("\"")) {
                    builder.append("value: |\n");
                    applyIndent();
                    builder.append("  ").append(value).append("\n");
                } else {
                    builder.append("value: \"").append(value).append("\"\n");
                }
            });
            return this;
        }

        @Override
        public YamlArrayBuilder withProperty(String name, String value) {
            return this;
        }

        @Override
        public YamlArrayBuilder withProperty(String name, long value) {
            return this;
        }

        @Override
        public YamlArrayBuilder withArray() {
            return new YamlStringBuilder.YamlArrayBuilder(this);
        }

        @Override
        public YamlNodeStringBuilder closeArray() {
            return parent;
        }
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
