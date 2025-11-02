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

package org.citrusframework.jbang.yaml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.jbang.actions.JBangAction;
import org.citrusframework.yaml.SchemaProperty;

public class JBang implements TestActionBuilder<JBangAction> {

    private final JBangAction.Builder builder = new JBangAction.Builder();

    private List<Argument> arguments;
    private List<SystemProperty> systemProperties;

    @SchemaProperty(advanced = true, description = "Test action description printed when the action is executed.")
    public void setDescription(String value) {
        builder.description(value);
    }

    @SchemaProperty
    public void setApp(String name) {
        builder.app(name);
    }

    @SchemaProperty
    public void setCommand(String command) {
        builder.command(command);
    }

    @SchemaProperty
    public void setFile(String path) {
        builder.file(path);
    }

    @SchemaProperty
    public void setExitCode(String codes) {
        builder.exitCodes(Arrays.stream(codes.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .mapToInt(Integer::intValue).toArray());
    }

    @SchemaProperty
    public void setPrintOutput(boolean enabled) {
        builder.printOutput(enabled);
    }

    @SchemaProperty
    public void setOutput(String expected) {
        builder.verifyOutput(expected);
    }

    @SchemaProperty
    public void setSavePid(String variable) {
        builder.savePid(variable);
    }

    @SchemaProperty
    public void setSaveOutput(String variable) {
        builder.saveOutput(variable);
    }

    public List<Argument> getArguments() {
        if (arguments == null) {
            arguments = new ArrayList<>();
        }
        return this.arguments;
    }

    @SchemaProperty
    public void setArgs(List<Argument> arguments) {
        this.arguments = arguments;
    }

    public List<SystemProperty> getSystemProperties() {
        if (systemProperties == null) {
            systemProperties = new ArrayList<>();
        }
        return this.systemProperties;
    }

    @SchemaProperty
    public void setSystemProperties(List<SystemProperty> systemProperties) {
        this.systemProperties = systemProperties;
    }

    @Override
    public JBangAction build() {
        for (Argument argument : getArguments()) {
            if (argument.getName() != null) {
                builder.arg(argument.getName(), argument.getValue());
            } else {
                builder.arg(argument.getValue());
            }
        }

        for (SystemProperty sysProp : getSystemProperties()) {
            builder.systemProperty(sysProp.getName(), sysProp.getValue());
        }

        return builder.build();
    }

    public static class Argument {
        private String name;

        private String value;

        public String getName() {
            return name;
        }

        @SchemaProperty(required = true)
        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(required = true)
        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class SystemProperty {
        private String name;

        private String value;

        public String getName() {
            return name;
        }

        @SchemaProperty(required = true)
        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(required = true)
        public void setValue(String value) {
            this.value = value;
        }
    }
}
