package org.citrusframework;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.citrusframework.container.AbstractActionContainer;
import org.citrusframework.container.AfterTest;
import org.citrusframework.container.BeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default test case implementation holding a list of test actions to execute. Test case also holds variable definitions and
 * performs the test lifecycle such as start, finish, before and after test.
 * @author Christoph Deppisch
 */
public class DefaultTestCase extends AbstractActionContainer implements TestCase, TestGroupAware, TestParameterAware {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DefaultTestCase.class);

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
    private final Map<String, Object> parameters = new LinkedHashMap<>();

    /** The result of this test case */
    private TestResult testResult;

    /** Marks this test case as instance that grows in size step by step as test actions are executed */
    private boolean incremental = false;

    /** Test groups */
    private String[] groups;

    /** Time to wait for nested actions to finish */
    private long timeout = 10000L;

    @Override
    public void start(final TestContext context) {
        context.getTestListeners().onTestStart(this);

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Initializing test case");
            }

            debugVariables("Global", context);
            initializeTestParameters(parameters, context);
            initializeTestVariables(variableDefinitions, context);
            debugVariables("Test", context);

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
                    executeAction(actionBuilder.build(), context);
                }

                testResult = TestResult.success(getName(), testClass.getName());
            } catch (final TestCaseFailedException e) {
                throw e;
            } catch (final Exception | AssertionError e) {
                testResult = TestResult.failed(getName(), testClass.getName(), e);
                throw new TestCaseFailedException(e);
            }
        } else {
            testResult = TestResult.skipped(getName(), testClass.getName());
            context.getTestListeners().onTestSkipped(this);
        }
    }

    @Override
    public void beforeTest(final TestContext context) {
        for (final BeforeTest sequenceBeforeTest : context.getBeforeTest()) {
            try {
                if (sequenceBeforeTest.shouldExecute(getName(), packageName, groups))
                    sequenceBeforeTest.execute(context);
            } catch (final Exception e) {
                throw new CitrusRuntimeException("Before test failed with errors", e);
            }
        }
    }

    @Override
    public void afterTest(final TestContext context) {
        for (final AfterTest sequenceAfterTest : context.getAfterTest()) {
            try {
                if (sequenceAfterTest.shouldExecute(getName(), packageName, groups)) {
                    sequenceAfterTest.execute(context);
                }
            } catch (final Exception | AssertionError e) {
                logger.warn("After test failed with errors", e);
            }
        }
    }

    @Override
    public void executeAction(final TestAction action, final TestContext context) {
        if (context.hasExceptions()) {
            throw context.getExceptions().remove(0);
        }

        try {
            setActiveAction(action);
            if (!action.isDisabled(context)) {
                context.getTestActionListeners().onTestActionStart(this, action);

                action.execute(context);
                context.getTestActionListeners().onTestActionFinish(this, action);
            } else {
                context.getTestActionListeners().onTestActionSkipped(this, action);
            }
        } catch (final Exception | AssertionError e) {
            testResult = TestResult.failed(getName(), testClass.getName(), e);
            throw new TestCaseFailedException(e);
        } finally {
            setExecutedAction(action);
        }
    }

    /**
     * Method that will be executed in any case of test case result (success, error)
     * Usually used for clean up tasks.
     */
    public void finish(final TestContext context) {
        if (getMetaInfo().getStatus().equals(TestCaseMetaInfo.Status.DISABLED)) {
            return;
        }

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
            executeFinalActions(context);

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
     * Run final test actions.
     * @param context
     */
    private void executeFinalActions(TestContext context) {
        if (!finalActions.isEmpty()) {
            logger.debug("Entering finally block in test case");

            /* walk through the finally chain and execute the actions in there */
            for (final TestActionBuilder<?> actionBuilder : finalActions) {
                TestAction action = actionBuilder.build();
                if (!action.isDisabled(context)) {
                    context.getTestActionListeners().onTestActionStart(this, action);
                    action.execute(context);
                    context.getTestActionListeners().onTestActionFinish(this, action);
                } else {
                    context.getTestActionListeners().onTestActionSkipped(this, action);
                }
            }
        }

        if (testResult.isSuccess() && context.hasExceptions()) {
            CitrusRuntimeException contextException = context.getExceptions().remove(0);
            testResult = TestResult.failed(getName(), testClass.getName(), contextException);
            throw new TestCaseFailedException(contextException);
        }
    }

    /**
     * Print variables in given test context.
     * @param scope
     * @param context
     */
    private void debugVariables(String scope, TestContext context) {
        /* Debug print global variables */
        if (context.hasVariables() && logger.isDebugEnabled()) {
            logger.debug(String.format("%s variables:", scope));
            for (final Map.Entry<String, Object> entry : context.getVariables().entrySet()) {
                logger.debug(String.format("%s = %s", entry.getKey(), entry.getValue()));
            }
        }
    }

    /**
     * Sets test parameters as test variables.
     * @param parameters
     * @param context
     */
    private void initializeTestParameters(Map<String, Object> parameters, TestContext context) {
        // add default variables for test
        context.setVariable(CitrusSettings.TEST_NAME_VARIABLE, getName());
        context.setVariable(CitrusSettings.TEST_PACKAGE_VARIABLE, packageName);

        for (final Map.Entry<String, Object> paramEntry : parameters.entrySet()) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Initializing test parameter '%s' as variable", paramEntry.getKey()));
            }
            context.setVariable(paramEntry.getKey(), paramEntry.getValue());
        }
    }

    /**
     * Initialize the test variables in the given test context.
     * @param variableDefinitions
     * @param context
     */
    private void initializeTestVariables(Map<String, Object> variableDefinitions, TestContext context) {
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
    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    @Override
    public String getPackageName() {
        return packageName;
    }

    @Override
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
    public List<TestActionBuilder<?>> getActionBuilders() {
        return actions;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public void setIncremental(boolean incremental) {
        this.incremental = incremental;
    }

    @Override
    public boolean isIncremental() {
        return incremental;
    }

    @Override
    public void setTestResult(final TestResult testResult) {
        this.testResult = testResult;
    }

    @Override
    public TestResult getTestResult() {
        return testResult;
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
