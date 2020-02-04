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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.container.SequenceAfterSuite;
import com.consol.citrus.container.SequenceBeforeSuite;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.report.TestListener;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.report.TestSuiteListener;
import com.consol.citrus.report.TestSuiteListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Citrus main class initializes a new Citrus runtime environment with a Spring application context. Provides before/after suite action execution
 * and test execution methods.
 *
 * @author Christoph Deppisch
 * @since 2.1
 */
public final class Citrus {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(Citrus.class);

    /** Strategy decides which instances are created */
    private static InstanceStrategy strategy = InstanceStrategy.NEW;

    /**
     * Citrus instance processor takes part in instance creation process.
     */
    public interface InstanceProcessor {
        void process(Citrus instance);
    }

    /**
     * Instance creation strategy.
     */
    public enum InstanceStrategy {
        NEW,
        SINGLETON
    }

    /**
     * Instance creation manager creates new Citrus instances or always a singleton based on instance creation strategy.
     */
    public static class CitrusInstanceManager {

        /** Singleton */
        private static Citrus citrus;

        /** List of instance resolvers capable of taking part in Citrus instance creation process */
        private static List<InstanceProcessor> instanceProcessors = new ArrayList<>();

        /**
         * Add instance processor.
         * @param processor
         */
        public static void addInstanceProcessor(InstanceProcessor processor) {
            instanceProcessors.add(processor);
        }

        /**
         * Initializing method loads Spring application context and reads bean definitions
         * such as test listeners and test context factory.
         * @return
         */
        public static Citrus newInstance() {
            if (strategy.equals(InstanceStrategy.NEW)) {
                Citrus instance = newInstance(new AnnotationConfigApplicationContext(CitrusSpringConfig.class));
                instanceProcessors.forEach(processor -> processor.process(instance));
                return instance;
            } else if (citrus == null) {
                citrus = newInstance(new AnnotationConfigApplicationContext(CitrusSpringConfig.class));
                instanceProcessors.forEach(processor -> processor.process(citrus));
            }

            return citrus;
        }

        /**
         * Initializing method with Spring application context Java configuration class
         * that gets loaded as application context.
         * @return
         */
        public static Citrus newInstance(Class<? extends CitrusSpringConfig> configClass) {
            if (strategy.equals(InstanceStrategy.NEW)) {
                Citrus instance = newInstance(new AnnotationConfigApplicationContext(configClass));
                instanceProcessors.forEach(processor -> processor.process(instance));
                return instance;
            } else if (citrus == null) {
                citrus = newInstance(new AnnotationConfigApplicationContext(configClass));
                instanceProcessors.forEach(processor -> processor.process(citrus));
            }

            return citrus;
        }

        /**
         * Create new Citrus instance with given Spring bean application context.
         * @param applicationContext
         * @return
         */
        public static Citrus newInstance(ApplicationContext applicationContext) {
            if (strategy.equals(InstanceStrategy.NEW)) {
                Citrus instance = new Citrus(applicationContext);
                instanceProcessors.forEach(processor -> processor.process(instance));
                return instance;
            } else if (citrus == null) {
                citrus = new Citrus(applicationContext);
                instanceProcessors.forEach(processor -> processor.process(citrus));
            }

            return citrus;
        }

        /**
         * Gets the singleton instance of Citrus.
         * @return
         */
        public static Citrus getSingleton() {
            return citrus;
        }
    }

    /** Test context factory **/
    private TestContextFactory testContextFactory;
    private TestSuiteListeners testSuiteListener;
    private TestListeners testListener;

    private Collection<SequenceBeforeSuite> beforeSuite;
    private Collection<SequenceAfterSuite> afterSuite;

    /** Basic Spring application context */
    private ApplicationContext applicationContext;

    /**
     * Private constructor with Spring bean application context that holds all basic Citrus
     * components needed to run a Citrus project.
     * @param applicationContext
     */
    private Citrus(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        this.testListener = applicationContext.getBean(TestListeners.class);
        this.testSuiteListener = applicationContext.getBean(TestSuiteListeners.class);
        this.testContextFactory = applicationContext.getBean(TestContextFactory.class);
        this.beforeSuite = applicationContext.getBeansOfType(SequenceBeforeSuite.class).values();
        this.afterSuite = applicationContext.getBeansOfType(SequenceAfterSuite.class).values();
    }

