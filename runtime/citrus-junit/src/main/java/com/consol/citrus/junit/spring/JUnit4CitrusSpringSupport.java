/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.junit.spring;

import java.lang.annotation.Annotation;
import java.util.Date;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusSpringContext;
import com.consol.citrus.DefaultTestCase;
import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.GherkinTestActionRunner;
import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.TestActionRunner;
import com.consol.citrus.TestBehavior;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.TestResult;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.common.TestLoader;
import com.consol.citrus.common.XmlTestLoader;
import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.junit.CitrusFrameworkMethod;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.util.ReflectionUtils;

/**
 * Base test implementation for test cases that use JUnit testing framework. Class also provides
 * test listener support and loads the Spring root application context files for Citrus.
 *
 * @author Christoph Deppisch
 */
@RunWith(CitrusSpringJUnit4Runner.class)
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class JUnit4CitrusSpringSupport extends AbstractJUnit4SpringContextTests implements GherkinTestActionRunner {

    /** Logger */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final String BUILDER_ATTRIBUTE = "builder";

    /** Citrus instance */
    protected Citrus citrus;

    /** Test builder delegate */
    private TestCaseRunner testCaseRunner;

    /**
     * Reads Citrus test annotation from framework method and executes test case.
     * @param frameworkMethod
     */
    protected void run(CitrusFrameworkMethod frameworkMethod) {
        if (citrus == null) {
            citrus = Citrus.newInstance(CitrusSpringContext.create(applicationContext));
        }

        TestContext ctx = prepareTestContext(citrus.getCitrusContext().createTestContext());

        if (frameworkMethod.getMethod().getAnnotation(CitrusXmlTest.class) != null) {
            TestLoader testLoader = createTestLoader(frameworkMethod.getTestName(), frameworkMethod.getPackageName());
            TestCase testCase = testLoader.load();

            citrus.run(testCase, ctx);
        } else if (frameworkMethod.getMethod().getAnnotation(CitrusTest.class) != null) {
            TestCaseRunner testCaseBuilder = createTestRunner(frameworkMethod, ctx);
            frameworkMethod.setAttribute(BUILDER_ATTRIBUTE, testCaseBuilder);
            CitrusAnnotations.injectAll(this, citrus, ctx);

            invokeTestMethod(frameworkMethod, testCaseBuilder, ctx);
        }
    }

    /**
     * Invokes test method based on designer or runner environment.
     * @param frameworkMethod
     * @param testCaseBuilder
     * @param context
     */
    protected void invokeTestMethod(CitrusFrameworkMethod frameworkMethod, TestCaseRunner testCaseBuilder, TestContext context) {
        final TestCase testCase = testCaseBuilder.getTestCase();
        try {
            Object[] params = resolveParameter(frameworkMethod, testCase, context);
            testCaseBuilder.start();
            ReflectionUtils.invokeMethod(frameworkMethod.getMethod(), this, params);
        } catch (Exception | AssertionError e) {
            testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), e));
            throw new TestCaseFailedException(e);
        } finally {
            testCaseBuilder.stop();
        }
    }

    /**
     * Resolves value for annotated method parameter.
     *
     * @param frameworkMethod
     * @param parameterType
     * @return
     */
    protected Object resolveAnnotatedResource(CitrusFrameworkMethod frameworkMethod, Class<?> parameterType, TestContext context) {
        Object storedBuilder = frameworkMethod.getAttribute(BUILDER_ATTRIBUTE);
        if (TestCaseRunner.class.isAssignableFrom(parameterType)) {
            return storedBuilder;
        } else if (TestActionRunner.class.isAssignableFrom(parameterType)
                && storedBuilder instanceof TestActionRunner) {
            return storedBuilder;
        } else if (GherkinTestActionRunner.class.isAssignableFrom(parameterType)
                && storedBuilder instanceof GherkinTestActionRunner) {
            return storedBuilder;
        } else if (TestContext.class.isAssignableFrom(parameterType)) {
            return context;
        } else {
            throw new CitrusRuntimeException("Not able to provide a Citrus resource injection for type " + parameterType);
        }
    }

    /**
     * Resolves method arguments supporting TestNG data provider parameters as well as
     * {@link CitrusResource} annotated methods.
     *
     * @param frameworkMethod
     * @param testCase
     * @param context
     * @return
     */
    protected Object[] resolveParameter(CitrusFrameworkMethod frameworkMethod, TestCase testCase, TestContext context) {
        Object[] values = new Object[frameworkMethod.getMethod().getParameterTypes().length];
        Class<?>[] parameterTypes = frameworkMethod.getMethod().getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            final Annotation[] parameterAnnotations = frameworkMethod.getMethod().getParameterAnnotations()[i];
            Class<?> parameterType = parameterTypes[i];
            for (Annotation annotation : parameterAnnotations) {
                if (annotation instanceof CitrusResource) {
                    values[i] = resolveAnnotatedResource(frameworkMethod, parameterType, context);
                }
            }
        }

        return values;
    }

    /**
     * Prepares the test context.
     *
     * Provides a hook for test context modifications before the test gets executed.
     *
     * @param testContext the test context.
     * @return the (prepared) test context.
     */
    protected TestContext prepareTestContext(final TestContext testContext) {
        return testContext;
    }

    /**
     * Creates new test loader which has TestNG test annotations set for test execution. Only
     * suitable for tests that get created at runtime through factory method. Subclasses
     * may overwrite this in order to provide custom test loader with custom test annotations set.
     * @param testName
     * @param packageName
     * @return
     */
    protected TestLoader createTestLoader(String testName, String packageName) {
        return new XmlTestLoader(getClass(), testName, packageName, CitrusSpringContext.create(applicationContext));
    }

    /**
     * Constructs the test case to execute.
     * @return
     */
    protected TestCase getTestCase() {
        return createTestLoader(this.getClass().getSimpleName(), this.getClass().getPackage().getName()).load();
    }

    /**
     * Creates new test runner instance for this test method.
     * @param frameworkMethod
     * @param context
     * @return
     */
    protected TestCaseRunner createTestRunner(CitrusFrameworkMethod frameworkMethod, TestContext context) {
        testCaseRunner = new DefaultTestCaseRunner(new DefaultTestCase(), context);
        testCaseRunner.testClass(this.getClass());
        testCaseRunner.name(frameworkMethod.getTestName());
        testCaseRunner.packageName(frameworkMethod.getPackageName());

        return testCaseRunner;
    }

    @Override
    public <T extends TestAction> T run(TestActionBuilder<T> builder) {
        return testCaseRunner.run(builder);
    }

    @Override
    public <T extends TestAction> TestActionBuilder<T> applyBehavior(TestBehavior behavior) {
        return testCaseRunner.applyBehavior(behavior);
    }

    public <T> T variable(String name, T value) {
        return testCaseRunner.variable(name, value);
    }

    public void name(String name) {
        testCaseRunner.name(name);
    }

    public void description(String description) {
        testCaseRunner.description(description);
    }

    public void author(String author) {
        testCaseRunner.author(author);
    }

    public void status(TestCaseMetaInfo.Status status) {
        testCaseRunner.status(status);
    }

    public void creationDate(Date date) {
        testCaseRunner.creationDate(date);
    }
}
