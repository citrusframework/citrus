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

package org.citrusframework.yaml.actions;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.TraceVariablesAction;
import org.citrusframework.yaml.SchemaProperty;

public class TraceVariables implements TestActionBuilder<TraceVariablesAction> {

    private final TraceVariablesAction.Builder builder = new TraceVariablesAction.Builder();

    protected List<Variable> variables;

    @SchemaProperty(description = "Name of the test variable to include.")
    public void setVariable(String variable) {
        builder.variable(variable);
    }

    public List<Variable> getVariables() {
        if (variables == null) {
            variables = new ArrayList<>();
        }
        return this.variables;
    }

    @SchemaProperty(description = "Test variable names to include.")
    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    @Override
    public TraceVariablesAction build() {
        getVariables().forEach(variable -> builder.variable(variable.name));
        return builder.build();
    }

    public static class Variable {

        protected String name;

        public String getName() {
            return name;
        }

        @SchemaProperty(required = true)
        public void setName(String value) {
            this.name = value;
        }
    }
}
