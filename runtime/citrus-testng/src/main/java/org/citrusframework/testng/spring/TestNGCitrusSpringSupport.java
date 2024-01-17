/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.testng.spring;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusContext;
import org.citrusframework.CitrusSpringContext;
import org.citrusframework.CitrusSpringContextProvider;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestBehavior;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.TestGroupAware;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.DefaultTestLoader;
import org.citrusframework.common.TestLoader;
import org.citrusframework.common.TestSourceAware;
import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testng.TestNGHelper;
import org.citrusframework.util.ObjectHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.IHookCallBack;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

/**
 * Basic Citrus TestNG support base class with Spring support automatically handles test case runner creation. Also provides method parameter resolution
 * and resource injection. Users can just extend this class and make use of the action runner methods provided in {@link org.citrusframework.TestActionRunner}
 * and {@link GherkinTestActionRunner}. Provides Spring test listener support and
 * loads basic Spring application context for Citrus.
 *
 * @author Christoph Deppisch
 */
@Listeners( { TestNGCitrusSpringMethodInterceptor.class } )
@ContextConfiguration(classes =  {CitrusSpringConfig.class})
public class TestNGCitrusSpringSupport extends AbstractTestNGSpringContextTests
        implements GherkinTestActionRunner {

    /** Citrus instance */
    protected Citrus citrus;

    /** Test builder delegate */
    private TestCaseRunner delegate;
    private TestCase testCase;

    @Override
    public void run(final IHookCallBack callBack, ITestResult testResult) {
        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();

        if (method == null) {
            super.run(callBack, testResult);
            return;
        }

        List<TestLoader> methodTestLoaders = TestNGHelper.createMethodTestLoaders(method, this::createTestLoader);
        if (method.getAnnotation(CitrusTest.class) != null ||
                method.getAnnotation(CitrusTestSource.class) != null) {
            try {
                run(testResult, method, methodTestLoaders, testResult.getMethod().getCurrentInvocationCount());
                testResult.setStatus(ITestResult.SUCCESS);
            } catch (Exception e) {
                testResult.setThrowable(e);
                testResult.setStatus(ITestResult.FAILURE);
            }

            super.run(new TestNGHelper.FakeExecutionCallBack(callBack.getParameters()), testResult);

            if (testResult.getThrowable() != null) {
                if (testResult.getThrowable() instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                } else {
                    throw new CitrusRuntimeException(testResult.getThrowable());
                }
            }
        } else {
            super.run(callBack, testResult);
        }
    }

    /**
     * Run method prepares and executes test case.
     * @param testResult
     * @param method
     * @param methodTestLoaders
     * @param invocationCount
     */
    protected void run(ITestResult testResult, Method method, List<TestLoader> methodTestLoaders, int invocationCount) {
        if (citrus == null) {
            citrus = Citrus.newInstance(new CitrusSpringContextProvider(applicationContext));
            CitrusAnnotations.injectCitrusFramework(this, citrus);
        }

        try {
            TestContext ctx = prepareTestContext(citrus.getCitrusContext().createTestContext());

            TestCaseRunner runner = TestNGHelper.createTestCaseRunner(this, method, ctx);
            runner.groups(testResult.getMethod().getGroups());
            testResult.setAttribute(TestNGHelper.BUILDER_ATTRIBUTE, runner);

            delegate = runner;

            CitrusAnnotations.injectAll(this, citrus, ctx);

            TestLoader testLoader ;
            if (method.getAnnotation(CitrusTestSource.class) != null && !methodTestLoaders.isEmpty()) {
                testLoader = methodTestLoaders.get(invocationCount % methodTestLoaders.size());

                if (testLoader instanceof TestSourceAware testSourceAware) {
                    String[] sources = method.getAnnotation(CitrusTestSource.class).sources();
                    if (sources.length > 0) {
                        testSourceAware.setSource(sources[0]);
                    }
                }
            } else {
                testLoader = new DefaultTestLoader();
            }

            CitrusAnnotations.injectAll(testLoader, citrus, ctx);
            CitrusAnnotations.injectTestRunner(testLoader, runner);
            testLoader.configureTestCase(t -> {
                if (t instanceof TestGroupAware testGroupAware) {
                    testGroupAware.setGroups(testResult.getMethod().getGroups());
                }
            });
            testLoader.configureTestCase(t -> testCase = t);

            TestNGHelper.invokeTestMethod(this, testResult, method, testLoader, ctx, invocationCount);
        } finally {
            testResult.removeAttribute(TestNGHelper.BUILDER_ATTRIBUTE);
        }
    }

    @BeforeClass(alwaysRun = true)
    public final void before() {
        if (citrus == null) {
            citrus = Citrus.newInstance(new CitrusSpringContextProvider(applicationContext));
            CitrusAnnotations.injectCitrusFramework(this, citrus);
        }

        before(citrus.getCitrusContext());
    }

    /**
     * Subclasses may add before test actions on the provided context.
     * @param context the Citrus context.
     */
    protected void before(CitrusContext context) {
    }

    @AfterClass(alwaysRun = true)
    public final void after() {
        if (citrus != null) {
            after(citrus.getCitrusContext());
        }
    }

    /**
     * Subclasses may add after test actions on the provided context.
     * @param context the Citrus context.
     */
    protected void after(CitrusContext context) {
    }

    @BeforeSuite(alwaysRun = true)
    public final void beforeSuite() {
        try {
            springTestContextPrepareTestInstance();
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to initialize Spring test context", e);
        }
        ObjectHelper.assertNotNull(applicationContext, "Missing proper application context in before suite initialization");

        citrus = Citrus.newInstance(new CitrusSpringContextProvider(applicationContext));
        CitrusAnnotations.injectCitrusFramework(this, citrus);
        beforeSuite(citrus.getCitrusContext());
        citrus.beforeSuite(Reporter.getCurrentTestResult().getTestContext().getSuite().getName(),
                Reporter.getCurrentTestResult().getTestContext().getIncludedGroups());
    }

    /**
     * Subclasses may add before suite actions on the provided context.
     * @param context the Citrus context.
     */
    protected void beforeSuite(CitrusContext context) {
    }

    @AfterSuite(alwaysRun = true)
    public final void afterSuite() {
        if (citrus != null) {
            afterSuite(citrus.getCitrusContext());
            citrus.afterSuite(Reporter.getCurrentTestResult().getTestContext().getSuite().getName(),
                    Reporter.getCurrentTestResult().getTestContext().getIncludedGroups());
        }
    }

    /**
     * Subclasses may add after suite actions on the provided context.
     * @param context the Citrus context.
     */
    protected void afterSuite(CitrusContext context) {
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
    protected TestLoader createTestLoader(String testName, String packageName, String type) {
        TestLoader testLoader = TestLoader.lookup(type)
                .orElseThrow(() -> new CitrusRuntimeException(String.format("Missing test loader for type '%s'", type)));

        testLoader.setTestClass(getClass());
        testLoader.setTestName(testName);
        testLoader.setPackageName(packageName);

        CitrusAnnotations.injectCitrusContext(testLoader, Optional.ofNullable(citrus)
                .map(Citrus::getCitrusContext)
                .orElseGet(() -> CitrusSpringContext.create(applicationContext)));
        return testLoader;
    }

    /**
     * Constructs the test case to execute.
     * @return
     */
    protected TestCase getTestCase() {
        if (testCase != null) {
            return testCase;
        }

        if (delegate != null) {
            return delegate.getTestCase();
        }

        return null;
    }

    @Override
    public <T extends TestAction> T run(TestActionBuilder<T> builder) {
        return delegate.run(builder);
    }

    @Override
    public <T extends TestAction> TestActionBuilder<T> applyBehavior(TestBehavior behavior) {
        return delegate.applyBehavior(behavior);
    }

    public <T> T variable(String name, T value) {
        return delegate.variable(name, value);
    }

    public void name(String name) {
        delegate.name(name);
    }

    public void description(String description) {
        delegate.description(description);
    }

    public void author(String author) {
        delegate.author(author);
    }

    public void status(TestCaseMetaInfo.Status status) {
        delegate.status(status);
    }

    public void creationDate(Date date) {
        delegate.creationDate(date);
    }
}
