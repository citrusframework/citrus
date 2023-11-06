/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.testng;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.TestClass;
import org.citrusframework.TestSource;
import org.citrusframework.main.AbstractTestEngine;
import org.citrusframework.main.TestRunConfiguration;
import org.citrusframework.main.scan.ClassPathTestScanner;
import org.citrusframework.main.scan.JarFileTestScanner;
import org.citrusframework.testng.main.TestNGCitrusTest;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class TestNGEngine extends AbstractTestEngine {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(TestNGEngine.class);

    private final List<ITestNGListener> listeners = new ArrayList<>();

    /**
     * Default constructor using run configuration.
     * @param configuration
     */
    public TestNGEngine(TestRunConfiguration configuration) {
        super(configuration);
    }

    public void run() {
        TestNG testng = new TestNG();

        for (ITestNGListener listener : listeners) {
            testng.addListener(listener);
        }

        XmlSuite suite = new XmlSuite();
        testng.setXmlSuites(Collections.singletonList(suite));

        if (getConfiguration().getTestSources().isEmpty()) {
            addTestPackages(suite, getConfiguration());
        } else {
            addTestClasses(suite, getConfiguration());
            addTestSources(suite, getConfiguration());
        }

        testng.run();
    }

    private void addTestSources(XmlSuite suite, TestRunConfiguration configuration) {
        List<TestSource> testSources = configuration.getTestSources().stream()
                .filter(source -> !"java".equals(source.getType()))
                .toList();

        for (TestSource source : testSources) {
            logger.info(String.format("Running test source %s", source.getName()));

            XmlTest test = new XmlTest(suite);
            Map<String, String> parameters = new HashMap<>();
            parameters.put(TestNGCitrusTest.TEST_NAME_PARAM, source.getName());
            parameters.put(TestNGCitrusTest.TEST_SOURCE_PARAM, Optional.ofNullable(source.getFilePath()).orElse(""));
            test.setParameters(parameters);

            XmlClass xmlClass = new XmlClass(TestNGCitrusTest.class);
            xmlClass.setIncludedMethods(Collections.singletonList(new XmlInclude("execute")));
            test.setClasses(Collections.singletonList(xmlClass));
        }
    }

    private void addTestPackages(XmlSuite suite, TestRunConfiguration configuration) {
        List<String> packagesToRun = configuration.getPackages();
        if (packagesToRun == null || packagesToRun.isEmpty()) {
            packagesToRun = Collections.singletonList("");
            logger.info("Running all tests in project");
        }

        for (String packageName : packagesToRun) {
            if (StringUtils.hasText(packageName)) {
                logger.info(String.format("Running tests in package %s", packageName));
            }

            XmlTest test = new XmlTest(suite);
            test.setClasses(new ArrayList<>());

            List<TestClass> classesToRun;
            if (configuration.getTestJar() != null) {
                classesToRun = new JarFileTestScanner(configuration.getTestJar(),
                        configuration.getIncludes()).findTestsInPackage(packageName);
            } else {
                classesToRun = new ClassPathTestScanner(Test.class, configuration.getIncludes()).findTestsInPackage(packageName);
            }

            classesToRun.stream()
                    .peek(testClass -> logger.info(String.format("Running test %s",
                            Optional.ofNullable(testClass.getMethod()).map(method -> testClass.getName() + "#" + method)
                                    .orElseGet(testClass::getName))))
                    .map(testClass -> {
                        try {
                            Class<?> clazz;
                            if (configuration.getTestJar() != null) {
                                clazz = Class.forName(testClass.getName(), false,
                                        new URLClassLoader(new URL[]{configuration.getTestJar().toURI().toURL()}, getClass().getClassLoader()));
                            } else {
                                clazz = Class.forName(testClass.getName());
                            }
                            return clazz;
                        } catch (ClassNotFoundException | MalformedURLException e) {
                            logger.warn("Unable to read test class: " + testClass.getName());
                            return Void.class;
                        }
                    })
                    .filter(clazz -> !clazz.equals(Void.class))
                    .map(XmlClass::new)
                    .forEach(test.getClasses()::add);

            logger.info(String.format("Found %s test classes to execute", test.getClasses().size()));
        }
    }

    private void addTestClasses(XmlSuite suite, TestRunConfiguration configuration) {
        List<TestClass> testClasses = configuration.getTestSources().stream()
                .filter(source -> "java".equals(source.getType()))
                .map(TestSource::getName)
                .map(TestClass::fromString)
                .toList();

        for (TestClass testClass : testClasses) {
            logger.info(String.format("Running test %s",
                    Optional.ofNullable(testClass.getMethod()).map(method -> testClass.getName() + "#" + method)
                            .orElseGet(testClass::getName)));

            XmlTest test = new XmlTest(suite);
            test.setClasses(new ArrayList<>());

            try {
                Class<?> clazz;
                if (configuration.getTestJar() != null) {
                    clazz = Class.forName(testClass.getName(), false,
                            new URLClassLoader(new URL[]{configuration.getTestJar().toURI().toURL()}, getClass().getClassLoader()));
                } else {
                    clazz = Class.forName(testClass.getName());
                }

                XmlClass xmlClass = new XmlClass(clazz);
                if (StringUtils.hasText(testClass.getMethod())) {
                    xmlClass.setIncludedMethods(Collections.singletonList(new XmlInclude(testClass.getMethod())));
                }

                test.getClasses().add(xmlClass);
            } catch (ClassNotFoundException | MalformedURLException e) {
                logger.warn("Unable to read test class: " + testClass.getName());
            }
        }
    }

    /**
     * Adds run listener in fluent API.
     * @param listener
     */
    public TestNGEngine addTestListener(ITestNGListener listener) {
        this.listeners.add(listener);
        return this;
    }
}
