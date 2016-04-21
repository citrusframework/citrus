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

import com.consol.citrus.container.*;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.report.TestActionListeners;
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

    /** Further chain of test actions to be executed in any case (success, error)
     * Usually used to clean up database in any case of test result */
    private List<TestAction> finalActions = new ArrayList<>();

    /** Tests variables */
    private Map<String, Object> variableDefinitions = new LinkedHashMap<>();

    /** Meta-Info */
    private TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();

    /** Test class type */
    private Class<?> testClass = this.getClass();

    /** Test package name */
    private String packageName = this.getClass().getPackage().getName();

    /** In case test was called with parameters from outside */
    private Map<String, Object> parameters = new LinkedHashMap<String, Object>();

    @Autowired
    private TestActionListeners testActionListeners = new TestActionListeners();

    @Autowired(required = false)
    private List<SequenceBeforeTest> beforeTest;

    @Autowired(required = false)
    private List<SequenceAfterTest> afterTest;

    /** The result of this test case */
    private TestResult testResult;

    /** Marks this test case as test runner instance that grows in size step by step as test actions are executed */
    private boolean testRunner = false;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(TestCase.class);

    /**
     * Starts the test case.
     * @param context
     */
    public void start(TestContext context) {
        context.getTestListeners().onTestStart(this);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing test case");
            }

           /* build up the global test variables in TestContext by
            * getting the names and the current values of all variables */
            for (Entry<String, Object> entry : variableDefinitions.entrySet()) {
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

            // add default variables for test
            context.setVariable(Citrus.TEST_NAME_VARIABLE, getName());
            context.setVariable(Citrus.TEST_PACKAGE_VARIABLE, getPackageName());

            for (Entry<String, Object> paramEntry : parameters.entrySet()) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Initializing test parameter '%s' as variable", paramEntry.getKey()));
                }
                context.setVariable(paramEntry.getKey(), paramEntry.getValue());
            }

            beforeTest(context);
        } catch (Exception e) {
            testResult = TestResult.failed(getName(), e);
            throw new TestCaseFailedException(e);
        } catch (AssertionError e) {
            testResult = TestResult.failed(getName(), e);
            throw new TestCaseFailedException(e);
        }
    }

    /**
     * Method executes the test case and all its actions.
     */
    public void doExecute(TestContext context) {
        if (!getMetaInfo().getStatus().equals(TestCaseMetaInfo.Status.DISABLED)) {

            try {
                start(context);

                for (TestAction action: actions) {
                    executeAction(action, context);
                }

                testResult = TestResult.success(getName());
            } catch (TestCaseFailedException e) {
                throw e;
            } catch (Exception e) {
                testResult = TestResult.failed(getName(), e);
                throw new TestCaseFailedException(e);
            } catch (AssertionError e) {
                testResult = TestResult.failed(getName(), e);
                throw new TestCaseFailedException(e);
            } finally {
                finish(context);
            }
        } else {
            testResult = TestResult.skipped(getName());
            context.getTestListeners().onTestSkipped(this);
        }
    }

    /**
     * Sequence of test actions before the test case.
     * @param context
     */
    public void beforeTest(TestContext context) {
        if (beforeTest != null) {
            for (SequenceBeforeTest sequenceBeforeTest : beforeTest) {
                try {
                    if (sequenceBeforeTest.shouldExecute(getName(), getPackageName(), null)) //TODO provide test group information
                        sequenceBeforeTest.execute(context);
                } catch (Exception e) {
                    throw new CitrusRuntimeException("Before test failed with errors", e);
                }
            }
        }
    }

    /**
     * Sequence of test actions after test case. This operation does not raise andy errors - exceptions
     * will only be logged as warning. This is because we do not want to overwrite errors that may have occurred
     * before in test execution.
     *
     * @param context
     */
    public void afterTest(TestContext context) {
        if (afterTest != null) {
            for (SequenceAfterTest sequenceAfterTest : afterTest) {
                try {
                    if (sequenceAfterTest.shouldExecute(getName(), getPackageName(), null)) {
                        sequenceAfterTest.execute(context);
                    }
                } catch (Exception e) {
                    log.warn("After test failed with errors", e);
                } catch (AssertionError e) {
                    log.warn("After test failed with errors", e);
                }
            }
        }
    }

    /**
     * Executes a single test action with given test context.
     * @param action
     * @param context
     */
    public void executeAction(TestAction action, TestContext context) {
        try {
            if (!action.isDisabled(context)) {
                testActionListeners.onTestActionStart(this, action);
                setLastExecutedAction(action);

                action.execute(context);
                testActionListeners.onTestActionFinish(this, action);
            } else {
                testActionListeners.onTestActionSkipped(this, action);
            }
        } catch (Exception e) {
            testResult = TestResult.failed(getName(), e);
            throw new TestCaseFailedException(e);
        } catch (AssertionError e) {
            testResult = TestResult.failed(getName(), e);
            throw new TestCaseFailedException(e);
        }
    }

    /**
     * Method that will be executed in any case of test case result (success, error)
     * Usually used for clean up tasks.
     */
    public void finish(TestContext context) {
        context.getTestListeners().onTestFinish(this);

        try {
            if (!finalActions.isEmpty()) {
                log.debug("Entering finally block in test case");

                /* walk through the finally chain and execute the actions in there */
                for (TestAction action : finalActions) {
                    action.execute(context);
                }
            }

            if (testResult == null) {
                testResult = TestResult.success(getName());
            }
        } catch (Exception e) {
            testResult = TestResult.failed(getName(), e);
            throw new TestCaseFailedException(e);
        } catch (AssertionError e) {
            testResult = TestResult.failed(getName(), e);
            throw new TestCaseFailedException(e);
        } finally {
            if (testResult.isSuccess()) {
                context.getTestListeners().onTestSuccess(this);
            } else {
                context.getTestListeners().onTestFailure(this, testResult.getCause());
            }

            afterTest(context);
        }
    }

    /**
     * Setter for variables.
     * @param variableDefinitions
     */
    public void setVariableDefinitions(Map<String, Object> variableDefinitions) {
        this.variableDefinitions = variableDefinitions;
    }

    /**
     * Gets the variable definitions.
     * @return
     */
    public Map<String, Object> getVariableDefinitions() {
        return variableDefinitions;
    }

    /**
     * Setter for finally chain.
     * @param finalActions
     */
    public void setFinalActions(List<TestAction> finalActions) {
        this.finalActions = finalActions;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("[testVariables:");

        for (Entry<String, Object> entry : variableDefinitions.entrySet()) {
            buf.append(entry.getKey()).append("=").append(entry.getValue().toString()).append(";");
        }

        buf.append("] ");

        buf.append("[testActions:");

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
    public void addFinalAction(TestAction testAction) {
        this.finalActions.add(testAction);
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
     * @return the finalActions
     */
    public List<TestAction> getFinalActions() {
        return finalActions;
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
     * Set the test class type.
     * @param type
     */
    public void setTestClass(Class<?> type) {
        this.testClass = type;
    }

    /**
     * Gets the value of the testClass property.
     * @return the testClass
     */
    public Class<?> getTestClass() {
        return testClass;
    }

    /**
     * Sets the parameters.
     * @param parameterNames the parameter names to set
     * @param parameterValues the parameters to set
     */
    public void setParameters(String[] parameterNames, Object[] parameterValues) {
        if (parameterNames.length != parameterValues.length) {
            throw new CitrusRuntimeException(String.format("Invalid test parameter usage - received '%s' parameters with '%s' values",
                    parameterNames.length, parameterValues.length));
        }

        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterValues[i] != null) {
                this.parameters.put(parameterNames[i], parameterValues[i]);
            }
        }
    }

    /**
     * Gets the test parameters.
     * @return the parameters
     */
    public Map<String, Object> getParameters() {
        return parameters;
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
    public void setBeforeTest(List<SequenceBeforeTest> beforeTest) {
        this.beforeTest = beforeTest;
    }

    /**
     * Sets the after test action sequence.
     * @param afterTest
     */
    public void setAfterTest(List<SequenceAfterTest> afterTest) {
        this.afterTest = afterTest;
    }

    /**
     * Sets the test runner flag.
     * @param testRunner
     */
    public void setTestRunner(boolean testRunner) {
        this.testRunner = testRunner;
    }

    /**
     * Gets the test runner flag.
     * @return
     */
    public boolean isTestRunner() {
        return testRunner;
    }
}
