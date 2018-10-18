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

package com.consol.citrus.dsl.junit.jupiter;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestResult;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.annotations.CitrusDslAnnotations;
import com.consol.citrus.dsl.design.DefaultTestDesigner;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.DefaultTestRunner;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.junit.jupiter.CitrusBaseExtension;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.*;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * JUnit5 extension adding {@link TestRunner} and {@link TestDesigner} support as well as Citrus annotation based resource injection
 * and lifecycle management such as before/after suite.
 *
 * Extension resolves method parameter of type {@link TestContext}, {@link TestRunner} or {@link TestDesigner} and injects endpoints and resources coming
 * from Citrus Spring application context that is automatically loaded at suite start up. After suite automatically includes Citrus report generation.
 *
 * Extension is based on Citrus Xml test extension that also allows to load test cases from external Spring configuration files.
 *
 * @author Christoph Deppisch
 */
public class CitrusExtension extends CitrusBaseExtension implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        if (!isXmlTestMethod(extensionContext.getRequiredTestMethod()) &&
                (isRunnerMethod(extensionContext.getRequiredTestMethod()) || isRunnerClass(extensionContext.getRequiredTestClass()))) {
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

            if (isDesignerMethod(extensionContext.getRequiredTestMethod()) ||
                    isDesignerClass(extensionContext.getRequiredTestClass())) {
                TestContext context = getTestContext(extensionContext);
                getCitrus(extensionContext).run(testCase, context);
            } else if (isRunnerMethod(extensionContext.getRequiredTestMethod()) ||
                    isRunnerClass(extensionContext.getRequiredTestClass())) {
                getTestRunner(extensionContext).stop();
            }

            extensionContext.getRoot().getStore(NAMESPACE).remove(getBaseKey(extensionContext) + TestRunner.class.getSimpleName());
            extensionContext.getRoot().getStore(NAMESPACE).remove(getBaseKey(extensionContext) + TestDesigner.class.getSimpleName());
        }

        super.afterTestExecution(extensionContext);
    }

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        if (isXmlTestMethod(extensionContext.getRequiredTestMethod())) {
            super.beforeTestExecution(extensionContext);
        }

        CitrusDslAnnotations.injectTestDesigner(extensionContext.getRequiredTestInstance(), getTestDesigner(extensionContext));
        CitrusDslAnnotations.injectTestRunner(extensionContext.getRequiredTestInstance(), getTestRunner(extensionContext));
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        if (isXmlTestMethod(extensionContext.getRequiredTestMethod())) {
            super.beforeEach(extensionContext);
        } else {
            getTestContext(extensionContext);
            TestCase testCase = getTestCase(extensionContext);

            if (isRunnerMethod(extensionContext.getRequiredTestMethod()) || isRunnerClass(extensionContext.getRequiredTestClass())) {
                TestRunner testRunner = getTestRunner(extensionContext);

                try {
                    testRunner.start();
                } catch (Exception | AssertionError e) {
                    getTestCase(extensionContext).setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), e));
                    throw new TestCaseFailedException(e);
                }
            }
        }
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (TestDesigner.class.isAssignableFrom(parameterContext.getParameter().getType())) {
            return getTestDesigner(extensionContext);
        } else if (TestRunner.class.isAssignableFrom(parameterContext.getParameter().getType())) {
            return getTestRunner(extensionContext);
        }

        return super.resolveParameter(parameterContext, extensionContext);
    }

    /**
     * Get the {@link TestDesigner} associated with the supplied {@code ExtensionContext} and its required test class name.
     * @return the {@code TestDesigner} (never {@code null})
     */
    protected static TestDesigner getTestDesigner(ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");

        return extensionContext.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(getBaseKey(extensionContext) + TestDesigner.class.getSimpleName(), key -> {
            String testName = extensionContext.getRequiredTestClass().getSimpleName() + "." + extensionContext.getRequiredTestMethod().getName();

            if (extensionContext.getRequiredTestMethod().getAnnotation(CitrusTest.class) != null) {
                CitrusTest citrusTestAnnotation = extensionContext.getRequiredTestMethod().getAnnotation(CitrusTest.class);
                if (StringUtils.hasText(citrusTestAnnotation.name())) {
                    testName = citrusTestAnnotation.name();
                }
            }

            TestDesigner testDesigner = new DefaultTestDesigner(getCitrus(extensionContext).getApplicationContext(), getTestContext(extensionContext));
            testDesigner.testClass(extensionContext.getRequiredTestClass());
            testDesigner.name(testName);
            testDesigner.packageName(extensionContext.getRequiredTestClass().getPackage().getName());
            return testDesigner;
        }, TestDesigner.class);
    }

    /**
     * Get the {@link TestRunner} associated with the supplied {@code ExtensionContext} and its required test class name.
     * @return the {@code TestRunner} (never {@code null})
     */
    protected static TestRunner getTestRunner(ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");

        return extensionContext.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(getBaseKey(extensionContext) + TestRunner.class.getSimpleName(), key -> {
            String testName = extensionContext.getRequiredTestClass().getSimpleName() + "." + extensionContext.getRequiredTestMethod().getName();

            if (extensionContext.getRequiredTestMethod().getAnnotation(CitrusTest.class) != null) {
                CitrusTest citrusTestAnnotation = extensionContext.getRequiredTestMethod().getAnnotation(CitrusTest.class);
                if (StringUtils.hasText(citrusTestAnnotation.name())) {
                    testName = citrusTestAnnotation.name();
                }
            }

            TestRunner testRunner = new DefaultTestRunner(getCitrus(extensionContext).getApplicationContext(), getTestContext(extensionContext));
            testRunner.testClass(extensionContext.getRequiredTestClass());
            testRunner.name(testName);
            testRunner.packageName(extensionContext.getRequiredTestClass().getPackage().getName());
            return testRunner;
        }, TestRunner.class);
    }

    /**
     * Get the {@link TestCase} associated with the supplied {@code ExtensionContext} and its required test class name.
     * @return the {@code TestCase} (never {@code null})
     */
    protected static TestCase getTestCase(ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");
        return extensionContext.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(getBaseKey(extensionContext) + TestCase.class.getSimpleName(), key -> {
            if (isDesignerMethod(extensionContext.getRequiredTestMethod())) {
                return getTestDesigner(extensionContext).getTestCase();
            } else if (isRunnerMethod(extensionContext.getRequiredTestMethod())) {
                return getTestRunner(extensionContext).getTestCase();
            } else if (isXmlTestMethod(extensionContext.getRequiredTestMethod())) {
                return CitrusBaseExtension.getXmlTestCase(extensionContext);
            } else if (isDesignerClass(extensionContext.getRequiredTestClass())) {
                return getTestDesigner(extensionContext).getTestCase();
            } else if (Stream.of(extensionContext.getRequiredTestClass().getDeclaredFields()).anyMatch(field -> TestRunner.class.isAssignableFrom(field.getType()))) {
                return getTestRunner(extensionContext).getTestCase();
            } else {
                throw new CitrusRuntimeException("Neither test class nor test method is using any of test runner, designer or Xml test file.");
            }
        }, TestCase.class);
    }

    /**
     * Searches for method parameter of type test designer.
     * @param method
     * @return
     */
    private static boolean isDesignerMethod(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();

        for (Class<?> parameterType : parameterTypes) {
            if (parameterType.isAssignableFrom(TestDesigner.class)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Searches for field of type test designer.
     * @param type
     * @return
     */
    private static boolean isDesignerClass(Class<?> type) {
        return Stream.of(type.getDeclaredFields()).anyMatch(field -> TestDesigner.class.isAssignableFrom(field.getType()));
    }

    /**
     * Searches for method parameter of type test runner.
     * @param method
     * @return
     */
    private static boolean isRunnerMethod(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();

        for (Class<?> parameterType : parameterTypes) {
            if (parameterType.isAssignableFrom(TestRunner.class)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Searches for field of type test runner.
     * @param type
     * @return
     */
    private static boolean isRunnerClass(Class<?> type) {
        return Stream.of(type.getDeclaredFields()).anyMatch(field -> TestRunner.class.isAssignableFrom(field.getType()));
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
