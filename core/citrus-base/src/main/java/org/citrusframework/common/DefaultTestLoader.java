/*
 * Copyright 2022 the original author or authors.
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

package org.citrusframework.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusContext;
import org.citrusframework.DefaultTestCase;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.TestCaseRunnerFactory;
import org.citrusframework.TestResult;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;

/**
 * Default test loader implementation takes case on test names/packages and initializes the test runner if applicable.
 * Also loads the test case and provides it to registered test handlers. This way a test case can be loaded from different sources
 * like Java code, Groovy code, XML, Json, YAML, etc.
 * @author Christoph Deppisch
 */
public class DefaultTestLoader implements TestLoader {

    @CitrusFramework
    protected Citrus citrus;

    @CitrusResource
    protected CitrusContext citrusContext;

    @CitrusResource
    protected TestContext context;

    @CitrusResource
    protected TestCaseRunner runner;

    protected TestCase testCase;

    protected Class<?> testClass;
    protected String testName;
    protected String packageName;

    protected final List<Consumer<TestCase>> configurer = new ArrayList<>();
    protected final List<Consumer<TestCase>> handler = new ArrayList<>();

    /**
     * Default constructor for full control over the loader.
     **/
    public DefaultTestLoader() {
        // Empty default constructor
    }

    /**
     * Constructor with context file and parent application context field for simple initialisation.
     **/
    public DefaultTestLoader(Class<?> testClass, String testName, String packageName,
        CitrusContext citrusContext) {
        this.testClass = testClass;
        this.testName = testName;
        this.packageName = packageName;
        this.citrusContext = citrusContext;
    }

    @Override
    public final void load() {
        if (testCase != null) {
            return;
        }

        initializeTestRunner();

        try {
            doLoad();
        } catch (TestCaseFailedException e) {
            // This kind of exception indicates that the error has already been handled. Just throw and end test run.
            throw e;
        } catch (Exception | AssertionError e) {
            if (testCase == null) {
                testCase = runner.getTestCase();
            }

            testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), e));
            throw new TestCaseFailedException(e);
        }  finally {
            runner.stop();
        }
    }

    /**
     * Subclasses are supposed to overwrite this method on order to add logic how to load the test case (e.g. from XML, Json, YAML).
     */
    protected void doLoad() {
        testCase = runner.getTestCase();
        configurer.forEach(it -> it.accept(testCase));
        runner.start();
        handler.forEach(it -> it.accept(testCase));
    }

    /**
     * Safely initialize default test runner if applicable.
     */
    protected void initializeTestRunner() {
        if (runner == null) {
            if (context == null) {
                if (citrusContext == null) {
                    if (citrus == null) {
                        throw new CitrusRuntimeException(
                            "Missing Citrus framework instance for loading test");
                    }

                    citrusContext = citrus.getCitrusContext();
                }

                context = citrusContext.createTestContext();
            }

            if (testCase == null) {
                testCase = new DefaultTestCase();
            }

            runner = TestCaseRunnerFactory.createRunner(testCase, context);
        }

        if (testClass == null) {
            testClass = runner.getTestCase().getTestClass();
        } else {
            runner.testClass(testClass);
        }

        if (testName == null) {
            testName = runner.getTestCase().getName();
        } else {
            runner.name(testName);
        }

        if (packageName == null) {
            packageName = runner.getTestCase().getPackageName();
        } else {
            runner.packageName(packageName);
        }
    }

    public void setCitrus(Citrus citrus) {
        this.citrus = citrus;
    }

    public DefaultTestLoader citrus(Citrus citrus) {
        setCitrus(citrus);
        return this;
    }

    public void setCitrusContext(CitrusContext citrusContext) {
        this.citrusContext = citrusContext;
    }

    public DefaultTestLoader citrusContext(CitrusContext citrusContext) {
        setCitrusContext(citrusContext);
        return this;
    }

    public void setContext(TestContext context) {
        this.context = context;
    }

    public DefaultTestLoader context(TestContext context) {
        setContext(context);
        return this;
    }

    public void setRunner(TestCaseRunner runner) {
        this.runner = runner;
    }

    public DefaultTestLoader runner(TestCaseRunner runner) {
        setRunner(runner);
        return this;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    public DefaultTestLoader testCase(TestCase testCase) {
        setTestCase(testCase);
        return this;
    }

    @Override
    public TestCase getTestCase() {
        return testCase;
    }

    @Override
    public void setTestClass(Class<?> testClass) {
        this.testClass = testClass;
    }

    public DefaultTestLoader testClass(Class<?> testClass) {
        setTestClass(testClass);
        return this;
    }

    @Override
    public void setTestName(String testName) {
        this.testName = testName;
    }

    public DefaultTestLoader testName(String testName) {
        setTestName(testName);
        return this;
    }


    @Override
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public DefaultTestLoader packageName(String packageName) {
        setPackageName(packageName);
        return this;
    }

    @Override
    public void doWithTestCase(Consumer<TestCase> handler) {
        this.handler.add(handler);
    }

    @Override
    public void configureTestCase(Consumer<TestCase> configurer) {
        this.configurer.add(configurer);
    }
}
