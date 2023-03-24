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

package org.citrusframework;

import org.citrusframework.container.AfterSuite;
import org.citrusframework.container.BeforeSuite;
import org.citrusframework.context.TestContext;
import org.citrusframework.report.MessageListener;
import org.citrusframework.report.MessageListenerAware;
import org.citrusframework.report.TestListener;
import org.citrusframework.report.TestListenerAware;
import org.citrusframework.report.TestReporter;
import org.citrusframework.report.TestReporterAware;
import org.citrusframework.report.TestSuiteListener;
import org.citrusframework.report.TestSuiteListenerAware;

/**
 * Citrus main class initializes a new Citrus runtime environment with a Citrus context. Provides before/after suite action execution
 * and test execution methods.
 *
 * @author Christoph Deppisch
 * @since 2.1
 */
public final class Citrus implements TestListenerAware, TestSuiteListenerAware, TestReporterAware, MessageListenerAware {

    private final CitrusContext citrusContext;

    /**
     * Constructor with given context that holds all basic Citrus components needed to run a Citrus project.
     * @param citrusContext
     */
    Citrus(CitrusContext citrusContext) {
        this.citrusContext = citrusContext;
    }

    /**
     * Initializing method creating a Citrus instance with new default CitrusContext and its components
     * such as test listeners and test context factory.
     * @return
     */
    public static Citrus newInstance() {
        return CitrusInstanceManager.newInstance();
    }

    /**
     * Initializing method creating a Citrus instance with given Citrus context provider.
     * Provider creates new CitrusContext and its components such as test listeners and test context factory.
     * @return
     */
    public static Citrus newInstance(CitrusContextProvider contextProvider) {
        return CitrusInstanceManager.newInstance(contextProvider);
    }

    /**
     * Performs before suite test actions.
     * @param suiteName
     * @param testGroups
     */
    public void beforeSuite(String suiteName, String ... testGroups) {
        citrusContext.getTestSuiteListeners().onStart();

        for (BeforeSuite sequenceBeforeSuite : citrusContext.getBeforeSuite()) {
            try {
                if (sequenceBeforeSuite.shouldExecute(suiteName, testGroups)) {
                    sequenceBeforeSuite.execute(citrusContext.createTestContext());
                }
            } catch (Exception e) {
                citrusContext.getTestSuiteListeners().onStartFailure(e);
                afterSuite(suiteName, testGroups);

                throw new AssertionError("Before suite failed with errors", e);
            }
        }

        citrusContext.getTestSuiteListeners().onStartSuccess();
    }

    /**
     * Performs after suite test actions.
     * @param suiteName
     * @param testGroups
     */
    public void afterSuite(String suiteName, String ... testGroups) {
        citrusContext.getTestSuiteListeners().onFinish();

        for (AfterSuite sequenceAfterSuite : citrusContext.getAfterSuite()) {
            try {
                if (sequenceAfterSuite.shouldExecute(suiteName, testGroups)) {
                    sequenceAfterSuite.execute(citrusContext.createTestContext());
                }
            } catch (Exception e) {
                citrusContext.getTestSuiteListeners().onFinishFailure(e);
                throw new AssertionError("After suite failed with errors", e);
            }
        }

        citrusContext.getTestSuiteListeners().onFinishSuccess();
    }

    /**
     * Runs a test action which can also be a whole test case.
     */
    public void run(TestAction action) {
        run(action, citrusContext.createTestContext());
    }

    /**
     * Runs test action with given test context. Test action can also be a whole test case.
     * @param action
     * @param testContext
     */
    public void run(TestAction action, TestContext testContext) {
        action.execute(testContext);
    }

    /**
     * Gets the Citrus version from classpath resource properties.
     * @return
     */
    public static String getVersion() {
        return CitrusVersion.version();
    }

    @Override
    public void addTestSuiteListener(TestSuiteListener suiteListener) {
        citrusContext.addTestSuiteListener(suiteListener);
    }

    @Override
    public void addTestListener(TestListener testListener) {
        citrusContext.addTestListener(testListener);
    }

    @Override
    public void addTestReporter(TestReporter testReporter) {
        citrusContext.addTestReporter(testReporter);
    }

    @Override
    public void addMessageListener(MessageListener listener) {
        citrusContext.addMessageListener(listener);
    }

    /**
     * Obtains the citrusContext.
     * @return
     */
    public CitrusContext getCitrusContext() {
        return citrusContext;
    }

    /**
     * Closing Citrus and its context.
     */
    public void close() {
        citrusContext.close();
    }
}
