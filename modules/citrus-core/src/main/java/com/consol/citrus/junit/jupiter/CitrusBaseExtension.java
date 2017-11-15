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
import com.consol.citrus.annotations.*;
import com.consol.citrus.common.TestLoader;
import com.consol.citrus.common.XmlTestLoader;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.extension.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * JUnit5 extension supports Citrus Xml test extension that also allows to load test cases from external Spring configuration files. In addition to that Citrus annotation based resource injection
 * and lifecycle management such as before/after suite is supported.
 *
 * Extension resolves method parameter of type {@link TestContext} and injects endpoints and resources coming from Citrus Spring application context that is automatically loaded at suite start up.
 * After suite automatically includes Citrus report generation.
 *
 * @author Christoph Deppisch
 */
public class CitrusBaseExtension implements BeforeAllCallback,
        BeforeEachCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback, ParameterResolver, TestInstancePostProcessor {

    /** Test suite name */
    private static final String SUITE_NAME = "citrus-junit5-suite";

    private static Citrus citrus;
    private static boolean beforeSuite = true;
    private static boolean afterSuite = true;

    /**
     * {@link ExtensionContext.Namespace} in which Citrus related objects are stored keyed by test class.
     */
    protected static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(CitrusBaseExtension.class);

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        extensionContext.getRoot().getStore(NAMESPACE).remove(getBaseKey(extensionContext) + TestContext.class.getSimpleName());
        extensionContext.getRoot().getStore(NAMESPACE).remove(getBaseKey(extensionContext) + TestCase.class.getSimpleName());
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        if (beforeSuite) {
            beforeSuite = false;
            getCitrus(extensionContext).beforeSuite(SUITE_NAME);
        }

        if (afterSuite) {
            afterSuite = false;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> getCitrus(extensionContext).afterSuite(SUITE_NAME)));
        }
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        getTestContext(extensionContext);
        getXmlTestCase(extensionContext);
    }

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        getCitrus(extensionContext).run(getXmlTestCase(extensionContext), getTestContext(extensionContext));
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) throws Exception {
        CitrusAnnotations.injectAll(testInstance, getCitrus(extensionContext));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().isAnnotationPresent(CitrusResource.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (TestContext.class.isAssignableFrom(parameterContext.getParameter().getType())) {
            return getTestContext(extensionContext);
        }

        throw new CitrusRuntimeException(String.format("Failed to resolve parameter %s", parameterContext.getParameter()));
    }

    /**
     * Gets base key for store.
     * @param extensionContext
     * @return
     */
    protected static String getBaseKey(ExtensionContext extensionContext) {
        return extensionContext.getRequiredTestClass().getName() + "." + extensionContext.getRequiredTestMethod().getName() + "#";
    }

    /**
     * Creates new test loader which has TestNG test annotations set for test execution. Only
     * suitable for tests that get created at runtime through factory method. Subclasses
     * may overwrite this in order to provide custom test loader with custom test annotations set.
     * @param extensionContext
     * @return
     */
    protected static TestLoader createTestLoader(ExtensionContext extensionContext) {
        Method method = extensionContext.getRequiredTestMethod();
        String testName = extensionContext.getRequiredTestClass().getSimpleName();
        String packageName = method.getDeclaringClass().getPackage().getName();

        if (method.getAnnotation(CitrusXmlTest.class) != null) {
            CitrusXmlTest citrusXmlTestAnnotation = method.getAnnotation(CitrusXmlTest.class);
            String[] packagesToScan = citrusXmlTestAnnotation.packageScan();

            if (StringUtils.hasText(citrusXmlTestAnnotation.packageName())) {
                packageName = citrusXmlTestAnnotation.packageName();
            }

            if (citrusXmlTestAnnotation.name().length > 0) {
                testName = citrusXmlTestAnnotation.name()[0];
            } else if (packagesToScan.length == 0) {
                testName = method.getName();
            }
        }

        return new XmlTestLoader(extensionContext.getRequiredTestClass(), testName, packageName, getCitrus(extensionContext).getApplicationContext());
    }

    /**
     * Get the {@link Citrus} associated with the supplied {@code ExtensionContext}.
     * @return the {@code Citrus} (never {@code null})
     */
    protected static Citrus getCitrus(ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");
        return extensionContext.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(Citrus.class.getName(), key -> {
            if (citrus == null) {
                citrus = Citrus.newInstance();
            }
            
            return citrus;
        }, Citrus.class);
    }

    /**
     * Get the {@link TestContext} associated with the supplied {@code ExtensionContext} and its required test class name.
     * @return the {@code TestContext} (never {@code null})
     */
    protected static TestContext getTestContext(ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");
        return extensionContext.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(getBaseKey(extensionContext) + TestContext.class.getSimpleName(), key -> getCitrus(extensionContext).createTestContext(), TestContext.class);
    }

    /**
     * Get the {@link TestCase} associated with the supplied {@code ExtensionContext} and its required test class name.
     * @return the {@code TestCase} (never {@code null})
     */
    protected static TestCase getXmlTestCase(ExtensionContext extensionContext) {
        Assert.notNull(extensionContext, "ExtensionContext must not be null");
        return extensionContext.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(getBaseKey(extensionContext) + TestCase.class.getSimpleName(), key -> createTestLoader(extensionContext).load(), TestCase.class);
    }

    /**
     * Creates stream of dynamic tests based on package scan. Scans package for all Xml test case files and creates dynamic test instance for it.
     * @param packagesToScan
     * @return
     */
    public static Stream<DynamicTest> packageScan(String ... packagesToScan) {
        List<DynamicTest> tests = new ArrayList<>();

        for (String packageScan : packagesToScan) {
            try {
                for (String fileNamePattern : Citrus.getXmlTestFileNamePattern()) {
                    Resource[] fileResources = new PathMatchingResourcePatternResolver().getResources(packageScan.replace('.', File.separatorChar) + fileNamePattern);
                    for (Resource fileResource : fileResources) {
                        String filePath = fileResource.getFile().getParentFile().getCanonicalPath();

                        if (packageScan.startsWith("file:")) {
                            filePath = "file:" + filePath;
                        }

                        filePath = filePath.substring(filePath.indexOf(packageScan.replace('.', File.separatorChar)));

                        String testName = fileResource.getFilename().substring(0, fileResource.getFilename().length() - ".xml".length());

                        XmlTestLoader testLoader = new XmlTestLoader(DynamicTest.class, testName, filePath, citrus.getApplicationContext());
                        tests.add(DynamicTest.dynamicTest(testName, () -> citrus.run(testLoader.load())));
                    }
                }
            } catch (IOException e) {
                throw new CitrusRuntimeException("Unable to locate file resources for test package '" + packageScan + "'", e);
            }
        }

        return tests.stream();
    }

    /**
     * Creates dynamic test that executes Xml test given by name and package.
     * @param packageName
     * @param testNames
     * @return
     */
    public static Stream<DynamicTest> dynamicTests(String packageName, String ... testNames) {
        return Stream.of(testNames).map(testName -> {
            XmlTestLoader testLoader = new XmlTestLoader(DynamicTest.class, testName, packageName, citrus.getApplicationContext());
            return DynamicTest.dynamicTest(testName, () -> citrus.run(testLoader.load()));
        });
    }

    /**
     * Creates dynamic test that executes Xml test given by name and package.
     * @param testClass
     * @return
     */
    public static Stream<DynamicTest> dynamicTests(Class<?> testClass, String ... testNames) {
        return Stream.of(testNames).map(testName -> {
            XmlTestLoader testLoader = new XmlTestLoader(DynamicTest.class, testName, testClass.getPackage().getName(), citrus.getApplicationContext());
            return DynamicTest.dynamicTest(testName, () -> citrus.run(testLoader.load()));
        });
    }

    /**
     * Creates dynamic test that executes Xml test given by name and package.
     * @param packageName
     * @param testName
     * @return
     */
    public static DynamicTest dynamicTest(String packageName, String testName) {
        XmlTestLoader testLoader = new XmlTestLoader(DynamicTest.class, testName, packageName, citrus.getApplicationContext());
        return DynamicTest.dynamicTest(testName, () -> citrus.run(testLoader.load()));
    }
}
