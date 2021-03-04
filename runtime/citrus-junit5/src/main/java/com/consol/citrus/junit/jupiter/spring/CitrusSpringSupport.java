/*
 * Copyright 2021 the original author or authors.
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

package com.consol.citrus.junit.jupiter.spring;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusSettings;
import com.consol.citrus.CitrusSpringContext;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.common.XmlTestLoader;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.junit.jupiter.CitrusExtensionHelper;
import com.consol.citrus.junit.jupiter.CitrusSupport;
import com.consol.citrus.util.FileUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * JUnit5 extension adding {@link TestCaseRunner} support as well as Citrus annotation based resource injection
 * and lifecycle management such as before/after suite.
 *
 * Extension resolves method parameter of type {@link com.consol.citrus.context.TestContext}, {@link TestCaseRunner}
 * or {@link com.consol.citrus.TestActionRunner} and injects endpoints and resources coming from Citrus Spring application context that
 * is automatically loaded at suite start up. After suite automatically includes Citrus report generation.
 *
 * Extension is based on Citrus Xml test extension that also allows to load test cases from external Spring configuration files.
 *
 * @author Christoph Deppisch
 */
public class CitrusSpringSupport extends CitrusSupport {

    @Override
    public void beforeXmlTestExecution(ExtensionContext extensionContext) {
        CitrusExtensionHelper.getCitrus(extensionContext).run(XmlTestHelper.getXmlTestCase(extensionContext), CitrusExtensionHelper.getTestContext(extensionContext));
    }

    @Override
    public void beforeEachXml(ExtensionContext extensionContext) {
        XmlTestHelper.getXmlTestCase(extensionContext);
    }

    @Override
    protected Citrus getCitrus() {
        if (citrus == null) {
            citrus = Citrus.newInstance(CitrusSpringContext.create());
        }

        return citrus;
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
                for (String fileNamePattern : CitrusSettings.getXmlTestFileNamePattern()) {
                    Resource[] fileResources = new PathMatchingResourcePatternResolver().getResources(packageScan.replace('.', File.separatorChar) + fileNamePattern);
                    for (Resource fileResource : fileResources) {
                        String filePath = fileResource.getFile().getParentFile().getCanonicalPath();

                        if (packageScan.startsWith("file:")) {
                            filePath = "file:" + filePath;
                        }

                        filePath = filePath.substring(filePath.indexOf(packageScan.replace('.', File.separatorChar)));

                        String testName = FileUtils.getBaseName(fileResource.getFilename());

                        XmlTestLoader testLoader = new XmlTestLoader(DynamicTest.class, testName, filePath, citrus.getCitrusContext());
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
            XmlTestLoader testLoader = new XmlTestLoader(DynamicTest.class, testName, packageName, citrus.getCitrusContext());
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
            XmlTestLoader testLoader = new XmlTestLoader(DynamicTest.class, testName, testClass.getPackage().getName(), citrus.getCitrusContext());
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
        XmlTestLoader testLoader = new XmlTestLoader(DynamicTest.class, testName, packageName, citrus.getCitrusContext());
        return DynamicTest.dynamicTest(testName, () -> citrus.run(testLoader.load()));
    }
}
