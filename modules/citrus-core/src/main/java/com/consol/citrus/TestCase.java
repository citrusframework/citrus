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
import com.consol.citrus.util.TestUtils;
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
@SuppressWarnings({"unused", "JavaDoc"})
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
    private Map<String, Object> parameters = new LinkedHashMap<>();

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

    /** Test groups */
    private String[] groups;

    /** Time to wait for nested actions to finish */
    private long timeout = 10000L;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(TestCase.class);

    /**
     * Starts the test case.
     */
    public void start(final TestContext context) {
        context.getTestListeners().onTestStart(this);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing test case");
            }

            /* Debug print global variables */
            if (context.hasVariables() && log.isDebugEnabled()) {
                log.debug("Global variables:");
                for (final Entry<String, Object> entry : context.getVariables().entrySet()) {
                    log.debug(entry.getKey() + " = " + entry.getValue());
                }
            }

            // add default variables for test
            context.setVariable(Citrus.TEST_NAME_VARIABLE, getName());
            context.setVariable(Citrus.TEST_PACKAGE_VARIABLE, packageName);

            for (final Entry<String, Object> paramEntry : parameters.entrySet()) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Initializing test parameter '%s' as variable", paramEntry.getKey()));
                }
                context.setVariable(paramEntry.getKey(), paramEntry.getValue());
            }

            /* build up the global test variables in TestContext by
             * getting the names and the current values of all variables */
            for (final Entry<String, Object> entry : variableDefinitions.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();

                if (value instanceof String) {
                    //check if value is a variable or function (and resolve it accordingly)
                    context.setVariable(key, context.replaceDynamicContentInString(value.toString()));
                } else {
                    context.setVariable(key, value);
                }
            }

            /* Debug print all variables */
            if (context.hasVariables() && log.isDebugEnabled()) {
                log.debug("Test variables:");
                for (final Entry<String, Object> entry : context.getVariables().entrySet()) {
                    log.debug(entry.getKey() + " = " + entry.getValue());
                }
            }

            beforeTest(context);
        } catch (final Exception | AssertionError e) {
            testResult = TestResult.failed(getName(), testClass.getName(), e);
            throw new TestCaseFailedException(e);
        }
    }

    /**
     * Method executes the test case and all its actions.
     */
    public void doExecute(final TestContext context) {
        if (!getMetaInfo().getStatus().equals(TestCaseMetaInfo.Status.DISABLED)) {
            try {
                start(context);
                for (final TestAction action: actions) {
                    executeAction(action, context);
                }

                testResult = TestResult.success(getName(), testClass.getName());
            } catch (final TestCaseFailedException e) {
                throw e;
            } catch (final Exception | AssertionError e) {
                testResult = TestResult.failed(getName(), testClass.getName(), e);
                throw new TestCaseFailedException(e);
            } finally {
                finish(context);
            }
        } else {
            testResult = TestResult.skipped(getName(), testClass.getName());
            context.getTestListeners().onTestSkipped(this);
        }
    }

    /**
     * Sequence of test actions before the test case.
     */
    public void beforeTest(final TestContext context) {
        if (beforeTest != null) {
            for (final SequenceBeforeTest sequenceBeforeTest : beforeTest) {
                try {
                    if (sequenceBeforeTest.shouldExecute(getName(), packageName, groups))
                        sequenceBeforeTest.execute(context);
                } catch (final Exception e) {
                    throw new CitrusRuntimeException("Before test failed with errors", e);
                }
            }
        }
    }

    /**
     * Sequence of test actions after test case. This operation does not raise andy errors - exceptions
     * will only be logged as warning. This is because we do not want to overwrite errors that may have occurred
     * before in test execution.
     */
    public void afterTest(final TestContext context) {
        if (afterTest != null) {
            for (final SequenceAfterTest sequenceAfterTest : afterTest) {
                try {
                    if (sequenceAfterTest.shouldExecute(getName(), packageName, groups)) {
                        sequenceAfterTest.execute(context);
                    }
                } catch (final Exception | AssertionError e) {
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
    public void executeAction(final TestAction action, final TestContext context) {
        if (context.hasExceptions()) {
            throw context.getExceptions().remove(0);
        }

        try {
            if (!action.isDisabled(context)) {
                testActionListeners.onTestActionStart(this, action);
                setActiveAction(action);

                action.execute(context);
                testActionListeners.onTestActionFinish(this, action);
            } else {
                testActionListeners.onTestActionSkipped(this, action);
            }
        } catch (final Exception | AssertionError e) {
            testResult = TestResult.failed(getName(), testClass.getName(), e);
            throw new TestCaseFailedException(e);
        }
    }

    /**
     * Method that will be executed in any case of test case result (success, error)
     * Usually used for clean up tasks.
     */
    public void finish(final TestContext context) {
        try {
            CitrusRuntimeException contextException = null;
            if (testResult == null) {
                if (context.hasExceptions()) {
                    contextException = context.getExceptions().remove(0);
                    testResult = TestResult.failed(getName(), testClass.getName(), contextException);
                } else {
                    testResult = TestResult.success(getName(), testClass.getName());
                }
            }

            if (context.isSuccess(testResult)) {
                TestUtils.waitForCompletion(this, context, timeout);
            }

            context.getTestListeners().onTestFinish(this);

            if (!finalActions.isEmpty()) {
                log.debug("Entering finally block in test case");

                /* walk through the finally chain and execute the actions in there */
                for (final TestAction action : finalActions) {
                    if (!action.isDisabled(context)) {
                        testActionListeners.onTestActionStart(this, action);
                        action.execute(context);
                        testActionListeners.onTestActionFinish(this, action);
                    } else {
                        testActionListeners.onTestActionSkipped(this, action);
                    }
                }
            }

            if (testResult.isSuccess() && context.hasExceptions()) {
                contextException = context.getExceptions().remove(0);
                testResult = TestResult.failed(getName(), testClass.getName(), contextException);
            }

            if (contextException != null) {
                throw new TestCaseFailedException(contextException);
            }
        } catch (final TestCaseFailedException e) {
            throw e;
        } catch (final Exception | AssertionError e) {
            testResult = TestResult.failed(getName(), testClass.getName(), e);
            throw new TestCaseFailedException(e);
        } finally {
            if (testResult != null) {
                if (testResult.isSuccess()) {
                    context.getTestListeners().onTestSuccess(this);
                } else {
                    context.getTestListeners().onTestFailure(this, testResult.getCause());
                }
            }

            afterTest(context);
        }
    }

    /**
     * Setter for variables.
     * @param variableDefinitions
     */
    public void setVariableDefinitions(final Map<String, Object> variableDefinitions) {
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
    public void setFinalActions(final List<TestAction> finalActions) {
        this.finalActions = finalActions;
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("[testVariables:");

        for (final Entry<String, Object> entry : variableDefinitions.entrySet()) {
            stringBuilder.append(entry.getKey()).append("=").append(entry.getValue().toString()).append(";");
        }

        stringBuilder.append("] ");

        stringBuilder.append("[testActions:");

        for (final TestAction action: actions) {
            stringBuilder.append(action.getClass().getName()).append(";");
        }

        stringBuilder.append("] ");

        return super.toString() + stringBuilder.toString();
    }

    /**
     * Adds action to finally action chain.
     * @param testAction
     */
    public void addFinalAction(final TestAction testAction) {
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
    public void setMetaInfo(final TestCaseMetaInfo metaInfo) {
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
    public void setBeanName(final String name) {
        if (getName() == null) {
            setName(name);
        }
    }

    /**
     * Set the package name
     * @param packageName the packageName to set
     */
    public void setPackageName(final String packageName) {
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
    public void setTestClass(final Class<?> type) {
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
    public void setParameters(final String[] parameterNames, final Object[] parameterValues) {
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
    public void setTestActionListeners(final TestActionListeners testActionListeners) {
        this.testActionListeners = testActionListeners;
    }

    /**
     * Sets the before test action sequence.
     * @param beforeTest
     */
    public void setBeforeTest(final List<SequenceBeforeTest> beforeTest) {
        this.beforeTest = beforeTest;
    }

    /**
     * Sets the after test action sequence.
     * @param afterTest
     */
    public void setAfterTest(final List<SequenceAfterTest> afterTest) {
        this.afterTest = afterTest;
    }

    /**
     * Sets the test runner flag.
     * @param testRunner
     */
    public void setTestRunner(final boolean testRunner) {
        this.testRunner = testRunner;
    }

    /**
     * Gets the test runner flag.
     * @return
     */
    public boolean isTestRunner() {
        return testRunner;
    }

    /**
     * Sets the test result from outside.
     * @param testResult
     */
    public void setTestResult(final TestResult testResult) {
        this.testResult = testResult;
    }

    /**
     * Gets the groups.
     *
     * @return
     */
    public String[] getGroups() {
        return groups;
    }

    /**
     * Sets the groups.
     *
     * @param groups
     */
    public void setGroups(final String[] groups) {
        this.groups = groups;
    }

    /**
     * Sets the timeout.
     *
     * @param timeout
     */
    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

    /**
     * Gets the timeout.
     *
     * @return
     */
    public long getTimeout() {
        return timeout;
    }
}
