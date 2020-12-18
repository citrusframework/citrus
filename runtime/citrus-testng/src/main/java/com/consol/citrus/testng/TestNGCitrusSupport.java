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

package com.consol.citrus.testng;

import java.lang.reflect.Method;
import java.util.Date;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusContext;
import com.consol.citrus.GherkinTestActionRunner;
import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.TestBehavior;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestContext;
import org.testng.ITestResult;

/**
 * Basic Citrus TestNG support base class automatically handles test case runner creation. Also provides method parameter resolution
 * and resource injection. Users can just extend this class and make use of the action runner methods provided in {@link com.consol.citrus.TestActionRunner}
 * and {@link GherkinTestActionRunner}.
 *
 * @author Christoph Deppisch
 */
public class TestNGCitrusSupport implements IHookable, TestNGSuiteListener, GherkinTestActionRunner {

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
                run(testResult, method, testResult.getMethod().getCurrentInvocationCount());
            } catch (Exception e) {
                testResult.setThrowable(e);
                testResult.setStatus(ITestResult.FAILURE);
            }

            if (testResult.getThrowable() != null) {
                if (testResult.getThrowable() instanceof RuntimeException) {
                    throw (RuntimeException) testResult.getThrowable();
                } else {
                    throw new CitrusRuntimeException(testResult.getThrowable());
                }
            }
        } else if (method != null && method.getAnnotation(CitrusXmlTest.class) != null) {
            throw new CitrusRuntimeException("Unsupported XML test annotation - please add Spring support");
        } else {
            callBack.runTestMethod(testResult);
        }
    }

    /**
     * Run method prepares and executes test case.
     * @param testResult
     * @param method
     * @param invocationCount
     */
    protected void run(ITestResult testResult, Method method, int invocationCount) {
        if (method != null && method.getAnnotation(CitrusXmlTest.class) != null) {
            throw new CitrusRuntimeException("Unsupported XML test annotation - please add Spring support");
        } else {
            try {
                if (citrus == null) {
                    citrus = Citrus.newInstance();
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
        citrus = Citrus.newInstance();
        beforeSuite(citrus.getCitrusContext());
        citrus.beforeSuite(testContext.getSuite().getName(), testContext.getIncludedGroups());
    }

    /**
     * Subclasses may add before suite actions on the provided context.
     * @param context the Citrus context.
     */
    protected void beforeSuite(CitrusContext context) {
    }

    @Override
    public void afterSuite(ITestContext testContext) {
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
