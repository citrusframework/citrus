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

package org.citrusframework.jbang.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.jbang.actions.JBangAction;

@XmlRootElement(name = "jbang")
public class JBang implements TestActionBuilder<JBangAction> {

    private final JBangAction.Builder builder = new JBangAction.Builder();

    @XmlElement
    public JBang setDescription(String value) {
        builder.description(value);
        return this;
    }

    @XmlAttribute
    public JBang setApp(String name) {
        builder.app(name);
        return this;
    }

    @XmlAttribute
    public JBang setCommand(String command) {
        builder.command(command);
        return this;
    }

    @XmlAttribute
    public JBang setFile(String path) {
        builder.file(path);
        return this;
    }

    @XmlAttribute
    public JBang setArgs(String args) {
        builder.args(args.split(","));
        return this;
    }

    @XmlAttribute(name = "exit-code")
    public JBang setExitCode(String codes) {
        builder.exitCodes(Arrays.stream(codes.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .mapToInt(Integer::intValue).toArray());
        return this;
    }

    @XmlAttribute(name = "print-output")
    public JBang setPrintOutput(boolean enabled) {
        builder.printOutput(enabled);
        return this;
    }

    @XmlElement
    public JBang setOutput(String expected) {
        builder.verifyOutput(expected);
        return this;
    }

    @XmlAttribute(name = "save-pid")
    public JBang setSavePid(String variable) {
        builder.savePid(variable);
        return this;
    }

    @XmlAttribute(name = "save-output")
    public JBang setSaveOutput(String variable) {
        builder.saveOutput(variable);
        return this;
    }

    @XmlElement(name = "args")
    public JBang setArguments(Arguments arguments) {
        for (Arguments.Argument argument : arguments.getArguments()) {
            if (argument.getName() != null) {
                builder.arg(argument.getName(), argument.getValue());
            } else {
                builder.arg(argument.getValue());
            }
        }

        return this;
    }

    @XmlElement(name = "system-properties")
    public JBang setSystemProperties(SystemProperties systemProperties) {
        for (SystemProperties.SystemProperty sysProp : systemProperties.getSystemProperties()) {
            builder.systemProperty(sysProp.getName(), sysProp.getValue());
        }

        return this;
    }

    @Override
    public JBangAction build() {
        return builder.build();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "arguments"
    })
    public static class Arguments {

        @XmlElement(name = "arg")
        private List<Argument> arguments;

        public List<Argument> getArguments() {
            if (arguments == null) {
                arguments = new ArrayList<>();
            }
            return this.arguments;
        }

        public void setArguments(List<Argument> arguments) {
            this.arguments = arguments;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Argument {
            @XmlAttribute
            private String name;

            @XmlAttribute(required = true)
            private String value;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "systemProperties"
    })
    public static class SystemProperties {

        @XmlElement(name = "system-property")
        private List<SystemProperty> systemProperties;

        public List<SystemProperty> getSystemProperties() {
            if (systemProperties == null) {
                systemProperties = new ArrayList<>();
            }
            return this.systemProperties;
        }

        public void setSystemProperties(List<SystemProperty> systemProperties) {
            this.systemProperties = systemProperties;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class SystemProperty {
            @XmlAttribute(required = true)
            private String name;

            @XmlAttribute(required = true)
            private String value;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }
}
