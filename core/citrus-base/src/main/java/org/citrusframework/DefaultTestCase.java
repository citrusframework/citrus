/*
 * Copyright the original author or authors.
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

package org.citrusframework;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.citrusframework.container.AbstractActionContainer;
import org.citrusframework.container.AfterTest;
import org.citrusframework.container.BeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.citrusframework.TestResult.failed;
import static org.citrusframework.TestResult.skipped;
import static org.citrusframework.TestResult.success;
import static org.citrusframework.util.TestUtils.waitForCompletion;

/**
 * Default test case implementation holding a list of test actions to execute. Test case also holds variable definitions and
 * performs the test lifecycle such as start, finish, before and after test.
 *
 */
public class DefaultTestCase extends AbstractActionContainer implements TestCase, TestGroupAware, TestParameterAware {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTestCase.class);

    private static final TestResultInstanceProvider DEFAULT_TEST_RESULT_INSTANCE_PROVIDER = new DefaultTestResultInstanceProvider();

    /**
     * Further chain of test actions to be executed in any case (success, error)
     * Usually used to clean up database in any case of test result
     */
    private List<TestActionBuilder<?>> finalActions = new ArrayList<>();

    /**
     * Tests variables
     */
    private Map<String, Object> variableDefinitions = new LinkedHashMap<>();

    /**
     * Adhoc endpoint definitions that create endpoints as part of the test.
     */
    private final List<String> endpointDefinitions = new ArrayList<>();

    /**
     * Adhoc endpoints that are port of the test.
     */
    private final List<EndpointBuilder<?>> endpoints = new ArrayList<>();

    /**
     * Meta-Info
     */
    private TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();

    /**
     * Test class type
     */
    private Class<?> testClass = this.getClass();

    /**
     * Test package name
     */
    private String packageName = this.getClass().getPackage().getName();

    /**
     * In case test was called with parameters from outside
     */
    private final Map<String, Object> parameters = new LinkedHashMap<>();

    /**
     * The result of this test case
     */
    private TestResult testResult;

    /**
     * Marks this test case as instance that grows in size step by step as test actions are executed
     */
    private boolean incremental = false;

    /**
     * Test groups
     */
    private String[] groups;

    /**
     * Time to wait for nested actions to finish
     */
    private long timeout = 10000L;

    private final StopWatch timer = new StopWatch();

    public DefaultTestCase() {
        super("test");
    }

    @Override
    public void doExecute(final TestContext context) {
        if (!getMetaInfo().getStatus().equals(TestCaseMetaInfo.Status.DISABLED)) {
            try {
                start(context);

                executeTest(context);

                testResult = getTestResultInstanceProvider(context).createSuccess(this);
            } catch (final TestCaseFailedException e) {
                gracefullyStopTimer();
                throw e;
            } catch (final Exception | Error e) {
                testResult = getTestResultInstanceProvider(context).createFailed(this, e);
                throw new TestCaseFailedException(e);
            }
        } else {
            testResult = getTestResultInstanceProvider(context).createSkipped(this);
            context.getTestListeners().onTestSkipped(this);
        }
    }

    private void executeTest(TestContext context) {
        context.getTestListeners().onTestExecutionStart(this);
        for (final TestActionBuilder<?> actionBuilder : actions) {
            executeAction(actionBuilder.build(), context);
        }
    }

    @Override
    public void start(final TestContext context) {
        context.getTestListeners().onTestStart(this);

        try {
            logger.debug("Initializing test case");

            debugVariables("Global", context);
            initializeTestParameters(parameters, context);
            initializeTestVariables(variableDefinitions, context);
            debugVariables("Test", context);
            initializeEndpoints(endpoints, endpointDefinitions, context);

            beforeTest(context);
        } catch (final Exception | Error e) {
            testResult = getTestResultInstanceProvider(context).createFailed(this, e);
            throw new TestCaseFailedException(e);
        }
    }

    @Override
    public void beforeTest(final TestContext context) {
        restartTimer();

        if (context.getBeforeTest().isEmpty()) {
            return;
        }

        try {
            context.getTestListeners().onBeforeTestStart(this);
            doExecuteSequenceBefore(context);
        } finally {
            context.getTestListeners().onBeforeTestEnd(this);
        }
    }

    private void doExecuteSequenceBefore(TestContext context) {
        for (final BeforeTest sequenceBeforeTest : context.getBeforeTest()) {
            try {
                if (sequenceBeforeTest.shouldExecute(getName(), packageName, groups)) {
                    sequenceBeforeTest.execute(context);
                }
            } catch (final Exception e) {
                throw new CitrusRuntimeException("Before test failed with errors", e);
            }
        }
    }

    @Override
    public void afterTest(final TestContext context) {
        if (context.getAfterTest().isEmpty()) {
            return;
        }

        try {
            context.getTestListeners().onAfterTestStart(this);
        } finally {
            doExecuteSequenceAfter(context);
            context.getTestListeners().onAfterTestEnd(this);
        }
    }

    private void doExecuteSequenceAfter(TestContext context) {
        for (final AfterTest sequenceAfterTest : context.getAfterTest()) {
            try {
                if (sequenceAfterTest.shouldExecute(getName(), packageName, groups)) {
                    sequenceAfterTest.execute(context);
                }
            } catch (final Exception | Error e) {
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
        } catch (final Exception | Error e) {
            testResult = getTestResultInstanceProvider(context).createFailed(this, e);
            throw new TestCaseFailedException(e);
        } finally {
            setExecutedAction(action);
        }
    }

    @Override
    public void fail(Throwable throwable) {
        setTestResult(failed(getName(), testClass.getName(), throwable));
        completeTestResultWithDuration();
    }

    /**
     * Method that will be executed in any case of test case result (success, error). Usually used for clean up tasks.
     */
    public void finish(final TestContext context) {
        if (getMetaInfo().getStatus().equals(TestCaseMetaInfo.Status.DISABLED)) {
            return;
        }

        try {
            CitrusRuntimeException contextException = null;
            if (isNull(testResult)) {
                if (context.hasExceptions()) {
                    contextException = context.getExceptions().remove(0);
                    testResult = getTestResultInstanceProvider(context).createFailed(this, contextException);
                } else {
                    testResult = getTestResultInstanceProvider(context).createSuccess(this);
                }
            }

            if (context.isSuccess(testResult)) {
                waitForCompletion(this, context, timeout);
            }

            executeFinalActions(context);

            if (contextException != null) {
                throw new TestCaseFailedException(contextException);
            }
        } catch (final TestCaseFailedException e) {
            if (isNull(testResult) || testResult.isSuccess()) {
                testResult = getTestResultInstanceProvider(context).createFailed(this, e.getCause());
            }
            throw e;
        } catch (final Exception | Error e) {
            testResult = getTestResultInstanceProvider(context).createFailed(this, e);
            throw new TestCaseFailedException(e);
        } finally {
            context.getTestListeners().onTestExecutionEnd(this);
            doFinishTest(context);
        }
    }

    private void doFinishTest(TestContext context) {
        try {
            if (testResult != null) {
                if (testResult.isSuccess()) {
                    context.getTestListeners().onTestSuccess(this);
                } else {
                    context.getTestListeners().onTestFailure(this, testResult.getCause());
                }
            }

            afterTest(context);
            completeTestResultWithDuration();
        } finally {
            context.getTestListeners().onTestFinalization(this);
        }
    }

    private void restartTimer() {
        if (timer.isStopped()) {
            timer.reset();
            timer.start();
        }
    }

    private void completeTestResultWithDuration() {
        gracefullyStopTimer();

        if (nonNull(testResult)) {
            testResult.withDuration(Duration.ofNanos(timer.getNanoTime()));
        }
    }

    private void gracefullyStopTimer() {
        if (!timer.isStopped()) {
            timer.stop();
        }
    }

    /**
     * Run final test actions.
     */
    private void executeFinalActions(TestContext context) {
        if (!finalActions.isEmpty() || !context.getFinalActions().isEmpty()) {
            try {
                context.getTestListeners().onFinalActionsStart(this);
                doExecuteFinalActions(context);
            } finally {
                context.getTestListeners().onFinalActionsEnd(this);
            }
        }

        if (testResult.isSuccess() && context.hasExceptions()) {
            CitrusRuntimeException contextException = context.getExceptions().remove(0);
            testResult = getTestResultInstanceProvider(context).createFailed(this, contextException);
            throw new TestCaseFailedException(contextException);
        }
    }

    private void doExecuteFinalActions(TestContext context) {
        if (!finalActions.isEmpty()) {

            logger.debug("Entering finally block in test case");

            /* walk through the finally-chain and execute the actions in there */
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

        /* test context may also have some actions to run finally */
        for (final TestActionBuilder<?> actionBuilder : context.getFinalActions()) {
            context.getTestListeners().onFinalActionsStart(this);

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

    /**
     * Print variables in given test context.
     */
    private void debugVariables(String scope, TestContext context) {
        /* Debug print global variables */
        if (context.hasVariables() && logger.isDebugEnabled()) {
            logger.debug("{} variables:", scope);
            for (final Map.Entry<String, Object> entry : context.getVariables().entrySet()) {
                logger.debug("{} = {}", entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Sets test parameters as test variables.
     */
    private void initializeTestParameters(Map<String, Object> parameters, TestContext context) {
        // add default variables for test
        context.setVariable(CitrusSettings.TEST_NAME_VARIABLE, getName());
        context.setVariable(CitrusSettings.TEST_PACKAGE_VARIABLE, packageName);

        for (final Map.Entry<String, Object> paramEntry : parameters.entrySet()) {
            logger.debug("Initializing test parameter '{}' as variable", paramEntry.getKey());
            context.setVariable(paramEntry.getKey(), paramEntry.getValue());
        }
    }

    /**
     * Initialize the test variables in the given test context.
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
     * Initialize endpoints with given uris in the test context.
     */
    private void initializeEndpoints(List<EndpointBuilder<?>> endpointBuilder, List<String> endpointUris, TestContext context) {
        List<Endpoint> bindToRegistry = new ArrayList<>();

        /* use given endpoint uris to create endpoints adhoc with the current test context */
        for (final EndpointBuilder<?> builder : endpointBuilder) {
            if (builder instanceof ReferenceResolverAware resolverAware) {
                resolverAware.setReferenceResolver(context.getReferenceResolver());
            }

            Endpoint endpoint = builder.build();
            if (endpoint instanceof ReferenceResolverAware resolverAware) {
                resolverAware.setReferenceResolver(context.getReferenceResolver());
            }
            bindToRegistry.add(endpoint);
        }

        /* use given endpoint uris to create endpoints adhoc with the current test context */
        for (final String endpointUri : endpointUris) {
            logger.debug("Initializing endpoint '{}'", endpointUri);
            Endpoint endpoint = context.getEndpointFactory().create(endpointUri, context);

            if (endpointUri.contains(EndpointComponent.ENDPOINT_NAME + "=")) {
                bindToRegistry.add(endpoint);
            }
        }

        for (final Endpoint endpoint : bindToRegistry) {
            if (context.getReferenceResolver().isResolvable(endpoint.getName())) {
                logger.warn("Skip binding endpoint to bean registry, because endpoint already exists: {}", endpoint.getName());
            } else {
                logger.info("Binding endpoint {} to bean registry", endpoint.getName());
                context.getReferenceResolver().bind(endpoint.getName(), endpoint);
            }
        }
    }

    /**
     * Setter for variables.
     */
    public void setVariableDefinitions(final Map<String, Object> variableDefinitions) {
        this.variableDefinitions = variableDefinitions;
    }

    @Override
    public Map<String, Object> getVariableDefinitions() {
        return variableDefinitions;
    }

    @Override
    public List<String> getEndpointDefinitions() {
        return endpointDefinitions;
    }

    @Override
    public List<EndpointBuilder<?>> getEndpoints() {
        return endpoints;
    }

    /**
     * Setter for finally chain.
     */
    public void setFinalActions(final List<TestAction> finalActions) {
        this.finalActions = finalActions.stream().map(action -> (TestActionBuilder<?>) () -> action).collect(Collectors.toList());
    }

    private TestResultInstanceProvider getTestResultInstanceProvider(TestContext context) {

        ReferenceResolver referenceResolver = context.getReferenceResolver();
        if (referenceResolver != null && referenceResolver.isResolvable(TestResultInstanceProvider.class)) {
            return referenceResolver.resolve(TestResultInstanceProvider.class);
        }

        return DEFAULT_TEST_RESULT_INSTANCE_PROVIDER;
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

        for (final TestActionBuilder<?> actionBuilder : actions) {
            TestAction action = actionBuilder.build();
            stringBuilder.append(action.getClass().getName()).append(";");
        }

        stringBuilder.append("] ");

        return super.toString() + stringBuilder;
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
     *
     * @param metaInfo the metaInfo to set
     */
    public void setMetaInfo(final TestCaseMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    /**
     * Get all actions in the finally chain.
     *
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
            throw new CitrusRuntimeException(
                    format(
                            "Invalid test parameter usage - received '%s' parameters with '%s' values",
                            parameterNames.length,
                            parameterValues.length));
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
     */
    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

    /**
     * Gets the timeout.
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Default implementation of {@link TestResultInstanceProvider} that provides simple TestResults
     * without any parameters.
     */
    private static final class DefaultTestResultInstanceProvider implements TestResultInstanceProvider {

        @Override
        public TestResult createSuccess(TestCase testCase) {
            return success(testCase.getName(), testCase.getTestClass().getName());
        }

        @Override
        public TestResult createFailed(TestCase testCase, Throwable throwable) {
            return failed(testCase.getName(), testCase.getTestClass().getName(), throwable);
        }

        @Override
        public TestResult createSkipped(TestCase testCase) {
            return skipped(testCase.getName(), testCase.getTestClass().getName());
        }
    }
}
