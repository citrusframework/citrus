package com.consol.citrus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.consol.citrus.container.AbstractActionContainer;
import com.consol.citrus.container.AfterTest;
import com.consol.citrus.container.BeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;

/**
 * Default test case implementation holding a list of test actions to execute. Test case also holds variable definitions and
 * performs the test lifecycle such as start, finish, before and after test.
 * @author Christoph Deppisch
 */
public class DefaultTestCase extends AbstractActionContainer implements TestCase, TestGroupAware, TestParameterAware, BeanNameAware {

    /** Further chain of test actions to be executed in any case (success, error)
     * Usually used to clean up database in any case of test result */
    private List<TestActionBuilder<?>> finalActions = new ArrayList<>();

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

    private TestActionListeners testActionListeners = new TestActionListeners();

    private List<BeforeTest> beforeTest;

    private List<AfterTest> afterTest;

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

    @Override
    public void start(final TestContext context) {
        context.getTestListeners().onTestStart(this);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing test case");
            }

            /* Debug print global variables */
            if (context.hasVariables() && log.isDebugEnabled()) {
                log.debug("Global variables:");
                for (final Map.Entry<String, Object> entry : context.getVariables().entrySet()) {
                    log.debug(entry.getKey() + " = " + entry.getValue());
                }
            }

            // add default variables for test
            context.setVariable(CitrusSettings.TEST_NAME_VARIABLE, getName());
            context.setVariable(CitrusSettings.TEST_PACKAGE_VARIABLE, packageName);

            for (final Map.Entry<String, Object> paramEntry : parameters.entrySet()) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Initializing test parameter '%s' as variable", paramEntry.getKey()));
                }
                context.setVariable(paramEntry.getKey(), paramEntry.getValue());
            }

            /* build up the global test variables in TestContext by
             * getting the names and the current values of all variables */
            for (final Map.Entry<String, Object> entry : variableDefinitions.entrySet()) {
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
                for (final Map.Entry<String, Object> entry : context.getVariables().entrySet()) {
                    log.debug(entry.getKey() + " = " + entry.getValue());
                }
            }

            beforeTest(context);
        } catch (final Exception | AssertionError e) {
            testResult = TestResult.failed(getName(), testClass.getName(), e);
            throw new TestCaseFailedException(e);
        }
    }

    @Override
    public void doExecute(final TestContext context) {
        if (!getMetaInfo().getStatus().equals(TestCaseMetaInfo.Status.DISABLED)) {
            try {
                start(context);
                for (final TestActionBuilder<?> actionBuilder: actions) {
                    TestAction action = actionBuilder.build();
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

    @Override
    public void beforeTest(final TestContext context) {
        if (beforeTest != null) {
            for (final BeforeTest sequenceBeforeTest : beforeTest) {
                try {
                    if (sequenceBeforeTest.shouldExecute(getName(), packageName, groups))
                        sequenceBeforeTest.execute(context);
                } catch (final Exception e) {
                    throw new CitrusRuntimeException("Before test failed with errors", e);
                }
            }
        }
    }

    @Override
    public void afterTest(final TestContext context) {
        if (afterTest != null) {
            for (final AfterTest sequenceAfterTest : afterTest) {
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

    @Override
    public void executeAction(final TestAction action, final TestContext context) {
        if (context.hasExceptions()) {
            throw context.getExceptions().remove(0);
        }

        try {
            if (!action.isDisabled(context)) {
                setActiveAction(action);
                testActionListeners.onTestActionStart(this, action);

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
                for (final TestActionBuilder<?> actionBuilder : finalActions) {
                    TestAction action = actionBuilder.build();
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

    @Override
    public Map<String, Object> getVariableDefinitions() {
        return variableDefinitions;
    }

    /**
     * Setter for finally chain.
     * @param finalActions
     */
    public void setFinalActions(final List<TestAction> finalActions) {
        this.finalActions = finalActions.stream().map(action -> (TestActionBuilder<?>) () -> action).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("[testVariables:");

        for (final Map.Entry<String, Object> entry : variableDefinitions.entrySet()) {
            stringBuilder.append(entry.getKey()).append("=").append(entry.getValue().toString()).append(";");
        }

        stringBuilder.append("] ");

        stringBuilder.append("[testActions:");

        for (final TestActionBuilder<?> actionBuilder: actions) {
            TestAction action = actionBuilder.build();
            stringBuilder.append(action.getClass().getName()).append(";");
        }

        stringBuilder.append("] ");

        return super.toString() + stringBuilder.toString();
    }

    @Override
    public void addFinalAction(final TestAction testAction) {
        this.finalActions.add(() -> testAction);
    }

    /**
     * Adds action to finally action chain.
     * @param testAction
     */
    public void addFinalAction(final TestActionBuilder<?> testAction) {
        this.finalActions.add(testAction);
    }

    @Override
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
        return finalActions.stream().map(TestActionBuilder::build).collect(Collectors.toList());
    }

    @Override
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

    @Override
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

    @Override
    public Class<?> getTestClass() {
        return testClass;
    }

    @Override
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

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public void setTestActionListeners(final TestActionListeners testActionListeners) {
        this.testActionListeners = testActionListeners;
    }

    @Override
    public void setBeforeTest(final List<BeforeTest> beforeTest) {
        this.beforeTest = beforeTest;
    }

    @Override
    public void setAfterTest(final List<AfterTest> afterTest) {
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

    @Override
    public void setTestResult(final TestResult testResult) {
        this.testResult = testResult;
    }

    @Override
    public String[] getGroups() {
        return groups;
    }

    @Override
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
