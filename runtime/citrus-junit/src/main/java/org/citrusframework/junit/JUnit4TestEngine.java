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

package org.citrusframework.junit;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.citrusframework.TestClass;
import org.citrusframework.TestSource;
import org.citrusframework.main.AbstractTestEngine;
import org.citrusframework.main.TestRunConfiguration;
import org.citrusframework.main.scan.ClassPathTestScanner;
import org.citrusframework.main.scan.JarFileTestScanner;
import org.citrusframework.util.StringUtils;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class JUnit4TestEngine extends AbstractTestEngine {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JUnit4TestEngine.class);

    private final List<RunListener> listeners = new ArrayList<>();

    /**
     * Default constructor using run configuration.
     * @param configuration
     */
    public JUnit4TestEngine(TestRunConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void run() {
        if (getConfiguration().getTestSources() != null && !getConfiguration().getTestSources().isEmpty()) {
            run(getConfiguration().getTestSources());
        } else {
            List<String> packagesToRun = getConfiguration().getPackages();
            if (packagesToRun.isEmpty() && getConfiguration().getTestSources().isEmpty()) {
                packagesToRun = Collections.singletonList("");
                logger.info("Running all tests in project");
            }

            List<TestSource> classesToRun = new ArrayList<>();
            for (String packageName : packagesToRun) {
                if (StringUtils.hasText(packageName)) {
                    logger.info(String.format("Running tests in package %s", packageName));
                }

                if (getConfiguration().getTestJar() != null) {
                    classesToRun.addAll(new JarFileTestScanner(getConfiguration().getTestJar(),
                            getConfiguration().getIncludes()).findTestsInPackage(packageName));
                } else {
                    classesToRun.addAll(new ClassPathTestScanner(Test.class,
                            getConfiguration().getIncludes()).findTestsInPackage(packageName));
                }
            }

            logger.info(String.format("Found %s test classes to execute", classesToRun.size()));
            run(classesToRun);
        }
    }

    /**
     * Run given set of test sources with JUnit4.
     * @param sourcesToRun
     */
    private void run(List<TestSource> sourcesToRun) {
        JUnitCore junit = new JUnitCore();

        for (RunListener listener : listeners) {
            junit.addListener(listener);
        }

        junit.run(sourcesToRun
                .stream()
                .filter(source -> source.getType().equals("java"))
                .peek(source -> {
                    if (source instanceof TestClass testClass) {
                        logger.info(String.format("Running test %s",
                                Optional.ofNullable(testClass.getMethod())
                                        .map(method -> testClass.getName() + "#" + method)
                                        .orElseGet(testClass::getName)));
                    } else {
                        logger.info(String.format("Running test %s", source.getName()));
                    }
                })
                .map(source -> {
                    try {
                        Class<?> clazz;
                        if (getConfiguration().getTestJar() != null) {
                            clazz = Class.forName(source.getName(), false,
                                    new URLClassLoader(new URL[]{ getConfiguration().getTestJar().toURI().toURL() }, getClass().getClassLoader()));
                        } else {
                            clazz = Class.forName(source.getName());
                        }
                        logger.debug("Found test candidate: " + source.getName());
                        return clazz;
                    } catch (ClassNotFoundException | MalformedURLException e) {
                        logger.warn("Unable to read test class: " + source.getName());
                        return Void.class;
                    }
                })
                .filter(clazz -> !clazz.equals(Void.class))
                .toArray(Class[]::new));
    }

    /**
     * Adds run listener in fluent API.
     * @param listener
     */
    public JUnit4TestEngine addRunListener(RunListener listener) {
        this.listeners.add(listener);
        return this;
    }
}
