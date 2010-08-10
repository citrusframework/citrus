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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.variable.VariableUtils;

/**
 * Action creating new test variables during a test. Existing test variables are overwritten
 * by new values.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class CreateVariablesAction extends AbstractTestAction {

    /** New variables to set */
    private Map<String, String> newVariables = new LinkedHashMap<String, String>();

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(CreateVariablesAction.class);

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute(TestContext)
     */
    @Override
    public void execute(TestContext context) {
        for (Entry<String, String> entry : newVariables.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            log.info("Try to set variable: " +  key + " to value " + value);

            if (VariableUtils.isVariableName(value)) {
                value = context.getVariable(value);
            } else if(context.getFunctionRegistry().isFunction(value)) {
                value = FunctionUtils.resolveFunction(value, context);
            } 

            log.info("Setting variable: " + key + " to value: " + value);

            context.setVariable(key, value);
        }
    }

    /**
     * Setter for variables
     * @param variables
     */
    public void setVariables(Map<String, String> variables) {
        this.newVariables = variables;
    }

}
