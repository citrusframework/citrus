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

package org.citrusframework.actions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.variable.VariableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action creating new test variables during a test. Existing test variables are overwritten
 * by new values.
 *
 * @since 2006
 */
public class CreateVariablesAction extends AbstractTestAction {

    /** New variables to set */
    private final Map<String, Object> variables;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CreateVariablesAction.class);

    /**
     * Default constructor.
     */
    private CreateVariablesAction(Builder builder) {
        super("create-variables", builder);

        this.variables = builder.variables;
    }

    @Override
    public void doExecute(TestContext context) {
        for (Entry<String, Object> entry : variables.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String stringValue) {
                if (stringValue.startsWith("script:<")) {
                    String scriptEngine = stringValue.substring("script:<".length(), stringValue.indexOf('>'));
                    stringValue = VariableUtils.getValueFromScript(scriptEngine,
                            context.replaceDynamicContentInString(stringValue.substring(stringValue.indexOf('>') + 1)));
                }

                //check if value is variable or function (and resolve it if yes)
                value = context.replaceDynamicContentInString(stringValue);
            }

            logger.info("Setting variable: {} to value: {}", key, value);

            context.setVariable(key, value);
        }
    }

    /**
     * Gets the variables.
     * @return the variables
     */
    public Map<String, Object> getVariables() {
        return variables;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<CreateVariablesAction, Builder> {

        private final Map<String, Object> variables = new LinkedHashMap<>();

        public static Builder createVariable(String variableName, Object value) {
            Builder builder = new Builder();
            builder.variable(variableName, value);
            return builder;
        }

        public static Builder createVariables() {
            return new Builder();
        }

        public Builder variable(String variableName, Object value) {
            this.variables.put(variableName, value);
            return this;
        }

        @Override
        public CreateVariablesAction build() {
            return new CreateVariablesAction(this);
        }
    }
}
