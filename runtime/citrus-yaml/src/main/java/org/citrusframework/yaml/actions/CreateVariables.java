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

package org.citrusframework.yaml.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.CreateVariablesAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.FileUtils;

/**
 * @author Christoph Deppisch
 */
public class CreateVariables implements TestActionBuilder<CreateVariablesAction> {

    private final CreateVariablesAction.Builder builder = new CreateVariablesAction.Builder();

    protected List<Variable> variables = new ArrayList<>();

    @Override
    public CreateVariablesAction build() {
        for (Variable variable : variables) {
            if (variable.getScript() != null) {
                if (variable.getScript().getFile() != null) {
                    try {
                        builder.variable(variable.getName(),
                                FileUtils.readToString(FileUtils.getFileResource(variable.getScript().getFile())));
                    } catch (IOException e) {
                        throw new CitrusRuntimeException("Failed to read variable script file resource", e);
                    }
                } else {
                    builder.variable(variable.getName(), String.format("script:<%s>%s",
                            Optional.ofNullable(variable.getScript().getType()).orElse("groovy"),
                            variable.getScript().getValue().trim()));
                }
            } else {
                builder.variable(variable.getName(), variable.getValue().trim());
            }
        }

        return builder.build();
    }

    public void setDescription(String value) {
        builder.description(value);
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public static class Variable {

        protected String name;
        protected String value;
        protected Script script;

        public Script getScript() {
            return script;
        }

        public void setScript(Script value) {
            this.script = value;
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

        public static class Script {

            protected String value;
            protected String type;
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
