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
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusGroovyTest;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.common.NoopTestLoader;
import com.consol.citrus.common.TestLoader;
import com.consol.citrus.common.TestSourceAware;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.junit.jupiter.groovy.CitrusGroovyTestFactory;
import com.consol.citrus.junit.jupiter.spring.CitrusSpringXmlTestFactory;
import com.consol.citrus.util.FileUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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
     * Checks for test factory annotations on test method.
     * @param method
     * @return
     */
    public static boolean isTestFactoryMethod(Method method) {
        return method.isAnnotationPresent(CitrusSpringXmlTestFactory.class) || method.isAnnotationPresent(CitrusGroovyTestFactory.class);
    }

    /**
     * Checks for Spring Xml Citrus test annotations on test method.
     * @param method
     * @return
     */
    public static boolean isXmlTestMethod(Method method) {
        return method.isAnnotationPresent(CitrusXmlTest.class) || method.isAnnotationPresent(CitrusSpringXmlTestFactory.class);
    }

    /**
     * Checks for Groovy Citrus test annotations on test method.
     * @param method
     * @return
     */
    public static boolean isGroovyTestMethod(Method method) {
        return method.isAnnotationPresent(CitrusGroovyTest.class);
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
                return getXmlTestCase(extensionContext);
            } else {
                return getTestRunner(extensionContext).getTestCase();
            }
        }, TestCase.class);
    }

    /**
     * Creates new test loader which has TestNG test annotations set for test execution. Only
     * suitable for tests that get created at runtime through factory method. Subclasses
     * may overwrite this in order to provide custom test loader with custom test annotations set.
     * @param extensionContext
     * @param type
     * @return
     */
    public static TestLoader createTestLoader(ExtensionContext extensionContext, String type) {
        Method method = extensionContext.getRequiredTestMethod();

        if (isTestFactoryMethod(method)) {
            TestLoader testLoader = new NoopTestLoader();
            configure(testLoader, extensionContext, method, new String[]{}, null, new String[]{}, new String[]{});
            return testLoader;
        }

        TestLoader testLoader = TestLoader.lookup(type)
                .orElseThrow(() -> new CitrusRuntimeException(String.format("Missing test loader for type '%s'", type)));

        if (method.getAnnotation(CitrusGroovyTest.class) != null) {
            CitrusGroovyTest citrusTestAnnotation = method.getAnnotation(CitrusGroovyTest.class);
            configure(testLoader, extensionContext, method, citrusTestAnnotation.name(),
                    citrusTestAnnotation.packageName(), citrusTestAnnotation.packageScan(), citrusTestAnnotation.sources());
        } else if (method.getAnnotation(CitrusXmlTest.class) != null) {
            CitrusXmlTest citrusTestAnnotation = method.getAnnotation(CitrusXmlTest.class);
            configure(testLoader, extensionContext, method, citrusTestAnnotation.name(),
                    citrusTestAnnotation.packageName(), citrusTestAnnotation.packageScan(), citrusTestAnnotation.sources());
        }

        return testLoader;
    }

    /**
     * Get the {@link TestCase} associated with the supplied {@code ExtensionContext} and its required test class name.
     * @return the {@code TestCase} (never {@code null})
     */
    public static TestCase getXmlTestCase(ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");
        return extensionContext.getRoot().getStore(CitrusExtension.NAMESPACE)
                .getOrComputeIfAbsent(CitrusExtensionHelper.getBaseKey(extensionContext) + TestCase.class.getSimpleName(),
                        key -> createTestLoader(extensionContext, TestLoader.SPRING).load(), TestCase.class);
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

    /**
     * Configure given test loader instance with proper test name, package name and optional sources based on
     * Citrus test annotation information.
     * @param testLoader
     * @param extensionContext
     * @param method
     * @param methodNames
     * @param methodPackageName
     * @param packagesToScan
     * @param sources
     */
    private static void configure(TestLoader testLoader, ExtensionContext extensionContext, Method method,
                                  String[] methodNames, String methodPackageName, String[] packagesToScan, String[] sources) {
        String testName = extensionContext.getRequiredTestClass().getSimpleName();
        String packageName = method.getDeclaringClass().getPackage().getName();
        String source = null;

        if (StringUtils.hasText(methodPackageName)) {
            packageName = methodPackageName;
        }

        if (methodNames.length > 0) {
            testName = methodNames[0];
        } else if (packagesToScan.length == 0 && sources.length == 0) {
            testName = method.getName();
        }

        if (sources.length > 0) {
            source = sources[0];

            Resource file = FileUtils.getFileResource(source);
            testName = FileUtils.getBaseName(file.getFilename());

            packageName = source;
            if (packageName.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX)) {
                packageName = source.substring(ResourceLoader.CLASSPATH_URL_PREFIX.length());
            }

            if (StringUtils.hasLength(packageName) && packageName.contains("/")) {
                packageName = packageName.substring(0, packageName.lastIndexOf("/"));
            }

            packageName = packageName.replace("/", ".");

        }

        testLoader.setTestClass(extensionContext.getRequiredTestClass());
        testLoader.setTestName(testName);
        testLoader.setPackageName(packageName);

        CitrusAnnotations.injectAll(testLoader, CitrusExtensionHelper.getCitrus(extensionContext));

        if (testLoader instanceof TestSourceAware) {
            ((TestSourceAware) testLoader).setSource(source);
        }
    }
}
