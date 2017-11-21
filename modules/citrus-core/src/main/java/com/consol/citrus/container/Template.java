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

package com.consol.citrus.container;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.variable.VariableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

/**
 * This class represents a previously defined block of test actions. Test cases can call
 * templates and reuse their functionality.
 * 
 * Templates operate on test variables. While calling, the template caller can set these
 * variables as parameters.
 * 
 * Nested test actions are executed in sequence.
 * 
 * The template execution may affect existing variable values in the calling test case. So
 * variables may have different values in the test case after template execution. Therefore
 * users can create a local test context by setting globalContext to false. Templates then will 
 * have no affect on the variables used in the test case.
 *
 * @author Christoph Deppisch
 * @since 2007
 */
public class Template extends AbstractTestAction {

    /** List of actions to be executed */
    private List<TestAction> actions = new ArrayList<TestAction>();

    /** List of parameters to set before execution */
    private Map<String, String> parameter = new LinkedHashMap<String, String>();
    
    /** Should variables effect the global variables scope? */
    private boolean globalContext = true;

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(Template.class);

    @Override
    public void doExecute(TestContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Executing template '" + getName() + "' with " + actions.size() + " embedded actions");
        }

        TestContext innerContext;
        
        //decide whether to use global test context or not
        if (globalContext) {
            innerContext = context;
        } else {
            innerContext = new TestContext();
            innerContext.setFunctionRegistry(context.getFunctionRegistry());

            GlobalVariables globalVariables = new GlobalVariables();
            globalVariables.getVariables().putAll(context.getGlobalVariables());
            innerContext.setGlobalVariables(globalVariables);
            innerContext.getVariables().putAll(context.getVariables());

            innerContext.setMessageStore(context.getMessageStore());
            innerContext.setMessageValidatorRegistry(context.getMessageValidatorRegistry());
            innerContext.setValidationMatcherRegistry(context.getValidationMatcherRegistry());
            innerContext.setTestListeners(context.getTestListeners());
            innerContext.setMessageListeners(context.getMessageListeners());
            innerContext.setGlobalMessageConstructionInterceptors(context.getGlobalMessageConstructionInterceptors());
            innerContext.setEndpointFactory(context.getEndpointFactory());
            innerContext.setNamespaceContextBuilder(context.getNamespaceContextBuilder());
            innerContext.setApplicationContext(context.getApplicationContext());
        }
        
        for (Entry<String, String> entry : parameter.entrySet()) {
            String param = entry.getKey();
            String paramValue = entry.getValue();

			paramValue = VariableUtils.replaceVariablesInString(paramValue, innerContext, false);
            if (context.getFunctionRegistry().isFunction(paramValue)) {
                paramValue = FunctionUtils.resolveFunction(paramValue, context);
            }

            if (log.isDebugEnabled()) {
                log.debug("Setting parameter for template " + param + "=" + paramValue);
            }

            innerContext.setVariable(param, paramValue);
        }

        for (TestAction action: actions) {
            action.execute(innerContext);
        }

        log.info("Template was executed successfully");
    }

    /**
     * Set nested test actions.
     * @param actions
     */
    public void setActions(List<TestAction> actions) {
        this.actions = actions;
    }

    /**
     * Set parameter before execution.
     * @param parameter the parameter to set
     */
    public void setParameter(Map<String, String> parameter) {
        this.parameter = parameter;
    }

    /**
     * Boolean flag marking the template variables should also affect
     * variables in test case.
     * @param globalContext the globalContext to set
     */
    public void setGlobalContext(boolean globalContext) {
        this.globalContext = globalContext;
    }

    /**
     * Gets the parameter.
     * @return the parameter
     */
    public Map<String, String> getParameter() {
        return parameter;
    }

    /**
     * Gets the globalContext.
     * @return the globalContext
     */
    public boolean isGlobalContext() {
        return globalContext;
    }

    /**
     * Gets the actions.
     * @return the actions
     */
    public List<TestAction> getActions() {
        return actions;
    }
}