    /**
     * Initializing method loads Spring application context and reads bean definitions
     * such as test listeners and test context factory.
     * @return
     */
    public static Citrus newInstance() {
        return CitrusInstanceManager.newInstance(new AnnotationConfigApplicationContext(CitrusSpringConfig.class));
    }

    /**
     * Initializing method with Spring application context Java configuration class
     * that gets loaded as application context.
     * @return
     */
    public static Citrus newInstance(Class<? extends CitrusSpringConfig> configClass) {
        return CitrusInstanceManager.newInstance(new AnnotationConfigApplicationContext(configClass));
    }

    /**
     * Create new Citrus instance with given Spring bean application context.
     * @param applicationContext
     * @return
     */
    public static Citrus newInstance(ApplicationContext applicationContext) {
        return CitrusInstanceManager.newInstance(applicationContext);
    }

    /**
     * Performs before suite test actions.
     * @param suiteName
     * @param testGroups
     */
    public void beforeSuite(String suiteName, String ... testGroups) {
        testSuiteListener.onStart();

        if (!CollectionUtils.isEmpty(beforeSuite)) {
            for (SequenceBeforeSuite sequenceBeforeSuite : beforeSuite) {
                try {
                    if (sequenceBeforeSuite.shouldExecute(suiteName, testGroups)) {
                        sequenceBeforeSuite.execute(createTestContext());
                    }
                } catch (Exception e) {
                    testSuiteListener.onStartFailure(e);
                    afterSuite(suiteName, testGroups);

                    throw new AssertionError("Before suite failed with errors", e);
                }
            }

            testSuiteListener.onStartSuccess();
        } else {
            testSuiteListener.onStartSuccess();
        }
    }

    /**
     * Performs after suite test actions.
     * @param suiteName
     * @param testGroups
     */
    public void afterSuite(String suiteName, String ... testGroups) {
        testSuiteListener.onFinish();

        if (!CollectionUtils.isEmpty(afterSuite)) {
            for (SequenceAfterSuite sequenceAfterSuite : afterSuite) {
                try {
                    if (sequenceAfterSuite.shouldExecute(suiteName, testGroups)) {
                        sequenceAfterSuite.execute(createTestContext());
                    }
                } catch (Exception e) {
                    testSuiteListener.onFinishFailure(e);
                    throw new AssertionError("After suite failed with errors", e);
                }
            }

            testSuiteListener.onFinishSuccess();
        } else {
            testSuiteListener.onFinishSuccess();
        }
    }

    /**
     * Gets set of file name patterns for XML test files.
     * @return
     */
    public static Set<String> getXmlTestFileNamePattern() {
        return StringUtils.commaDelimitedListToSet(CitrusSettings.XML_TEST_FILE_NAME_PATTERN);
    }

    /**
     * Gets set of file name patterns for Java test files.
     * @return
     */
    public static Set<String> getJavaTestFileNamePattern() {
        return StringUtils.commaDelimitedListToSet(CitrusSettings.JAVA_TEST_FILE_NAME_PATTERN);
    }

    /**
     * Runs a test action which can also be a whole test case.
     */
    public void run(TestAction action) {
        run(action, createTestContext());
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
     * Creates a new test context.
     * @return the new citrus test context.
     */
    public TestContext createTestContext() {
        return testContextFactory.getObject();
    }

    /**
     * Gets the basic Citrus Spring bean application context.
     * @return
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Gets the Citrus version from classpath resource properties.
     * @return
     */
    public static String getVersion() {
        return CitrusVersion.version();
    }

    /**
     * Adds new test suite listener.
     * @param suiteListener
     */
    public void addTestSuiteListener(TestSuiteListener suiteListener) {
        this.testSuiteListener.addTestSuiteListener(suiteListener);
    }

    /**
     * Adds new test listener.
     * @param testListener
     */
    public void addTestListener(TestListener testListener) {
        this.testListener.addTestListener(testListener);
    }

    /**
     * Sets the instance creation strategy.
     * @param mode
     */
    public static void mode(InstanceStrategy mode) {
        strategy = mode;
    }

    /**
     * Closing Citrus and its application context.
     */
    public void close() {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            if (((ConfigurableApplicationContext) applicationContext).isActive()) {
                ((ConfigurableApplicationContext) applicationContext).close();
            }
        }
    }
}
