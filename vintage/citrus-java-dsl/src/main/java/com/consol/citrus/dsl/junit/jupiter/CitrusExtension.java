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

import java.lang.reflect.Method;
import java.util.stream.Stream;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestResult;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.annotations.CitrusTestSource;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.common.TestLoader;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.annotations.CitrusDslAnnotations;
import com.consol.citrus.dsl.design.DefaultTestDesigner;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.DefaultTestRunner;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.junit.jupiter.CitrusBaseExtension;
import com.consol.citrus.junit.jupiter.CitrusExtensionHelper;
import com.consol.citrus.junit.jupiter.spring.CitrusSpringXmlTestFactory;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

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
public class CitrusExtension extends CitrusBaseExtension implements ParameterResolver,
        BeforeEachCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback, TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        if (!isSpringXmlTestMethod(extensionContext.getRequiredTestMethod()) &&
                (isRunnerMethod(extensionContext.getRequiredTestMethod()) || isRunnerClass(extensionContext.getRequiredTestClass()))) {
            TestCase testCase = getTestCase(extensionContext);
            testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), throwable));
        }

        throw throwable;
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        if (!isSpringXmlTestMethod(extensionContext.getRequiredTestMethod())) {
            TestCase testCase = getTestCase(extensionContext);

            extensionContext.getExecutionException()
                    .ifPresent(e -> testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), e)));

            if (isDesignerMethod(extensionContext.getRequiredTestMethod()) ||
                    isDesignerClass(extensionContext.getRequiredTestClass())) {
                TestContext context = CitrusExtensionHelper.getTestContext(extensionContext);
                CitrusExtensionHelper.getCitrus(extensionContext).run(testCase, context);
            } else if (isRunnerMethod(extensionContext.getRequiredTestMethod()) ||
                    isRunnerClass(extensionContext.getRequiredTestClass())) {
                getTestRunner(extensionContext).stop();
            }

            extensionContext.getRoot().getStore(NAMESPACE).remove(CitrusExtensionHelper.getBaseKey(extensionContext) + TestRunner.class.getSimpleName());
            extensionContext.getRoot().getStore(NAMESPACE).remove(CitrusExtensionHelper.getBaseKey(extensionContext) + TestDesigner.class.getSimpleName());
        }

        extensionContext.getRoot().getStore(NAMESPACE).remove(CitrusExtensionHelper.getBaseKey(extensionContext) + TestContext.class.getSimpleName());
        extensionContext.getRoot().getStore(NAMESPACE).remove(CitrusExtensionHelper.getBaseKey(extensionContext) + TestCase.class.getSimpleName());
    }

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        if (isSpringXmlTestMethod(extensionContext.getRequiredTestMethod())) {
            CitrusExtensionHelper.getCitrus(extensionContext).run(getXmlTestCase(extensionContext), CitrusExtensionHelper.getTestContext(extensionContext));
        } else {
            CitrusDslAnnotations.injectTestDesigner(extensionContext.getRequiredTestInstance(), getTestDesigner(extensionContext));
            CitrusDslAnnotations.injectTestRunner(extensionContext.getRequiredTestInstance(), getTestRunner(extensionContext));
            CitrusAnnotations.injectTestContext(extensionContext.getRequiredTestInstance(), CitrusExtensionHelper.getTestContext(extensionContext));
            CitrusAnnotations.injectEndpoints(extensionContext.getRequiredTestInstance(), CitrusExtensionHelper.getTestContext(extensionContext));
        }
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        CitrusExtensionHelper.getTestContext(extensionContext);

        if (isSpringXmlTestMethod(extensionContext.getRequiredTestMethod())) {
            getXmlTestCase(extensionContext);
        } else {
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
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().isAnnotationPresent(CitrusResource.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (TestDesigner.class.isAssignableFrom(parameterContext.getParameter().getType())) {
            return getTestDesigner(extensionContext);
        } else if (TestRunner.class.isAssignableFrom(parameterContext.getParameter().getType())) {
            return getTestRunner(extensionContext);
        }

        return CitrusExtensionHelper.resolveParameter(parameterContext, extensionContext);
    }

    /**
     * Get the {@link TestDesigner} associated with the supplied {@code ExtensionContext} and its required test class name.
     * @return the {@code TestDesigner} (never {@code null})
     */
    protected static TestDesigner getTestDesigner(ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");

        return extensionContext.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(CitrusExtensionHelper.getBaseKey(extensionContext) + TestDesigner.class.getSimpleName(), key -> {
            String testName = extensionContext.getRequiredTestClass().getSimpleName() + "." + extensionContext.getRequiredTestMethod().getName();

            if (extensionContext.getRequiredTestMethod().getAnnotation(CitrusTest.class) != null) {
                CitrusTest citrusTestAnnotation = extensionContext.getRequiredTestMethod().getAnnotation(CitrusTest.class);
                if (StringUtils.hasText(citrusTestAnnotation.name())) {
                    testName = citrusTestAnnotation.name();
                }
            }

            TestDesigner testDesigner = new DefaultTestDesigner(CitrusExtensionHelper.getTestContext(extensionContext));
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

        return extensionContext.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(CitrusExtensionHelper.getBaseKey(extensionContext) + TestRunner.class.getSimpleName(), key -> {
            String testName = extensionContext.getRequiredTestClass().getSimpleName() + "." + extensionContext.getRequiredTestMethod().getName();

            if (extensionContext.getRequiredTestMethod().getAnnotation(CitrusTest.class) != null) {
                CitrusTest citrusTestAnnotation = extensionContext.getRequiredTestMethod().getAnnotation(CitrusTest.class);
                if (StringUtils.hasText(citrusTestAnnotation.name())) {
                    testName = citrusTestAnnotation.name();
                }
            }

            TestRunner testRunner = new DefaultTestRunner(CitrusExtensionHelper.getTestContext(extensionContext));
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
        return extensionContext.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(CitrusExtensionHelper.getBaseKey(extensionContext) + TestCase.class.getSimpleName(), key -> {
            if (isDesignerMethod(extensionContext.getRequiredTestMethod())) {
                return getTestDesigner(extensionContext).getTestCase();
            } else if (isRunnerMethod(extensionContext.getRequiredTestMethod())) {
                return getTestRunner(extensionContext).getTestCase();
            } else if (isSpringXmlTestMethod(extensionContext.getRequiredTestMethod())) {
                return getXmlTestCase(extensionContext);
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
     * Get the {@link TestCase} associated with the supplied {@code ExtensionContext} and its required test class name.
     * @return the {@code TestCase} (never {@code null})
     */
    public static TestCase getXmlTestCase(ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");
        return extensionContext.getRoot().getStore(com.consol.citrus.junit.jupiter.CitrusExtension.NAMESPACE)
                .getOrComputeIfAbsent(CitrusExtensionHelper.getBaseKey(extensionContext) + TestCase.class.getSimpleName(),
                        key -> CitrusExtensionHelper.createTestLoader(extensionContext).load(), TestCase.class);
    }

    /**
     * Checks for Spring Xml Citrus test annotations on test method.
     * @param method
     * @return
     */
    private static boolean isSpringXmlTestMethod(Method method) {
        return method.isAnnotationPresent(CitrusXmlTest.class) || method.isAnnotationPresent(CitrusSpringXmlTestFactory.class) ||
                (method.isAnnotationPresent(CitrusTestSource.class) && method.getAnnotation(CitrusTestSource.class).type().equals(TestLoader.SPRING));
    }
}
