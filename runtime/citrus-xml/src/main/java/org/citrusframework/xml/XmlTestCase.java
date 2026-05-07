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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.CitrusSettings;
import org.citrusframework.DefaultTestCase;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.variable.VariableUtils;
import org.citrusframework.xml.actions.script.ScriptDefinitionType;
import org.w3c.dom.Node;

@XmlRootElement(name = "test")
public class XmlTestCase {

    private final DefaultTestCase delegate = new DefaultTestCase();

    /**
     * Gets the test case.
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
            if (StringUtils.hasText(endpoint.getUri())) {
                delegate.getEndpointDefinitions().add(endpoint.getUri());
            } else {
                Map<String, String> endpointProperties = endpoint.getProperties().stream()
                        .collect(Collectors.toMap(Endpoints.Endpoint.Property::getName, Endpoints.Endpoint.Property::getValue));

                if (StringUtils.hasText(endpoint.getName())) {
                    endpointProperties.put(EndpointComponent.ENDPOINT_NAME, endpoint.getName());
                }

                if (endpoint.isAutoClose() != CitrusSettings.isAutoCloseDynamicEndpoints()) {
                    endpointProperties.put(EndpointComponent.AUTO_CLOSE, String.valueOf(endpoint.isAutoClose()));
                }

                if (endpoint.isAutoRemove() != CitrusSettings.isAutoRemoveDynamicEndpoints()) {
                    endpointProperties.put(EndpointComponent.AUTO_REMOVE, String.valueOf(endpoint.isAutoRemove()));
                }

                if (endpointProperties.isEmpty()) {
                    delegate.getEndpointDefinitions().add(endpoint.getType());
                } else {
                    delegate.getEndpointDefinitions().add("%s?%s".formatted(endpoint.getType(),
                            endpointProperties.entrySet()
                                    .stream()
                                    .map(prop -> "%s=%s".formatted(prop.getKey(), prop.getValue()))
                                    .collect(Collectors.joining("&"))));
                }
            }
        });

        for (EndpointBuilder<?> endpointBuilder : endpoints.getEndpointBuilders()) {
            delegate.getEndpoints().add(endpointBuilder);
        }
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
            "endpoints",
            "anyEndpoints"
    })
    public static class Endpoints {

        /** Unmarshaller cache filled with instances created for custom endpoint Xml element refs */
        private static final Map<String, Unmarshaller> UNMARSHALLER_CACHE = new HashMap<>();

        @XmlElement(name = "endpoint", required = true)
        protected List<Endpoint> endpoints;

        @XmlAnyElement(lax = true)
        private List<Object> anyEndpoints;

        public List<Endpoint> getEndpoints() {
            if (endpoints == null) {
                endpoints = new ArrayList<>();
            }
            return this.endpoints;
        }

        public void setEndpoints(List<Endpoint> endpoints) {
            this.endpoints = endpoints;
        }

        public List<Object> getAnyEndpoints() {
            if (anyEndpoints == null) {
                anyEndpoints = new ArrayList<>();
            }
            return this.anyEndpoints;
        }

        public void setAnyEndpoints(List<Object> anyEndpoints) {
            this.anyEndpoints = anyEndpoints;
        }

        public List<EndpointBuilder<?>> getEndpointBuilders() {
            List<EndpointBuilder<?>> builders = new ArrayList<>();

            for (Object object : getAnyEndpoints()) {
                Object endpoint = object;

                if (object instanceof JAXBElement) {
                    endpoint = ((JAXBElement<?>) object).getValue();
                }

                if (object instanceof Node node) {
                    Optional<EndpointBuilder<?>> builder = EndpointBuilder.lookup(node.getLocalName());
                    if (builder.isPresent()) {
                        try {
                            Unmarshaller unmarshaller;
                            if (UNMARSHALLER_CACHE.containsKey(builder.get().getClass().getName())) {
                                unmarshaller = UNMARSHALLER_CACHE.get(builder.get().getClass().getName());
                            } else {
                                unmarshaller = JAXBContext.newInstance(builder.get().getClass()).createUnmarshaller();
                                UNMARSHALLER_CACHE.put(builder.get().getClass().getName(), unmarshaller);
                            }

                            endpoint = unmarshaller.unmarshal(node, builder.get().getClass()).getValue();
                        } catch (JAXBException e) {
                            throw new CitrusRuntimeException("Failed to create XMLTestLoader instance", e);
                        }
                    }
                }

                if (endpoint instanceof EndpointBuilder<?>) {
                    builders.add((EndpointBuilder<?>) endpoint);
                }
            }

            return builders;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "properties"
        })
        public static class Endpoint {

            @XmlAttribute(name = "type")
            protected String type;
            @XmlAttribute
            protected String name;
            @XmlAttribute
            protected String uri;
            @XmlAttribute(name = "auto-close")
            protected boolean autoClose;
            @XmlAttribute(name = "auto-remove")
            protected boolean autoRemove;
            @XmlElement(name = "property")
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

            public String getUri() {
                return uri;
            }

            public void setUri(String uri) {
                this.uri = uri;
            }

            public void setAutoClose(boolean autoClose) {
                this.autoClose = autoClose;
            }

            public boolean isAutoClose() {
                return autoClose;
            }

            public void setAutoRemove(boolean autoRemove) {
                this.autoRemove = autoRemove;
            }

            public boolean isAutoRemove() {
                return autoRemove;
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

                @XmlAttribute(required = true)
                protected String name;
                @XmlAttribute(required = true)
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
