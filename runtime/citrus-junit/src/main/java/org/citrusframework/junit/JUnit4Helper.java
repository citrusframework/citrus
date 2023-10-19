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

package org.citrusframework.junit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.citrusframework.CitrusSettings;
import org.citrusframework.DefaultTestCase;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.TestCaseRunnerFactory;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ClasspathResourceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.ReflectionHelper;
import org.citrusframework.util.StringUtils;
import org.junit.runners.model.FrameworkMethod;

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
     * @param context
     */
    public static void invokeTestMethod(Object target, CitrusFrameworkMethod frameworkMethod, TestContext context) {
        Object[] params = JUnit4ParameterHelper.resolveParameter(frameworkMethod, context);
        ReflectionHelper.invokeMethod(frameworkMethod.getMethod(), target, params);
    }

    /**
     * Creates new test runner instance for this test method.
     * @param frameworkMethod
     * @param testClass
     * @param context
     * @return
     */
    public static TestCaseRunner createTestRunner(CitrusFrameworkMethod frameworkMethod, Class<?> testClass, TestContext context) {
        TestCaseRunner testCaseRunner = TestCaseRunnerFactory.createRunner(new DefaultTestCase(), context);
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
            if (source.startsWith(Resources.CLASSPATH_RESOURCE_PREFIX)) {
                sourceFilePackageName = source.substring(Resources.CLASSPATH_RESOURCE_PREFIX.length());
            }

            if (StringUtils.hasText(sourceFilePackageName) && sourceFilePackageName.contains("/")) {
                sourceFilePackageName = sourceFilePackageName.substring(0, sourceFilePackageName.lastIndexOf("/"));
            }

            CitrusFrameworkMethod frameworkMethod = new CitrusFrameworkMethod(method.getMethod(), type, FileUtils.getBaseName(FileUtils.getFileName(file.getLocation())),
                    sourceFilePackageName.replace("/","."));
            frameworkMethod.setSource(source);
            interceptedMethods.add(frameworkMethod);
        }

        for (String packageScan : packagesToScan) {
            try {
                for (String fileNamePattern : CitrusSettings.getTestFileNamePattern(type)) {
                    Set<Path> fileResources = new ClasspathResourceResolver().getResources(packageScan.replace('.', File.separatorChar), fileNamePattern);
                    for (Path fileResource : fileResources) {
                        String filePath = fileResource.getParent().toFile().getCanonicalPath();

                        if (packageScan.startsWith("file:")) {
                            filePath = "file:" + filePath;
                        }

                        filePath = filePath.substring(filePath.indexOf(packageScan.replace('.', File.separatorChar)));

                        interceptedMethods.add(new CitrusFrameworkMethod(method.getMethod(), type,
                                FileUtils.getBaseName(String.valueOf(fileResource.getFileName())), filePath));
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
