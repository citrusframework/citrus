/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.junit.jupiter;

import com.consol.citrus.Citrus;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.TestResult;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestCaseFailedException;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

/**
 * JUnit5 extension adding {@link TestCaseRunner} support as well as Citrus annotation based resource injection
 * and lifecycle management such as before/after suite.
 *
 * Extension resolves method parameter of type {@link com.consol.citrus.context.TestContext}, {@link TestCaseRunner}
 * or {@link com.consol.citrus.TestActionRunner} and injects endpoints and resources coming from Citrus Spring application context that
 * is automatically loaded at suite start up. After suite automatically includes Citrus report generation.
 *
 * Extension is based on Citrus Xml test extension that also allows to load test cases from external Spring configuration files.
 *
 * @author Christoph Deppisch
 */
public class CitrusExtension implements BeforeAllCallback, BeforeEachCallback, BeforeTestExecutionCallback,
        AfterTestExecutionCallback, ParameterResolver,
        TestInstancePostProcessor, TestExecutionExceptionHandler {

    /** Test suite name */
    private static final String SUITE_NAME = "citrus-junit5-suite";

    private static Citrus citrus;
    private static boolean beforeSuite = true;
    private static boolean afterSuite = true;

    /**
     * {@link ExtensionContext.Namespace} in which Citrus related objects are stored keyed by test class.
     */
    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(CitrusExtension.class);

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        if (CitrusExtensionHelper.requiresCitrus(extensionContext)) {
            CitrusExtensionHelper.setCitrus(getCitrus(), extensionContext);
        } else {
            citrus = CitrusExtensionHelper.getCitrus(extensionContext);
        }

        if (beforeSuite) {
            beforeSuite = false;
            CitrusExtensionHelper.getCitrus(extensionContext).beforeSuite(SUITE_NAME);
        }

        if (afterSuite) {
            afterSuite = false;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> CitrusExtensionHelper.getCitrus(extensionContext).afterSuite(SUITE_NAME)));
        }
    }

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        if (!CitrusExtensionHelper.isXmlTestMethod(extensionContext.getRequiredTestMethod())) {
            TestCase testCase = CitrusExtensionHelper.getTestCase(extensionContext);
            testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), throwable));
        }

        throw throwable;
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) {
        extensionContext.getRoot().getStore(NAMESPACE).remove(CitrusExtensionHelper.getBaseKey(extensionContext) + TestContext.class.getSimpleName());
        extensionContext.getRoot().getStore(NAMESPACE).remove(CitrusExtensionHelper.getBaseKey(extensionContext) + TestCase.class.getSimpleName());
    }

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) {
        TestCaseRunner testRunner = CitrusExtensionHelper.getTestRunner(extensionContext);
        CitrusAnnotations.injectTestRunner(extensionContext.getRequiredTestInstance(), testRunner);
        CitrusAnnotations.injectTestContext(extensionContext.getRequiredTestInstance(), CitrusExtensionHelper.getTestContext(extensionContext));
        CitrusAnnotations.injectEndpoints(extensionContext.getRequiredTestInstance(), CitrusExtensionHelper.getTestContext(extensionContext));
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        CitrusExtensionHelper.getTestContext(extensionContext);

        TestCase testCase = CitrusExtensionHelper.getTestCase(extensionContext);
        TestCaseRunner testRunner = CitrusExtensionHelper.getTestRunner(extensionContext);

        try {
            testRunner.start();
        } catch (Exception | AssertionError e) {
            CitrusExtensionHelper.getTestCase(extensionContext).setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), e));
            throw new TestCaseFailedException(e);
        }
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) {
        CitrusAnnotations.injectCitrusFramework(testInstance, CitrusExtensionHelper.getCitrus(extensionContext));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().isAnnotationPresent(CitrusResource.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return CitrusExtensionHelper.resolveParameter(parameterContext, extensionContext);
    }

    /**
     * Initialize and get Citrus instance.
     * @return
     */
    protected Citrus getCitrus() {
        if (citrus == null) {
            citrus = Citrus.newInstance();
        }

        return citrus;
    }
}
