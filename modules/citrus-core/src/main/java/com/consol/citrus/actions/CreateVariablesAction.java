/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.actions;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.variable.VariableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Action creating new test variables during a test. Existing test variables are overwritten
 * by new values.
 *
 * @author Christoph Deppisch
 * @since 2006
 */
public class CreateVariablesAction extends AbstractTestAction {

    /** New variables to set */
    private Map<String, String> variables = new LinkedHashMap<String, String>();

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CreateVariablesAction.class);

    /**
     * Default constructor.
     */
    public CreateVariablesAction() {
        setName("create-variables");
    }

    @Override
    public void doExecute(TestContext context) {
        for (Entry<String, String> entry : variables.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value.startsWith("script:<")) {
                String scriptEngine = value.substring("script:<".length(), value.indexOf('>'));
                value = VariableUtils.getValueFromScript(scriptEngine,
                        context.replaceDynamicContentInString(value.substring(value.indexOf('>') + 1)));
            }

            //check if value is variable or function (and resolve it if yes)
            value = context.replaceDynamicContentInString(value);

            log.info("Setting variable: " + key + " to value: " + value);

            context.setVariable(key, value);
        }
    }

    /**
     * Setter for variables
     * @param variables
     */
    public CreateVariablesAction setVariables(Map<String, String> variables) {
        this.variables = variables;
        return this;
    }

    /**
     * Gets the variables.
     * @return the variables
     */
    public Map<String, String> getVariables() {
        return variables;
    }

}
