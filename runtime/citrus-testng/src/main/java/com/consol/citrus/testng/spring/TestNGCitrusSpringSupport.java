/*
 * Copyright 2020 the original author or authors.
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

package com.consol.citrus.testng.spring;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusContext;
import com.consol.citrus.CitrusSpringContext;
import com.consol.citrus.CitrusSpringContextProvider;
import com.consol.citrus.GherkinTestActionRunner;
import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.TestBehavior;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.TestGroupAware;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusGroovyTest;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.common.TestLoader;
import com.consol.citrus.common.TestSourceAware;
import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.PrepareTestNGMethodInterceptor;
import com.consol.citrus.testng.TestNGHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.Assert;
import org.testng.IHookCallBack;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

/**
 * Basic Citrus TestNG support base class with Spring support automatically handles test case runner creation. Also provides method parameter resolution
 * and resource injection. Users can just extend this class and make use of the action runner methods provided in {@link com.consol.citrus.TestActionRunner}
 * and {@link GherkinTestActionRunner}. Provides Spring test listener support and
 * loads basic Spring application context for Citrus.
 *
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Listeners( { PrepareTestNGMethodInterceptor.class } )
public class TestNGCitrusSpringSupport extends AbstractTestNGSpringContextTests
        implements GherkinTestActionRunner {

    /** Logger */
    protected final Logger log = LoggerFactory.getLogger(getClass());

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
                method.getAnnotation(CitrusGroovyTest.class) != null ||
                method.getAnnotation(CitrusXmlTest.class) != null) {
            try {
                run(testResult, method, methodTestLoaders, testResult.getMethod().getCurrentInvocationCount());
            } catch (Exception e) {
                testResult.setThrowable(e);
                testResult.setStatus(ITestResult.FAILURE);
            }

            super.run(new TestNGHelper.FakeExecutionCallBack(callBack.getParameters()), testResult);

            if (testResult.getThrowable() != null) {
                if (testResult.getThrowable() instanceof RuntimeException) {
                    throw (RuntimeException) testResult.getThrowable();
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

        if (method.getAnnotation(CitrusXmlTest.class) != null) {
            if (!methodTestLoaders.isEmpty()) {
                TestContext ctx = prepareTestContext(citrus.getCitrusContext().createTestContext());
                testCase = methodTestLoaders.get(invocationCount % methodTestLoaders.size()).load();

                if (testCase instanceof TestGroupAware) {
                    ((TestGroupAware) testCase).setGroups(testResult.getMethod().getGroups());
                }

                TestNGHelper.invokeTestMethod(citrus, this, testResult, method, testCase, ctx, invocationCount);
            }
        } else {
            try {
                TestContext ctx = prepareTestContext(citrus.getCitrusContext().createTestContext());

                TestCaseRunner runner = TestNGHelper.createTestCaseRunner(this, method, ctx);
                runner.groups(testResult.getMethod().getGroups());
                testResult.setAttribute(TestNGHelper.BUILDER_ATTRIBUTE, runner);

                delegate = runner;

                CitrusAnnotations.injectAll(this, citrus, ctx);

                if (method.getAnnotation(CitrusGroovyTest.class) != null && !methodTestLoaders.isEmpty()) {
                    TestLoader testLoader = methodTestLoaders.get(invocationCount % methodTestLoaders.size());

                    if (testLoader instanceof TestSourceAware) {
                        String[] sources = method.getAnnotation(CitrusGroovyTest.class).sources();
                        if (sources.length > 0) {
                            ((TestSourceAware) testLoader).setSource(sources[0]);
                        }
                    }

                    CitrusAnnotations.injectAll(testLoader, citrus, ctx);
                    CitrusAnnotations.injectTestRunner(testLoader, runner);

                    testCase = testLoader.load();
                }

                TestNGHelper.invokeTestMethod(this, testResult, method, runner, ctx, invocationCount);
            } finally {
                testResult.removeAttribute(TestNGHelper.BUILDER_ATTRIBUTE);
            }
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
    public final void beforeSuite(ITestContext testContext) {
        try {
            springTestContextPrepareTestInstance();
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to initialize Spring test context", e);
        }
        Assert.notNull(applicationContext, "Missing proper application context in before suite initialization");

        citrus = Citrus.newInstance(new CitrusSpringContextProvider(applicationContext));
        CitrusAnnotations.injectCitrusFramework(this, citrus);
        beforeSuite(citrus.getCitrusContext());
        citrus.beforeSuite(testContext.getSuite().getName(), testContext.getIncludedGroups());
    }

    /**
     * Subclasses may add before suite actions on the provided context.
     * @param context the Citrus context.
     */
    protected void beforeSuite(CitrusContext context) {
    }

    @AfterSuite(alwaysRun = true)
    public final void afterSuite(ITestContext testContext) {
        if (citrus != null) {
            afterSuite(citrus.getCitrusContext());
            citrus.afterSuite(testContext.getSuite().getName(), testContext.getIncludedGroups());
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
