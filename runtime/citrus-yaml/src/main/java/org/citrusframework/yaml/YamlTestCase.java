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

package org.citrusframework.yaml;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import org.citrusframework.DefaultTestCase;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.FileUtils;
import org.citrusframework.variable.VariableUtils;
import org.citrusframework.yaml.actions.script.ScriptDefinitionType;

/**
 * @author Christoph Deppisch
 */
public class YamlTestCase {

    private final DefaultTestCase delegate = new DefaultTestCase();

    /**
     * Gets the test case.
     * @return
     */
    public TestCase getTestCase() {
        return delegate;
    }

    public void setName(String name) {
        delegate.setName(name);
    }

    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    public void setAuthor(String author) {
        delegate.getMetaInfo().setAuthor(author);
    }

    public void setStatus(TestCaseMetaInfo.Status status) {
        delegate.getMetaInfo().setStatus(status);
    }

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

    public void setActions(List<TestActions> actions) {
        actions.forEach(action -> delegate.addTestAction(action.get()));
    }

    public void setFinally(List<TestActions> actions) {
        actions.forEach(action -> delegate.addTestAction(action.get()));
    }

    public static class Variable {

        protected ScriptDefinitionType script;
        protected String name;
        protected String value;

        public ScriptDefinitionType getScript() {
            return script;
        }

        public void setScript(ScriptDefinitionType value) {
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
    }
}
