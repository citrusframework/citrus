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

import com.consol.citrus.container.AbstractActionContainer;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.report.TestListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.Map.Entry;

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
    private Map<String, ?> variableDefinitions = new LinkedHashMap<String, Object>();

    /** Meta-Info */
    private TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
    
    /** Test package name */
    private String packageName;
    
    /** In case test was called with parameters from outside */
    private String[] parameters = new String[] {};

    @Autowired
    private TestListeners testListeners = new TestListeners();

    @Autowired
    private TestActionListeners testActionListeners = new TestActionListeners();

    @Autowired(required = false)
    private SequenceBeforeTest beforeTest;
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(TestCase.class);

    /**
     * Method executes the test case and all its actions.
     */
    public void doExecute(TestContext context) {
        if (!getMetaInfo().getStatus().equals(TestCaseMetaInfo.Status.DISABLED)) {
            testListeners.onTestStart(this);

            try {
                beforeTest(context);
                run(context);

                testListeners.onTestSuccess(this);
            } catch (Exception e) {
                testListeners.onTestFailure(this, e);
                throw new TestCaseFailedException(e);
            } catch (Error e) {
                testListeners.onTestFailure(this, e);
                throw new TestCaseFailedException(e);
            } finally {
                testListeners.onTestFinish(this);
                finish(context);
            }
        } else {
            testListeners.onTestSkipped(this);
        }
    }

    public void beforeTest(TestContext context) {
        if (beforeTest != null) {
            try {
                beforeTest.execute(context);
            } catch (Exception e) {
                throw new CitrusRuntimeException("Before test failed with errors", e);
            }
        }
    }

    protected void run(TestContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing test case");
        }

        /* build up the global test variables in TestContext by
         * getting the names and the current values of all variables */
        for (Entry<String, ?> entry : variableDefinitions.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                //check if value is a variable or function (and resolve it accordingly)
                context.setVariable(key, context.replaceDynamicContentInString(value.toString()));
            } else {
                context.setVariable(key, value);
            }
        }

        /* Debug print all variables */
        if (context.hasVariables() && log.isDebugEnabled()) {
            log.debug("Global variables:");
            for (Entry<String, Object> entry : context.getVariables().entrySet()) {
                log.debug(entry.getKey() + " = " + entry.getValue());
            }
        }

        /* execute the test actions */
        for (TestAction action: actions) {
            if (!action.isDisabled(context)) {
                testActionListeners.onTestActionStart(this, action);
                setLastExecutedAction(action);

                /* execute the test action and validate its success */
                action.execute(context);
                testActionListeners.onTestActionFinish(this, action);
            } else {
                testActionListeners.onTestActionSkipped(this, action);
            }
        }
    }

    /**
     * Method that will be executed in any case of test case result (success, error)
     * Usually used for clean up tasks.
     */
    protected void finish(TestContext context) {
        if (!finallyChain.isEmpty()) {
            log.info("Finish test case with finally block actions");
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
    public void setVariableDefinitions(Map<String, ?> variableDefinitions) {
        this.variableDefinitions = variableDefinitions;
    }

    /**
     * Gets the variable definitions.
     * @return
     */
    public Map<String, ?> getVariableDefinitions() {
        return variableDefinitions;
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

        for (Entry<String, ?> entry : variableDefinitions.entrySet()) {
            buf.append(entry.getKey()).append("=").append(entry.getValue().toString()).append(";");
        }

        buf.append("] ");

        buf.append("[testChain:");

        for (TestAction action: actions) {
            buf.append(action.getClass().getName()).append(";");
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

    /**
     * Sets the parameters.
     * @param parameters the parameters to set
     */
    public void setParameters(String[] parameters) {
        this.parameters = Arrays.copyOf(parameters, parameters.length);
    }

    /**
     * Gets the parameters.
     * @return the parameters
     */
    public String[] getParameters() {
        return Arrays.copyOf(parameters, parameters.length);
    }

    /**
     * Sets the list of test listeners.
     * @param testListeners
     */
    public void setTestListeners(TestListeners testListeners) {
        this.testListeners = testListeners;
    }

    /**
     * Sets the list of test action listeners.
     * @param testActionListeners
     */
    public void setTestActionListeners(TestActionListeners testActionListeners) {
        this.testActionListeners = testActionListeners;
    }

    /**
     * Sets the before test action sequence.
     * @param beforeTest
     */
    public void setBeforeTest(SequenceBeforeTest beforeTest) {
        this.beforeTest = beforeTest;
    }
}
