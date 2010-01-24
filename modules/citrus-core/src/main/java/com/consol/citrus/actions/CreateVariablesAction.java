/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
