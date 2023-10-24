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

package org.citrusframework.testng;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.citrusframework.CitrusSettings;
import org.citrusframework.DefaultTestCase;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.TestCaseRunnerFactory;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.common.TestSourceAware;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ClasspathResourceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.ReflectionHelper;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IHookCallBack;
import org.testng.ITestResult;

/**
 * @author Christoph Deppisch
 */
public final class TestNGHelper {

    public static final String BUILDER_ATTRIBUTE = "builder";

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(TestNGHelper.class);

    /**
     * Prevent instantiation of utility class
     */
    private TestNGHelper() {
        // prevent instantiation
    }

    /**
     * Invokes test method.
     * @param target
     * @param testResult
     * @param method
     * @param testLoader
     * @param context
     * @param invocationCount
     */
    public static void invokeTestMethod(Object target, ITestResult testResult, Method method,
                                        TestLoader testLoader, TestContext context, int invocationCount) {
        Object[] params = TestNGParameterHelper.resolveParameter(target, testResult, method, context, invocationCount);
        testLoader.configureTestCase(t -> TestNGParameterHelper.injectTestParameters(method, t, params));
        testLoader.doWithTestCase(t -> ReflectionHelper.invokeMethod(method, target, params));
        testLoader.load();
    }

    /**
     * Creates new test runner instance for this test method.
     * @param method
     * @param context
     * @return
     */
    public static TestCaseRunner createTestCaseRunner(Object target, Method method, TestContext context) {
        TestCaseRunner testCaseRunner = TestCaseRunnerFactory.createRunner(new DefaultTestCase(), context);
        testCaseRunner.testClass(target.getClass());
        testCaseRunner.name(target.getClass().getSimpleName());
        testCaseRunner.packageName(target.getClass().getPackage().getName());

        if (method.getAnnotation(CitrusTest.class) != null) {
            CitrusTest citrusTestAnnotation = method.getAnnotation(CitrusTest.class);
            if (StringUtils.hasText(citrusTestAnnotation.name())) {
                testCaseRunner.name(citrusTestAnnotation.name());
            } else {
                testCaseRunner.name(method.getDeclaringClass().getSimpleName() + "." + method.getName());
            }
        }  else {
            testCaseRunner.name(method.getDeclaringClass().getSimpleName() + "." + method.getName());
        }

        return testCaseRunner;
    }

    /**
     * Creates test loader from @CitrusTestSource(type = TestLoader.SPRING) annotated test method and saves those to local member.
     * Test loaders get executed later when actual method is called by TestNG. This way user can annotate
     * multiple methods in one single class each executing several Citrus XML tests.
     *
     * @param method
     * @param provider
     * @return
     */
    public static List<TestLoader> createMethodTestLoaders(Method method, TestLoaderProvider provider) {
        List<TestLoader> methodTestLoaders = new ArrayList<>();

        if (method.getAnnotation(CitrusTestSource.class) != null) {
            CitrusTestSource citrusTestAnnotation = method.getAnnotation(CitrusTestSource.class);
            methodTestLoaders.addAll(createMethodTestLoaders(method, citrusTestAnnotation.name(), citrusTestAnnotation.packageName(),
                    citrusTestAnnotation.packageScan(), citrusTestAnnotation.sources(), provider, citrusTestAnnotation.type(), CitrusSettings.getTestFileNamePattern(citrusTestAnnotation.type())));
        }

        return methodTestLoaders;
    }

    /**
     * Constructs list of test loaders for given method with proper test name, package name and source according to
     * Citrus test annotation.
     * @param method
     * @param testNames
     * @param testPackageName
     * @param packagesToScan
     * @param sources
     * @param provider
     * @param type
     * @param testFileNamePattern
     * @return
     */
    private static List<? extends TestLoader> createMethodTestLoaders(Method method, String[] testNames, String testPackageName,
                                                                      String[] packagesToScan, String[] sources, TestLoaderProvider provider,
                                                                      String type, Set<String> testFileNamePattern) {
        List<TestLoader> methodTestLoaders = new ArrayList<>();

        String packageName = method.getDeclaringClass().getPackage().getName();
        if (StringUtils.hasText(testPackageName)) {
            packageName = testPackageName;
        }

        if (testNames.length > 0) {
            for (String name : testNames) {
                methodTestLoaders.add(provider.createTestLoader(name, packageName, type));
            }
        } else if (packagesToScan.length == 0 && sources.length == 0) {
            methodTestLoaders.add(provider.createTestLoader(method.getName(), packageName, type));
        }

        for (String source : sources) {
            Resource file = FileUtils.getFileResource(source);

            String sourceFilePackageName  = "";
            if (source.startsWith(Resources.CLASSPATH_RESOURCE_PREFIX)) {
                sourceFilePackageName = source.substring(Resources.CLASSPATH_RESOURCE_PREFIX.length());
            }

            if (StringUtils.hasText(sourceFilePackageName) && sourceFilePackageName.contains("/")) {
                sourceFilePackageName = sourceFilePackageName.substring(0, sourceFilePackageName.lastIndexOf("/"));
            }

            TestLoader testLoader = provider.createTestLoader(FileUtils.getBaseName(FileUtils.getFileName(file.getLocation())),
                    sourceFilePackageName.replace("/","."), type);

            if (testLoader instanceof TestSourceAware) {
                ((TestSourceAware) testLoader).setSource(source);
                methodTestLoaders.add(testLoader);
            } else {
                logger.warn(String.format("Test loader %s is not able to handle test source %s", testLoader.getClass(), source));
            }
        }

        for (String packageScan : packagesToScan) {
            try {
                for (String fileNamePattern : testFileNamePattern) {
                    Set<Path> fileResources = new ClasspathResourceResolver().getResources(packageScan.replace('.', File.separatorChar), fileNamePattern);
                    for (Path fileResource : fileResources) {
                        String filePath = fileResource.getParent().toFile().getCanonicalPath();

                        if (packageScan.startsWith("file:")) {
                            filePath = "file:" + filePath;
                        }

                        filePath = filePath.substring(filePath.indexOf(packageScan.replace('.', File.separatorChar)));

                        methodTestLoaders.add(provider.createTestLoader(
                                FileUtils.getBaseName(String.valueOf(fileResource.getFileName())), filePath, type));
                    }
                }
            } catch (RuntimeException | IOException e) {
                throw new CitrusRuntimeException("Unable to locate file resources for test package '" + packageScan + "'", e);
            }
        }

        return methodTestLoaders;
    }

    @FunctionalInterface
    public interface TestLoaderProvider {
        /**
         * Creates new test loader which has TestNG test annotations set for test execution. Only
         * suitable for tests that get created at runtime through factory method. Subclasses
         * may overwrite this in order to provide custom test loader with custom test annotations set.
         * @param testName
         * @param packageName
         * @param type
         * @return
         */
        TestLoader createTestLoader(String testName, String packageName, String type);
    }

    /**
     * Class faking test execution as callback. Used in run hookable method when test case
     * was executed before and callback is needed for super class run method invocation.
     */
    public static final class FakeExecutionCallBack implements IHookCallBack {
        private Object[] parameters;

        public FakeExecutionCallBack(Object[] parameters) {
            this.parameters = Arrays.copyOf(parameters, parameters.length);
        }

        @Override
        public void runTestMethod(ITestResult testResult) {
            // do nothing as test case was already executed
        }

        @Override
        public Object[] getParameters() {
            return Arrays.copyOf(parameters, parameters.length);
        }

    }
}
