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

package com.consol.citrus.junit;

import com.consol.citrus.TestClass;
import com.consol.citrus.main.AbstractTestEngine;
import com.consol.citrus.main.TestRunConfiguration;
import com.consol.citrus.main.scan.ClassPathTestScanner;
import org.junit.Test;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class JUnit4TestEngine extends AbstractTestEngine {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JUnit4TestEngine.class);

    /**
     * Default constructor using run configuration.
     * @param configuration
     */
    public JUnit4TestEngine(TestRunConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void run() {
        if (!CollectionUtils.isEmpty(getConfiguration().getTestClasses())) {
            run(getConfiguration().getTestClasses());
        }

        List<String> packagesToRun = getConfiguration().getPackages();
        if (CollectionUtils.isEmpty(packagesToRun)) {
            packagesToRun = Collections.singletonList("");
            log.info("Running all tests in project");
        }

        List<TestClass> classesToRun = new ArrayList<>();
        for (String packageName : packagesToRun) {
            if (StringUtils.hasText(packageName)) {
                log.info(String.format("Running tests in package %s", packageName));
            }

            classesToRun.addAll(new ClassPathTestScanner(getConfiguration()).findTestsInPackage(packageName, Test.class));
        }

        log.info(String.format("Found %s test classes to execute", classesToRun.size()));
        run(classesToRun);
    }

    /**
     * Run given set of test classes with JUnit4.
     * @param classesToRun
     */
    private void run(List<TestClass> classesToRun) {
        Runner junitRunner = Request.classes(classesToRun
                .stream()
                .peek(testClass -> log.info(String.format("Running test %s", Optional.ofNullable(testClass.getMethod()).map(method -> testClass.getName() + "#" + method).orElse(testClass.getName()))))
                .map(testClass -> {
                    try {
                        Class<?> clazz = Class.forName(testClass.getName());
                        log.debug("Found test candidate: " + testClass.getName());
                        return clazz;
                    } catch (ClassNotFoundException e) {
                        log.warn("Unable to read test class: " + testClass.getName());
                        return Void.class;
                    }
                })
                .filter(clazz -> !clazz.equals(Void.class))
                .toArray(Class[]::new))
                .getRunner();

        RunNotifier notifier = new RunNotifier();
        junitRunner.run(notifier);
    }
}
