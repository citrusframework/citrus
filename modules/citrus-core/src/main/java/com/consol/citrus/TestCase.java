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

package com.consol.citrus;

import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;

import com.consol.citrus.container.AbstractActionContainer;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.variable.VariableUtils;

/**
 * Test case executing a list of {@link TestAction} in sequence.
 *
 * @author Christoph Deppisch
 * @since 2006
 */
public class TestCase extends AbstractActionContainer implements BeanNameAware {

    /** Further chain of test actions to be executed in any case (Success, error)
     * Usually used to clean up database in any case of test result */
    private List<TestAction> finallyChain = new ArrayList<TestAction>();

    /** Tests variables */
    private Map<String, String> variableDefinitions = new HashMap<String, String>();

    /** Test context */
    private TestContext context;

    /** Meta-Info */
    private TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
    
    /** Test package name */
    private String packageName;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(TestCase.class);

    /**
     * Method executes the test case and all its actions.
     */
    public void execute(TestContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing TestCase");
        }
        
        this.context = context;

        /* build up the global test variables in TestContext by
         * getting the names and the current values of all variables */
        for (Entry<String, String> entry : variableDefinitions.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (VariableUtils.isVariableName(value)) {
                value = context.getVariable(value);
            } else if(context.getFunctionRegistry().isFunction(value)) {
                value = FunctionUtils.resolveFunction(value, context);
            }

            context.setVariable(key, value);
        }

        /* Debug print all variables */
        if (context.hasVariables() && log.isDebugEnabled()) {
            log.debug("TestCase using the following global variables:");
            for (Entry<String, String> entry : context.getVariables().entrySet()) {
                log.debug(entry.getKey() + " = " + entry.getValue());
            }
        }

        /* execute the test actions */
        for (TestAction action: actions) {
            log.info("");
            log.info("TESTACTION " + (getActionIndex(action)+1) + "/" + getActionCount());

            setLastExecutedAction(action);
            
            /* execute the test action and validate its success */
            action.execute(context);
        }
    }

    /**
     * Method that will be executed in any case of test case result (success, error)
     * Usually used for clean up tasks.
     */
    public void finish() {
        if (!finallyChain.isEmpty()) {
            log.info("Now reaching finally block to finish test case");
        }

        /* walk through the finally chain and execute the actions in there */
        for (TestAction action : finallyChain) {
            /* execute the test action and validate its success */
            action.execute(context);
        }
    }

    /**
     * Setter for variables.
     * @param variableDefinitions
     */
    public void setVariableDefinitions(Map<String, String> variableDefinitions) {
        this.variableDefinitions = variableDefinitions;
    }

    /**
     * Setter for finally chain.
     * @param finallyChain
     */
    public void setFinallyChain(List<TestAction> finallyChain) {
        this.finallyChain = finallyChain;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("[testVariables:");

        for (Entry<String, String> entry : variableDefinitions.entrySet()) {
            buf.append(entry.getKey() + "=" + entry.getValue() + ";");
        }

        buf.append("] ");

        buf.append("[testChain:");

        for (TestAction action: actions) {
            buf.append(action.getClass().getName() + ";");
        }

        buf.append("] ");

        return super.toString() + buf.toString();
    }

    /**
     * Adds action to finally action chain.
     * @param testAction
     */
    public void addFinallyChainAction(TestAction testAction) {
        this.finallyChain.add(testAction);
    }
    
    /**
     * Get the test case meta information.
     * @return the metaInfo
     */
    public TestCaseMetaInfo getMetaInfo() {
        return metaInfo;
    }

    /**
     * Set the test case meta information.
     * @param metaInfo the metaInfo to set
     */
    public void setMetaInfo(TestCaseMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    /**
     * Get all actions in the finally chain.
     * @return the finallyChain
     */
    public List<TestAction> getFinallyChain() {
        return finallyChain;
    }

    /**
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name) {
        if (getName() == null) {
            setName(name);
        }
    }

    /**
     * Get the test context.
     * @return the variables
     */
    public TestContext getTestContext() {
        return context;
    }

    /**
     * Set the test context.
     * @param context the context to set
     */
    public void setTestContext(TestContext context) {
        this.context = context;
    }

    /**
     * Set the package name
     * @param packageName the packageName to set
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Get the package name
     * @return the packageName
     */
    public String getPackageName() {
        return packageName;
    }
}
