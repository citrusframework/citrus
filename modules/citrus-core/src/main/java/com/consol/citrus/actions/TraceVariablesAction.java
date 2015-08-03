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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * Action that prints variable values to the console/logger. Action requires a list of variable
 * names. Tries to find the variables in the test context and print its values.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class TraceVariablesAction extends AbstractTestAction {
    /** List of variable names */
    private List<String> variableNames;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(TraceVariablesAction.class);

    /**
     * Default constructor.
     */
    public TraceVariablesAction() {
        setName("trace");
    }

    @Override
    public void doExecute(TestContext context) {
        Iterator<String> it;
        if (variableNames != null && variableNames.size() > 0) {
            log.info("Validating variables using custom map:");
            
            it = variableNames.iterator();
        } else {
            log.info("Validating all variables in context:");
            
            it = context.getVariables().keySet().iterator();
        }

        while (it.hasNext()) {
            String key = it.next();
            String value = context.getVariable(key);

            log.info("Current value of variable " + key + " = " + value);
        }
    }

    /**
     * Setter for info values list
     * @param variableNames
     */
    public TraceVariablesAction setVariableNames(List<String> variableNames) {
        this.variableNames = variableNames;
        return this;
    }

    /**
     * Gets the variableNames.
     * @return the variableNames
     */
    public List<String> getVariableNames() {
        return variableNames;
    }
}
