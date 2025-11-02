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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.citrusframework.DefaultTestCase;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.common.Named;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.variable.VariableUtils;
import org.citrusframework.yaml.actions.script.ScriptDefinitionType;

public class YamlTestCase {

    private final DefaultTestCase delegate = new DefaultTestCase();

    public TestCase getTestCase() {
        return delegate;
    }

    @SchemaProperty(required = true, description = "The test name.")
    public void setName(String name) {
        delegate.setName(name);
    }

    @SchemaProperty(description = "The test description.")
    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    @SchemaProperty(description = "The test author.")
    public void setAuthor(String author) {
        delegate.getMetaInfo().setAuthor(author);
    }

    @SchemaProperty(description = "The test status.")
    public void setStatus(TestCaseMetaInfo.Status status) {
        delegate.getMetaInfo().setStatus(status);
    }

    @SchemaProperty(description = "The test variables.")
    public void setVariables(List<Variable> variables) {
        variables.forEach(variable -> {
            if (variable.script != null) {
                if (variable.script.getFile() != null) {
                    try {
                        delegate.getVariableDefinitions().put(variable.name, VariableUtils.getValueFromScript(variable.script.getType(),
                                FileUtils.readToString(FileUtils.getFileResource(variable.script.getFile()),
                                        Optional.ofNullable(variable.script.getCharset()).map(Charset::forName).orElseGet(FileUtils::getDefaultCharset))));
                    } catch (IOException e) {
                        throw new CitrusRuntimeException("Failed to read script file resource", e);
                    }
                } else {
                    delegate.getVariableDefinitions().put(variable.name, VariableUtils.getValueFromScript(variable.script.getType(), variable.script.getValue()));
                }
            } else {
                delegate.getVariableDefinitions().put(variable.name, variable.value);
            }
        });
    }

    @SchemaProperty(advanced = true, description = "List of endpoints for this test.")
    public void setEndpoints(List<Endpoint> endpoints) {
        endpoints.forEach(endpoint -> {
            if (endpoint.getBuilder() != null) {
                if (StringUtils.hasText(endpoint.getName()) && endpoint.getBuilder() instanceof Named namedBuilder) {
                    namedBuilder.setName(endpoint.getName());
                }

                delegate.getEndpoints().add(endpoint.getBuilder());
            } else if (endpoint.getProperties().isEmpty()) {
                delegate.getEndpointDefinitions().add(endpoint.getType());
            } else {
                delegate.getEndpointDefinitions().add("%s?%s".formatted(endpoint.getType(),
                        endpoint.getProperties().entrySet()
                                .stream()
                                .map(entry -> "%s=%s".formatted(entry.getKey(), entry.getValue()))
                                .collect(Collectors.joining("&"))));
            }
        });
    }

    @SchemaProperty(description = "The test actions.")
    public void setActions(List<TestActions> actions) {
        actions.forEach(action -> delegate.addTestAction(action.get()));
    }

    @SchemaProperty(advanced = true, description = "The final test actions.")
    public void setFinally(List<TestActions> actions) {
        actions.forEach(action -> delegate.addFinalAction(action.get()));
    }

    public static class Variable {

        protected ScriptDefinitionType script;
        protected String name;
        protected String value;

        public String getName() {
            return name;
        }

        @SchemaProperty(required = true, description = "The test variable name.")
        public void setName(String value) {
            this.name = value;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(description = "The test variable value.")
        public void setValue(String value) {
            this.value = value;
        }

        public ScriptDefinitionType getScript() {
            return script;
        }

        @SchemaProperty(
                metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:script") },
                description = "Script that sets the test variable value.")
        public void setScript(ScriptDefinitionType value) {
            this.script = value;
        }
    }

    public static class Endpoint {

        protected String type;
        protected String name;
        protected final Map<String, String> properties = new HashMap<>();

        private EndpointBuilder<?> builder;

        public EndpointBuilder<?> getBuilder() {
            return builder;
        }

        public void setBuilder(EndpointBuilder<?> builder) {
            this.builder = builder;
        }

        public String getType() {
            return type;
        }

        @SchemaProperty(
                metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:generic" ) },
                description = "The endpoint type.")
        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        @SchemaProperty(
                metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:generic" ) },
                description = "The endpoint name. When set the endpoint is registered in the bean registry for later reference.")
        public void setName(String value) {
            this.name = value;
            this.properties.put(EndpointComponent.ENDPOINT_NAME, value);
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        @SchemaProperty(
                metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:generic" ) },
                description = "The endpoint properties.")
        public void setProperties(Map<String, String> properties) {
            this.properties.putAll(properties);
        }
    }
}
