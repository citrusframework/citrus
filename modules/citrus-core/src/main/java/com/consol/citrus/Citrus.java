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

import com.consol.citrus.config.CitrusBaseConfig;
import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.container.SequenceAfterSuite;
import com.consol.citrus.container.SequenceBeforeSuite;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.TestSuiteListeners;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

/**
 * Citrus main class initializes a new Citrus runtime environment with a Spring application context. Provides before/after suite action execution
 * and test execution methods.
 *
 * @author Christoph Deppisch
 * @since 2.1
 */
public final class Citrus {

    /** Citrus version */
    private static String version;

    /** Test context factory **/
    private TestContextFactory testContextFactory;
    private TestSuiteListeners testSuiteListener;

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

    /**
     * Private constructor with Spring bean application context that holds all basic Citrus
     * components needed to run a Citrus project.
     * @param applicationContext
     */
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
    public static Citrus newInstance() {
        return newInstance(new AnnotationConfigApplicationContext(CitrusSpringConfig.class));
    }

    /**
     * Initializing method with Spring application context Java configuration class
     * that gets loaded as application context.
     * @return
     */
    public static Citrus newInstance(Class<? extends CitrusBaseConfig> configClass) {
        return newInstance(new AnnotationConfigApplicationContext(configClass));
    }

    /**
     * Create new Citrus instance with given Spring bean application context.
     * @param applicationContext
     * @return
     */
    public static Citrus newInstance(ApplicationContext applicationContext) {
        return new Citrus(applicationContext);
    }

    /**
     * Performs before suite test actions.
     * @param suiteName
     * @param testGroups
     */
    public void beforeSuite(String suiteName, String ... testGroups) {
        if (!CollectionUtils.isEmpty(beforeSuite)) {
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
        if (!CollectionUtils.isEmpty(afterSuite)) {
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
     * Gets the endpoint from Spring application context.
     * @param name
     * @return
     */
    public Endpoint getEndpoint(String name) {
        return getEndpoint(name, Endpoint.class);
    }

    /**
     * Gets the endpoint from Spring application context with given type.
     * @param requiredType
     * @return
     */
    public <T extends Endpoint> T getEndpoint(Class<T> requiredType) {
        try {
            return getApplicationContext().getBean(requiredType);
        } catch (NoSuchBeanDefinitionException e) {
            throw new CitrusRuntimeException(String.format("Unable to find endpoint for type '%s'", requiredType));
        }
    }

    /**
     * Gets the endpoint from Spring application context with given type.
     * @param name
     * @param requiredType
     * @return
     */
    public <T extends Endpoint> T getEndpoint(String name, Class<T> requiredType) {
        try {
            return getApplicationContext().getBean(name, requiredType);
        } catch (NoSuchBeanDefinitionException e) {
            throw new CitrusRuntimeException(String.format("Unable to find endpoint for name '%s'", name));
        }
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
        return version;
    }
}
