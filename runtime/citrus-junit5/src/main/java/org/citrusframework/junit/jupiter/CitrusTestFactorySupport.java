/*
 * Copyright 2022 the original author or authors.
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.CitrusSettings;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.common.TestLoader;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ClasspathResourceResolver;
import org.citrusframework.util.FileUtils;
import org.junit.jupiter.api.DynamicTest;

/**
 * @author Christoph Deppisch
 */
public final class CitrusTestFactorySupport {

    private final String type;
    private final Consumer<TestLoader> handler;

    public CitrusTestFactorySupport(String type, Consumer<TestLoader> handler) {
        this.type = type;
        this.handler = handler;
    }

    public static CitrusTestFactorySupport factory(String type) {
        return new CitrusTestFactorySupport(type, TestLoader::load);
    }

    public static CitrusTestFactorySupport xml() {
        return factory(TestLoader.XML);
    }

    public static CitrusTestFactorySupport groovy() {
        return factory(TestLoader.GROOVY);
    }

    public static CitrusTestFactorySupport springXml() {
        return factory(TestLoader.SPRING);
    }

    /**
     * Creates stream of dynamic tests based on package scan. Scans package for all Xml test case files and creates dynamic test instance for it.
     * @param packagesToScan
     * @return
     */
    public Stream<DynamicTest> packageScan(String ... packagesToScan) {
        List<DynamicTest> tests = new ArrayList<>();

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

                        String testName = FileUtils.getBaseName(String.valueOf(fileResource.getFileName()));

                        TestLoader testLoader = createTestLoader(testName, filePath);
                        tests.add(DynamicTest.dynamicTest(testName, () -> handler.accept(testLoader)));
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
    public Stream<DynamicTest> dynamicTests(String packageName, String ... testNames) {
        return Stream.of(testNames).map(testName -> {
            TestLoader testLoader = createTestLoader(testName, packageName);
            return DynamicTest.dynamicTest(testName, () -> handler.accept(testLoader));
        });
    }

    /**
     * Creates dynamic test that executes Xml test given by name and package.
     * @param testClass
     * @param testNames
     * @return
     */
    public Stream<DynamicTest> dynamicTests(Class<?> testClass, String ... testNames) {
        return Stream.of(testNames).map(testName -> {
            TestLoader testLoader = createTestLoader(testName, testClass.getPackage().getName());
            return DynamicTest.dynamicTest(testName, () -> handler.accept(testLoader));
        });
    }

    /**
     * Creates dynamic test that executes Xml test given by name and package.
     * @param packageName
     * @param testName
     * @return
     */
    public DynamicTest dynamicTest(String packageName, String testName) {
        TestLoader testLoader = createTestLoader(testName, packageName);
        return DynamicTest.dynamicTest(testName, () -> handler.accept(testLoader));
    }

    private TestLoader createTestLoader(String testName, String packageName) {
        TestLoader testLoader = TestLoader.lookup(type)
                .orElseThrow(() -> new CitrusRuntimeException(String.format("Missing '%s' test loader in project classpath - " +
                        "please add proper Citrus module to the project", type)));

        testLoader.setTestClass(DynamicTest.class);
        testLoader.setTestName(testName);
        testLoader.setPackageName(packageName);

        CitrusAnnotations.injectAll(testLoader, CitrusInstanceManager.getOrDefault());

        return testLoader;
    }
}
