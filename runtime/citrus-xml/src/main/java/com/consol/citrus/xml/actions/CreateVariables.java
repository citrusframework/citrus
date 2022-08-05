/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.xml.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.actions.CreateVariablesAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "create-variables")
public class CreateVariables implements TestActionBuilder<CreateVariablesAction> {

    private final CreateVariablesAction.Builder builder = new CreateVariablesAction.Builder();

    @XmlElement(name = "variable", required = true)
    protected List<Variable> variables = new ArrayList<>();

    @Override
    public CreateVariablesAction build() {
        for (Variable variable : variables) {
            if (variable.getVariableValue() != null) {
                if (variable.getVariableValue().getScript() != null) {
                    if (variable.getVariableValue().getScript().getFile() != null) {
                        try {
                            builder.variable(variable.getName(),
                                    FileUtils.readToString(FileUtils.getFileResource(variable.getVariableValue().getScript().getFile())));
                        } catch (IOException e) {
                            throw new CitrusRuntimeException("Failed to read variable script file resource", e);
                        }
                    } else {
                        builder.variable(variable.getName(), String.format("script:<%s>%s",
                                Optional.ofNullable(variable.getVariableValue().getScript().getType()).orElse("groovy"),
                                variable.getVariableValue().getScript().getValue()));
                    }
                } else {
                    builder.variable(variable.getName(), variable.getVariableValue().getData());
                }
            } else {
                builder.variable(variable.getName(), variable.getValue());
            }
        }

        return builder.build();
    }

    @XmlElement
    public CreateVariables setDescription(String value) {
        builder.description(value);
        return this;
    }


    public CreateVariables setVariables(List<Variable> variables) {
        this.variables = variables;
        return this;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Variable {

        @XmlElement(name = "value")
        protected Value variableValue;
        @XmlAttribute(name = "name", required = true)
        protected String name;
        @XmlAttribute(name = "value")
        protected String value;

        public Variable.Value getVariableValue() {
            return variableValue;
        }

        public void setVariableValue(Variable.Value value) {
            this.variableValue = value;
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

            protected Script script;
            protected String data;

            public Script getScript() {
                return script;
            }

            public void setScript(Script value) {
                this.script = value;
            }

            public String getData() {
                return data;
            }

            public void setData(String value) {
                this.data = value;
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "ScriptType", propOrder = {
                "value"
        })
        public static class Script {

            @XmlValue
            protected String value;
            @XmlAttribute(name = "type", required = true)
            protected String type;
            @XmlAttribute(name = "file")
            protected String file;

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public String getType() {
                return type;
            }

            public void setType(String value) {
                this.type = value;
            }

            public String getFile() {
                return file;
            }

            public void setFile(String value) {
                this.file = value;
            }
        }
    }
}
