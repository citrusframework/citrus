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
import com.consol.citrus.CitrusSpringContext;
import com.consol.citrus.GherkinTestActionRunner;
import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.TestBehavior;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.TestGroupAware;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.common.TestLoader;
import com.consol.citrus.common.XmlTestLoader;
import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.PrepareTestNGMethodInterceptor;
import com.consol.citrus.testng.TestNGHelper;
import com.consol.citrus.testng.TestNGSuiteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.testng.IHookCallBack;
import org.testng.ITestContext;
import org.testng.ITestResult;
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
public class TestNGCitrusSpringSupport extends AbstractTestNGSpringContextTests implements GherkinTestActionRunner, TestNGSuiteListener {

    /** Logger */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** Citrus instance */
    protected Citrus citrus;

    /** Test builder delegate */
    private TestCaseRunner testCaseRunner;

    @Override
    public void run(final IHookCallBack callBack, ITestResult testResult) {
        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();

        if (method != null && method.getAnnotation(CitrusTest.class) != null) {
            try {
                run(testResult, method, null, testResult.getMethod().getCurrentInvocationCount());
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
        } else if (method != null && method.getAnnotation(CitrusXmlTest.class) != null) {
            List<TestLoader> methodTestLoaders = TestNGHelper.createTestLoadersForMethod(method, this::createTestLoader);

            if (!CollectionUtils.isEmpty(methodTestLoaders)) {
                try {
                    run(testResult, method, methodTestLoaders.get(testResult.getMethod().getCurrentInvocationCount() % methodTestLoaders.size()),
                            testResult.getMethod().getCurrentInvocationCount());
                } catch (Exception e) {
                    testResult.setThrowable(e);
                    testResult.setStatus(ITestResult.FAILURE);
                }
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
     * @param testLoader
     * @param invocationCount
     */
    protected void run(ITestResult testResult, Method method, TestLoader testLoader, int invocationCount) {
        if (method != null && method.getAnnotation(CitrusXmlTest.class) != null) {
            if (citrus == null) {
                citrus = Citrus.newInstance(CitrusSpringContext.create(applicationContext));
            }

            TestContext ctx = prepareTestContext(citrus.getCitrusContext().createTestContext());
            TestCase testCase = testLoader.load();

            if (testCase instanceof TestGroupAware) {
                ((TestGroupAware) testCase).setGroups(testResult.getMethod().getGroups());
            }

            TestNGHelper.invokeTestMethod(citrus, this, testResult, method, testCase, ctx, invocationCount);
        } else {
            try {
                if (citrus == null) {
                    citrus = Citrus.newInstance(CitrusSpringContext.create(applicationContext));
                }

                TestContext ctx = prepareTestContext(citrus.getCitrusContext().createTestContext());

                testCaseRunner = TestNGHelper.createTestCaseRunner(this, method, ctx);
                testCaseRunner.groups(testResult.getMethod().getGroups());
                testResult.setAttribute(TestNGHelper.BUILDER_ATTRIBUTE, testCaseRunner);

                CitrusAnnotations.injectAll(this, citrus, ctx);

                TestNGHelper.invokeTestMethod(this, testResult, method, testCaseRunner, ctx, invocationCount);
            } finally {
                testResult.removeAttribute(TestNGHelper.BUILDER_ATTRIBUTE);
            }
        }
    }

    @Override
    public void beforeSuite(ITestContext testContext) {
        try {
            springTestContextPrepareTestInstance();
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to initialize Spring test context", e);
        }
        Assert.notNull(applicationContext, "Missing proper application context in before suite initialization");

        citrus = Citrus.newInstance(CitrusSpringContext.create(applicationContext));
        citrus.beforeSuite(testContext.getSuite().getName(), testContext.getIncludedGroups());
    }

    @Override
    public void afterSuite(ITestContext testContext) {
        if (citrus != null) {
            citrus.afterSuite(testContext.getSuite().getName(), testContext.getIncludedGroups());
        }
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
        return new XmlTestLoader(getClass(), testName, packageName,
                Optional.ofNullable(citrus)
                        .map(Citrus::getCitrusContext)
                        .orElseGet(() -> CitrusSpringContext.create(applicationContext)));
    }

    /**
     * Constructs the test case to execute.
     * @return
     */
    protected TestCase getTestCase() {
        return createTestLoader(this.getClass().getSimpleName(), this.getClass().getPackage().getName()).load();
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
