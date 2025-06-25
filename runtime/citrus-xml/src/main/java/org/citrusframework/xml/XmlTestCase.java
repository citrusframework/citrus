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

package org.citrusframework.xml;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import org.citrusframework.DefaultTestCase;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.FileUtils;
import org.citrusframework.variable.VariableUtils;
import org.citrusframework.xml.actions.script.ScriptDefinitionType;

@XmlRootElement(name = "test")
public class XmlTestCase {

    private final DefaultTestCase delegate = new DefaultTestCase();

    /**
     * Gets the test case.
     * @return
     */
    public TestCase getTestCase() {
        return delegate;
    }

    @XmlAttribute
    public void setName(String name) {
        delegate.setName(name);
    }

    @XmlAttribute
    public void setAuthor(String author) {
        delegate.getMetaInfo().setAuthor(author);
    }

    @XmlAttribute
    public void setStatus(TestCaseMetaInfo.Status status) {
        delegate.getMetaInfo().setStatus(status);
    }

    @XmlElement
    public void setVariables(Variables variables) {
        variables.getVariables().forEach(variable -> {
            if (variable.multilineValue != null) {
                if (variable.multilineValue.script != null) {
                    if (variable.multilineValue.script.getFile() != null) {
                        try {
                            delegate.getVariableDefinitions().put(variable.name, VariableUtils.getValueFromScript(variable.multilineValue.script.getType(),
                                    FileUtils.readToString(FileUtils.getFileResource(variable.multilineValue.script.getFile()),
                                            Optional.ofNullable(variable.multilineValue.script.getCharset()).map(Charset::forName).orElseGet(FileUtils::getDefaultCharset))));
                        } catch (IOException e) {
                            throw new CitrusRuntimeException("Failed to read script file resource", e);
                        }
                    } else {
                        delegate.getVariableDefinitions().put(variable.name, VariableUtils.getValueFromScript(variable.multilineValue.script.getType(), variable.multilineValue.script.getValue()));
                    }
                } else {
                    delegate.getVariableDefinitions().put(variable.name, variable.multilineValue.data);
                }
            } else {
                delegate.getVariableDefinitions().put(variable.name, variable.value);
            }
        });
    }

    @XmlElement
    public void setEndpoints(Endpoints endpoints) {
        endpoints.getEndpoints().forEach(endpoint -> {
            if (endpoint.getProperties().isEmpty()) {
                delegate.getEndpointDefinitions().add(endpoint.getType());
            } else {
                delegate.getEndpointDefinitions().add("%s?%s".formatted(endpoint.getType(),
                        endpoint.getProperties()
                                .stream()
                                .map(prop -> "%s=%s".formatted(prop.getName(), prop.getValue()))
                                .collect(Collectors.joining("&"))));
            }
        });
    }

    @XmlElement
    public void setActions(TestActions actions) {
        actions.getActionBuilders()
                .forEach(delegate::addTestAction);
    }

    @XmlElement
    public void setFinally(TestActions actions) {
        actions.getActionBuilders().forEach(delegate::addFinalAction);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "variables"
    })
    public static class Variables {

        @XmlElement(name = "variable", required = true)
        protected List<Variable> variables;

        public List<Variable> getVariables() {
            if (variables == null) {
                variables = new ArrayList<>();
            }
            return this.variables;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "multilineValue"
        })
        public static class Variable {

            @XmlElement(name = "value")
            protected Variable.Value multilineValue;
            @XmlAttribute(name = "name", required = true)
            protected String name;
            @XmlAttribute(name = "value")
            protected String value;

            public Variable.Value getMultilineValue() {
                return multilineValue;
            }

            public void setMultilineValue(Variable.Value value) {
                this.multilineValue = value;
            }

            public String getName() {
                return name;
            }

            public void setName(String value) {
                this.name = value;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                    "script",
                    "data"
            })
            public static class Value {

                protected ScriptDefinitionType script;
                protected String data;

                public ScriptDefinitionType getScript() {
                    return script;
                }

                public void setScript(ScriptDefinitionType value) {
                    this.script = value;
                }

                public String getData() {
                    return data;
                }

                public void setData(String value) {
                    this.data = value;
                }
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "endpoints"
    })
    public static class Endpoints {

        @XmlElement(name = "endpoint", required = true)
        protected List<Endpoint> endpoints;

        public List<Endpoint> getEndpoints() {
            if (endpoints == null) {
                endpoints = new ArrayList<>();
            }
            return this.endpoints;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "properties"
        })
        public static class Endpoint {

            @XmlAttribute(name = "type", required = true)
            protected String type;
            @XmlAttribute
            protected String name;
            @XmlElement
            protected List<Property> properties;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getName() {
                return name;
            }

            public void setName(String value) {
                this.name = value;
            }

            public List<Property> getProperties() {
                if (properties == null) {
                    properties = new ArrayList<>();
                }
                return properties;
            }

            public void setProperties(List<Property> properties) {
                this.properties = properties;
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
            })
            public static class Property {

                @XmlAttribute
                protected String name;
                @XmlAttribute
                protected String value;

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
}
