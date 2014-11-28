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

import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.container.SequenceAfterSuite;
import com.consol.citrus.container.SequenceBeforeSuite;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.report.TestSuiteListeners;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

/**
 * Citrus main class loads Spring application context and executes before/after suite actions.
 *
 * @author Christoph Deppisch
 * @since 2.0.1
 */
public final class Citrus {

    /** Citrus version */
    private static String version;

    private TestSuiteListeners testSuiteListener;

    private TestContextFactory testContextFactory;

    private Collection<SequenceBeforeSuite> beforeSuite;
    private Collection<SequenceAfterSuite> afterSuite;

    /** Basic Spring application context */
    private ApplicationContext applicationContext;

    /** Load Citrus version */
    static {
        Properties versionProperties = new Properties();

        try (final InputStream in = new ClassPathResource("META-INF/citrus.version").getInputStream()) {
            versionProperties.load(in);
        } catch (IOException e) {
            version = "";
        }

        version = versionProperties.get("citrus.version").toString();
    }

    private Citrus(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

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
    public static Citrus create() {
        return create(new AnnotationConfigApplicationContext(CitrusSpringConfig.class));
    }

    /**
     * Create new Citrus instance with given Spring bean application context.
     * @param applicationContext
     * @return
     */
    public static Citrus create(ApplicationContext applicationContext) {
        Citrus citrus = new Citrus(applicationContext);
        return citrus;
    }

    /**
     * Performs before suite test actions.
     * @param suiteName
     * @param testGroups
     */
    public void beforeSuite(String suiteName, String ... testGroups) {
        if (beforeSuite != null) {
            for (SequenceBeforeSuite sequenceBeforeSuite : beforeSuite) {
                try {
                    if (sequenceBeforeSuite.shouldExecute(suiteName, testGroups)) {
                        sequenceBeforeSuite.execute(createTestContext());
                    }
                } catch (Exception e) {
                    org.testng.Assert.fail("Before suite failed with errors", e);
                }
            }
        } else {
            testSuiteListener.onStart();
            testSuiteListener.onStartSuccess();
        }
    }

    /**
     * Performs after suite test actions.
     * @param suiteName
     * @param testGroups
     */
    public void afterSuite(String suiteName, String ... testGroups) {
        if (afterSuite != null) {
            for (SequenceAfterSuite sequenceAfterSuite : afterSuite) {
                try {
                    if (sequenceAfterSuite.shouldExecute(suiteName, testGroups)) {
                        sequenceAfterSuite.execute(createTestContext());
                    }
                } catch (Exception e) {
                    org.testng.Assert.fail("After suite failed with errors", e);
                }
            }
        } else {
            testSuiteListener.onFinish();
            testSuiteListener.onFinishSuccess();
        }
    }

    /**
     * Runs a test case.
     */
    public void run(TestCase testCase) {
        run(testCase, createTestContext());
    }

    /**
     * Runs test case with given test context.
     * @param testCase
     * @param testContext
     */
    public void run(TestCase testCase, TestContext testContext) {
        testCase.execute(testContext);
    }

    /**
     * Creates a new test context.
     * @return the new citrus test context.
     */
    public TestContext createTestContext() {
        return testContextFactory.getObject();
    }

    /**
     * Gets the Citrus version from classpath resource properties.
     * @return
     */
    public static String getVersion() {
        return version;
    }
}
