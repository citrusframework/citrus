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

import java.lang.reflect.Method;

import com.consol.citrus.DefaultTestCase;
import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.GherkinTestActionRunner;
import com.consol.citrus.TestActionRunner;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.TestResult;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.exceptions.TestCaseFailedException;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

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
public class CitrusSupport extends CitrusBaseExtension implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        if (!isXmlTestMethod(extensionContext.getRequiredTestMethod())) {
            TestCase testCase = getTestCase(extensionContext);
            testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), throwable));
        }

        throw throwable;
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        if (!isXmlTestMethod(extensionContext.getRequiredTestMethod())) {
            TestCase testCase = getTestCase(extensionContext);

            extensionContext.getExecutionException()
                    .ifPresent(e -> testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), e)));

            getTestRunner(extensionContext).stop();

            extensionContext.getRoot().getStore(NAMESPACE).remove(getBaseKey(extensionContext) + TestCaseRunner.class.getSimpleName());
        }

        super.afterTestExecution(extensionContext);
    }

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        if (isXmlTestMethod(extensionContext.getRequiredTestMethod())) {
            super.beforeTestExecution(extensionContext);
        }

        TestCaseRunner testRunner = getTestRunner(extensionContext);
        CitrusAnnotations.injectTestRunner(extensionContext.getRequiredTestInstance(), testRunner);
        CitrusAnnotations.injectTestActionRunner(extensionContext.getRequiredTestInstance(), testRunner);
        CitrusAnnotations.injectGherkinTestActionRunner(extensionContext.getRequiredTestInstance(), testRunner);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        if (isXmlTestMethod(extensionContext.getRequiredTestMethod())) {
            super.beforeEach(extensionContext);
        } else {
            getTestContext(extensionContext);
            TestCase testCase = getTestCase(extensionContext);
            TestCaseRunner testRunner = getTestRunner(extensionContext);

            try {
                testRunner.start();
            } catch (Exception | AssertionError e) {
                getTestCase(extensionContext).setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), e));
                throw new TestCaseFailedException(e);
            }
        }
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        TestCaseRunner storedBuilder = getTestRunner(extensionContext);
        if (TestCaseRunner.class.isAssignableFrom(parameterContext.getParameter().getType())) {
            return storedBuilder;
        } else if (TestActionRunner.class.isAssignableFrom(parameterContext.getParameter().getType())
                && storedBuilder instanceof TestActionRunner) {
            return storedBuilder;
        } else if (GherkinTestActionRunner.class.isAssignableFrom(parameterContext.getParameter().getType())
                && storedBuilder instanceof GherkinTestActionRunner) {
            return storedBuilder;
        }

        return super.resolveParameter(parameterContext, extensionContext);
    }

    /**
     * Get the {@link TestCaseRunner} associated with the supplied {@code ExtensionContext} and its required test class name.
     * @return the {@code TestCaseRunner} (never {@code null})
     */
    protected static TestCaseRunner getTestRunner(ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");

        return extensionContext.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(getBaseKey(extensionContext) + TestCaseRunner.class.getSimpleName(), key -> {
            String testName = extensionContext.getRequiredTestClass().getSimpleName() + "." + extensionContext.getRequiredTestMethod().getName();

            if (extensionContext.getRequiredTestMethod().getAnnotation(CitrusTest.class) != null) {
                CitrusTest citrusTestAnnotation = extensionContext.getRequiredTestMethod().getAnnotation(CitrusTest.class);
                if (StringUtils.hasText(citrusTestAnnotation.name())) {
                    testName = citrusTestAnnotation.name();
                }
            }

            TestCaseRunner testCaseRunner = new DefaultTestCaseRunner(new DefaultTestCase(), getTestContext(extensionContext));
            testCaseRunner.testClass(extensionContext.getRequiredTestClass());
            testCaseRunner.name(testName);
            testCaseRunner.packageName(extensionContext.getRequiredTestClass().getPackage().getName());
            return testCaseRunner;
        }, TestCaseRunner.class);
    }

    /**
     * Get the {@link TestCase} associated with the supplied {@code ExtensionContext} and its required test class name.
     * @return the {@code TestCase} (never {@code null})
     */
    protected static TestCase getTestCase(ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");
        return extensionContext.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(getBaseKey(extensionContext) + TestCase.class.getSimpleName(), key -> {
            if (isXmlTestMethod(extensionContext.getRequiredTestMethod())) {
                return CitrusBaseExtension.getXmlTestCase(extensionContext);
            } else {
                return getTestRunner(extensionContext).getTestCase();
            }
        }, TestCase.class);
    }

    /**
     * Checks for {@link CitrusXmlTest} annotations or {@link TestFactory} annotations on test method.
     * @param method
     * @return
     */
    private static boolean isXmlTestMethod(Method method) {
        return method.isAnnotationPresent(CitrusXmlTest.class) || method.isAnnotationPresent(TestFactory.class);
    }
}
