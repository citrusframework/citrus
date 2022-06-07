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

package com.consol.citrus.junit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.consol.citrus.CitrusSettings;
import com.consol.citrus.DefaultTestCase;
import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.TestResult;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.util.FileUtils;
import org.junit.runners.model.FrameworkMethod;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
public final class JUnit4Helper {

    public static final String BUILDER_ATTRIBUTE = "builder";

    /**
     * Prevent instantiation of utility class
     */
    private JUnit4Helper() {
        // prevent instantiation
    }

    /**
     * Invokes test method based on designer or runner environment.
     * @param target
     * @param frameworkMethod
     * @param runner
     * @param context
     */
    public static void invokeTestMethod(Object target, CitrusFrameworkMethod frameworkMethod, TestCaseRunner runner, TestContext context) {
        final TestCase testCase = runner.getTestCase();
        try {
            Object[] params = JUnit4ParameterHelper.resolveParameter(frameworkMethod, testCase, context);
            runner.start();
            ReflectionUtils.invokeMethod(frameworkMethod.getMethod(), target, params);
        } catch (Exception | AssertionError e) {
            testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), e));
            throw new TestCaseFailedException(e);
        } finally {
            runner.stop();
        }
    }

    /**
     * Creates new test runner instance for this test method.
     * @param frameworkMethod
     * @param testClass
     * @param context
     * @return
     */
    public static TestCaseRunner createTestRunner(CitrusFrameworkMethod frameworkMethod, Class<?> testClass, TestContext context) {
        TestCaseRunner testCaseRunner = new DefaultTestCaseRunner(new DefaultTestCase(), context);
        testCaseRunner.testClass(testClass);
        testCaseRunner.name(frameworkMethod.getTestName());
        testCaseRunner.packageName(frameworkMethod.getPackageName());

        return testCaseRunner;
    }

    /**
     * Construct list of intercepted framework methods with proper test name, package name and source from given framework
     * method and its Citrus test annotation information.
     * @param method
     * @param testNames
     * @param testPackageName
     * @param packagesToScan
     * @param sources
     */
    public static List<FrameworkMethod> findInterceptedMethods(FrameworkMethod method, String type, String[] testNames,
                                       String testPackageName, String[] packagesToScan, String[] sources) {
        List<FrameworkMethod> interceptedMethods = new ArrayList<>();

        String packageName = method.getMethod().getDeclaringClass().getPackage().getName();
        if (StringUtils.hasText(testPackageName)) {
            packageName = testPackageName;
        }

        if (testNames.length > 0) {
            for (String name : testNames) {
                interceptedMethods.add(new CitrusFrameworkMethod(method.getMethod(), type, name, packageName));
            }
        } else if (packagesToScan.length == 0 && sources.length == 0) {
            interceptedMethods.add(new CitrusFrameworkMethod(method.getMethod(), type, method.getName(), packageName));
        }

        for (String source : sources) {
            Resource file = FileUtils.getFileResource(source);

            String sourceFilePackageName  = "";
            if (source.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX)) {
                sourceFilePackageName = source.substring(ResourceLoader.CLASSPATH_URL_PREFIX.length());
            }

            if (StringUtils.hasLength(sourceFilePackageName) && sourceFilePackageName.contains("/")) {
                sourceFilePackageName = sourceFilePackageName.substring(0, sourceFilePackageName.lastIndexOf("/"));
            }

            CitrusFrameworkMethod frameworkMethod = new CitrusFrameworkMethod(method.getMethod(), type, FileUtils.getBaseName(file.getFilename()),
                    sourceFilePackageName.replace("/","."));
            frameworkMethod.setSource(source);
            interceptedMethods.add(frameworkMethod);
        }

        for (String packageScan : packagesToScan) {
            try {
                for (String fileNamePattern : CitrusSettings.getTestFileNamePattern(type)) {
                    Resource[] fileResources = new PathMatchingResourcePatternResolver().getResources(packageScan.replace('.', File.separatorChar) + fileNamePattern);
                    for (Resource fileResource : fileResources) {
                        String filePath = fileResource.getFile().getParentFile().getCanonicalPath();

                        if (packageScan.startsWith("file:")) {
                            filePath = "file:" + filePath;
                        }

                        filePath = filePath.substring(filePath.indexOf(packageScan.replace('.', File.separatorChar)));

                        interceptedMethods.add(new CitrusFrameworkMethod(method.getMethod(), type,
                                FileUtils.getBaseName(fileResource.getFilename()), filePath));
                    }
                }
            } catch (RuntimeException | IOException e) {
                interceptedMethods.add(new CitrusFrameworkMethod(method.getMethod(), type, method.getName(), packageScan)
                        .withError(new CitrusRuntimeException(String.format("Unable to locate file resources for test package '%s'", packageScan), e)));
            }
        }

        return interceptedMethods;
    }
}
