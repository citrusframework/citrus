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

package org.citrusframework.junit.jupiter;

import java.lang.reflect.Method;

import org.citrusframework.Citrus;
import org.citrusframework.DefaultTestCase;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.TestCaseRunnerFactory;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.DefaultTestLoader;
import org.citrusframework.common.TestLoader;
import org.citrusframework.common.TestSourceAware;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.ObjectHelper;
import org.citrusframework.util.StringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

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
        return method.isAnnotationPresent(CitrusTestFactory.class);
    }

    /**
     * Checks for Citrus test source annotations on test method.
     * @param method
     * @return
     */
    public static boolean isTestSourceMethod(Method method) {
        return method.isAnnotationPresent(CitrusTestSource.class);
    }

    /**
     * Creates new test runner instance for this test method.
     * @param testName
     * @param extensionContext
     * @return
     */
    public static TestCaseRunner createTestRunner(String testName, ExtensionContext extensionContext) {
        TestCaseRunner testCaseRunner = TestCaseRunnerFactory.createRunner(
            new DefaultTestCase(), getTestContext(extensionContext));
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
        ObjectHelper.assertNotNull(extensionContext, "ExtensionContext must not be null");

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
     * Get the {@link TestLoader} associated with the supplied {@code ExtensionContext} and its required test class name.
     * @param extensionContext
     * @return the {@code TestLoader} (never {@code null})
     */
    public static TestLoader getTestLoader(ExtensionContext extensionContext) {
        ObjectHelper.assertNotNull(extensionContext, "ExtensionContext must not be null");

        return extensionContext.getRoot().getStore(CitrusExtension.NAMESPACE).getOrComputeIfAbsent(getBaseKey(extensionContext) + TestLoader.class.getSimpleName(),
                key -> createTestLoader(extensionContext), TestLoader.class);
    }

    /**
     * Get the {@link TestCase} associated with the supplied {@code ExtensionContext} and its required test class name.
     * @param extensionContext
     * @return the {@code TestCase} (never {@code null})
     */
    public static TestCase getTestCase(ExtensionContext extensionContext) {
        ObjectHelper.assertNotNull(extensionContext, "ExtensionContext must not be null");
        return extensionContext.getRoot().getStore(CitrusExtension.NAMESPACE).getOrComputeIfAbsent(getBaseKey(extensionContext) + TestCase.class.getSimpleName(), key -> {
            if (CitrusExtensionHelper.isTestSourceMethod(extensionContext.getRequiredTestMethod())) {
                return getTestLoader(extensionContext).getTestCase();
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
     * @return
     */
    public static TestLoader createTestLoader(ExtensionContext extensionContext) {
        Method method = extensionContext.getRequiredTestMethod();

        if (isTestFactoryMethod(method)) {
            TestLoader testLoader = new DefaultTestLoader();
            configure(testLoader, extensionContext, method, new String[]{}, null, new String[]{}, new String[]{});
            return testLoader;
        }

        TestLoader testLoader;
        if (CitrusExtensionHelper.isTestSourceMethod(method)) {
            CitrusTestSource citrusTestAnnotation = method.getAnnotation(CitrusTestSource.class);

            testLoader = TestLoader.lookup(citrusTestAnnotation.type())
                    .orElseThrow(() -> new CitrusRuntimeException(String.format("Missing test loader for type '%s'", citrusTestAnnotation.type())));

            configure(testLoader, extensionContext, method, citrusTestAnnotation.name(),
                    citrusTestAnnotation.packageName(), citrusTestAnnotation.packageScan(), citrusTestAnnotation.sources());
        } else {
            testLoader = new DefaultTestLoader();
            configure(testLoader, extensionContext, method, new String[]{}, null, new String[]{}, new String[]{});
        }

        return testLoader;
    }

    /**
     * Get the {@link TestContext} associated with the supplied {@code ExtensionContext} and its required test class name.
     * @param extensionContext
     * @return the {@code TestContext} (never {@code null})
     */
    public static TestContext getTestContext(ExtensionContext extensionContext) {
        ObjectHelper.assertNotNull(extensionContext, "ExtensionContext must not be null");
        return extensionContext.getRoot().getStore(CitrusExtension.NAMESPACE).getOrComputeIfAbsent(getBaseKey(extensionContext) + TestContext.class.getSimpleName(),
                key -> getCitrus(extensionContext).getCitrusContext().createTestContext(), TestContext.class);
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
        ObjectHelper.assertNotNull(extensionContext, "ExtensionContext must not be null");
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
        ObjectHelper.assertNotNull(extensionContext, "ExtensionContext must not be null");
        extensionContext.getRoot().getStore(CitrusExtension.NAMESPACE).put(Citrus.class.getName(), citrus);
    }

    public static Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        TestCaseRunner runner = CitrusExtensionHelper.getTestRunner(extensionContext);
        if (TestCaseRunner.class.isAssignableFrom(parameterContext.getParameter().getType())) {
            return runner;
        } else if (GherkinTestActionRunner.class.isAssignableFrom(parameterContext.getParameter().getType())) {
            return runner;
        } else if (TestActionRunner.class.isAssignableFrom(parameterContext.getParameter().getType())) {
            return runner;
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
        ObjectHelper.assertNotNull(extensionContext, "ExtensionContext must not be null");
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
            testName = FileUtils.getBaseName(FileUtils.getFileName(file.getLocation()));

            packageName = source;
            if (packageName.startsWith(Resources.CLASSPATH_RESOURCE_PREFIX)) {
                packageName = source.substring(Resources.CLASSPATH_RESOURCE_PREFIX.length());
            }

            if (StringUtils.hasText(packageName) && packageName.contains("/")) {
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
