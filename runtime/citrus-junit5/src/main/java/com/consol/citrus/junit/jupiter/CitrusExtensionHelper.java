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

package com.consol.citrus.junit.jupiter;

import java.lang.reflect.Method;

import com.consol.citrus.Citrus;
import com.consol.citrus.DefaultTestCase;
import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.GherkinTestActionRunner;
import com.consol.citrus.TestActionRunner;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.junit.jupiter.spring.XmlTestHelper;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
public final class CitrusExtensionHelper {

    /**
     * Prevent instantiation of utility class
     */
    private CitrusExtensionHelper() {
        // prevent instantiation
    }

    /**
     * Checks for {@link CitrusXmlTest} annotations or {@link TestFactory} annotations on test method.
     * @param method
     * @return
     */
    public static boolean isXmlTestMethod(Method method) {
        return method.isAnnotationPresent(CitrusXmlTest.class) || method.isAnnotationPresent(TestFactory.class);
    }

    /**
     * Creates new test runner instance for this test method.
     * @param testName
     * @param extensionContext
     * @return
     */
    public static TestCaseRunner createTestRunner(String testName, ExtensionContext extensionContext) {
        TestCaseRunner testCaseRunner = new DefaultTestCaseRunner(new DefaultTestCase(), getTestContext(extensionContext));
        testCaseRunner.testClass(extensionContext.getRequiredTestClass());
        testCaseRunner.name(testName);
        testCaseRunner.packageName(extensionContext.getRequiredTestClass().getPackage().getName());

        return testCaseRunner;
    }

    /**
     * Get the {@link TestCaseRunner} associated with the supplied {@code ExtensionContext} and its required test class name.
     * @param extensionContext
     * @return the {@code TestCaseRunner} (never {@code null})
     */
    public static TestCaseRunner getTestRunner(ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");

        return extensionContext.getRoot().getStore(CitrusExtension.NAMESPACE).getOrComputeIfAbsent(getBaseKey(extensionContext) + TestCaseRunner.class.getSimpleName(), key -> {
            String testName = extensionContext.getRequiredTestClass().getSimpleName() + "." + extensionContext.getRequiredTestMethod().getName();

            if (extensionContext.getRequiredTestMethod().getAnnotation(CitrusTest.class) != null) {
                CitrusTest citrusTestAnnotation = extensionContext.getRequiredTestMethod().getAnnotation(CitrusTest.class);
                if (StringUtils.hasText(citrusTestAnnotation.name())) {
                    testName = citrusTestAnnotation.name();
                }
            }

            return createTestRunner(testName, extensionContext);
        }, TestCaseRunner.class);
    }

    /**
     * Get the {@link TestCase} associated with the supplied {@code ExtensionContext} and its required test class name.
     * @param extensionContext
     * @return the {@code TestCase} (never {@code null})
     */
    public static TestCase getTestCase(ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");
        return extensionContext.getRoot().getStore(CitrusExtension.NAMESPACE).getOrComputeIfAbsent(getBaseKey(extensionContext) + TestCase.class.getSimpleName(), key -> {
            if (CitrusExtensionHelper.isXmlTestMethod(extensionContext.getRequiredTestMethod())) {
                return XmlTestHelper.getXmlTestCase(extensionContext);
            } else {
                return getTestRunner(extensionContext).getTestCase();
            }
        }, TestCase.class);
    }

    /**
     * Get the {@link TestContext} associated with the supplied {@code ExtensionContext} and its required test class name.
     * @param extensionContext
     * @return the {@code TestContext} (never {@code null})
     */
    public static TestContext getTestContext(ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");
        return extensionContext.getRoot().getStore(CitrusExtension.NAMESPACE).getOrComputeIfAbsent(getBaseKey(extensionContext) + TestContext.class.getSimpleName(), key -> getCitrus(extensionContext).getCitrusContext().createTestContext(), TestContext.class);
    }

    /**
     * Gets base key for store.
     * @param extensionContext
     * @return
     */
    public static String getBaseKey(ExtensionContext extensionContext) {
        return extensionContext.getRequiredTestClass().getName() + "." + extensionContext.getRequiredTestMethod().getName() + "#";
    }

    /**
     * Get the {@link Citrus} associated with the supplied {@code ExtensionContext}.
     * @param extensionContext
     * @return the {@code Citrus} (never {@code null})
     */
    public static Citrus getCitrus(ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");
        Citrus citrus = extensionContext.getRoot().getStore(CitrusExtension.NAMESPACE).get(Citrus.class.getName(), Citrus.class);

        if (citrus == null) {
            throw new CitrusRuntimeException("Missing Citrus instance in JUnit5 extension context");
        }

        return citrus;
    }

    /**
     * Sets the {@link Citrus} instance on the {@code ExtensionContext}.
     * @param citrus
     * @param extensionContext
     * @return the {@code Citrus} (never {@code null})
     */
    public static void setCitrus(Citrus citrus, ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");
        extensionContext.getRoot().getStore(CitrusExtension.NAMESPACE).put(Citrus.class.getName(), citrus);
    }

    public static Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        TestCaseRunner storedBuilder = CitrusExtensionHelper.getTestRunner(extensionContext);
        if (TestCaseRunner.class.isAssignableFrom(parameterContext.getParameter().getType())) {
            return storedBuilder;
        } else if (GherkinTestActionRunner.class.isAssignableFrom(parameterContext.getParameter().getType())
                && storedBuilder instanceof GherkinTestActionRunner) {
            return storedBuilder;
        } else if (TestActionRunner.class.isAssignableFrom(parameterContext.getParameter().getType())
                && storedBuilder instanceof TestActionRunner) {
            return storedBuilder;
        } else if (TestContext.class.isAssignableFrom(parameterContext.getParameter().getType())) {
            return CitrusExtensionHelper.getTestContext(extensionContext);
        }

        throw new CitrusRuntimeException(String.format("Failed to resolve parameter %s", parameterContext.getParameter()));
    }

    /**
     * Checks if Citrus instance is present in given extension context.
     * @param extensionContext
     * @return
     */
    public static boolean requiresCitrus(ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");
        Citrus citrus = extensionContext.getRoot().getStore(CitrusExtension.NAMESPACE).get(Citrus.class.getName(), Citrus.class);
        return citrus == null;
    }
}
