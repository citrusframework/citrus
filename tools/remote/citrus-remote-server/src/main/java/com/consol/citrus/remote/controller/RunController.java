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

package com.consol.citrus.remote.controller;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.main.CitrusApp;
import com.consol.citrus.main.CitrusAppConfiguration;
import com.consol.citrus.remote.CitrusRemoteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class RunController {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(RunController.class);

    private final CitrusRemoteConfiguration configuration;

    /**
     * Constructor with given configuration.
     * @param configuration
     */
    public RunController(CitrusRemoteConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Run all tests found in classpath.
     */
    public void runAll() {
        try {
            List<String> packages = Stream.of(new PathMatchingResourcePatternResolver().getResources("classpath*:**/*IT.class"))
                    .parallel()
                    .map(resource -> {
                        try {
                            return resource.getFile().getPath();
                        } catch (IOException e) {
                            log.warn(String.format("Unable to access class %s in classpath", resource.getFilename()), e);
                            return "";
                        }
                    })
                    .filter(StringUtils::hasText)
                    .map(className -> {
                        try {
                            return Class.forName(className).getPackage().getName();
                        } catch (ClassNotFoundException e) {
                            log.warn(String.format("Unable to access class for name %s", className), e);
                            return "";
                        }
                    })
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.toList());

            packages.forEach(this::runPackage);
        } catch (IOException e) {
            log.warn("Unable to find test classes in classpath", e);
        }
    }

    /**
     * Run Citrus application with given test package name.
     * @param testPackage
     */
    public void runPackage(String testPackage) {
        CitrusAppConfiguration citrusAppConfiguration = new CitrusAppConfiguration();
        citrusAppConfiguration.setPackageName(testPackage);
        citrusAppConfiguration.setConfigClass(configuration.getConfigClass());

        run(citrusAppConfiguration);
    }

    /**
     * Run Citrus application with given test class name.
     * @param testClass
     */
    public void runClass(String testClass) {
        CitrusAppConfiguration citrusAppConfiguration = new CitrusAppConfiguration();

        String className;
        String methodName = null;
        if (testClass.contains("#")) {
            className = testClass.substring(0, testClass.indexOf("#"));
            methodName = testClass.substring(testClass.indexOf("#") + 1);
        } else {
            className = testClass;
        }

        if (StringUtils.hasText(methodName)) {
            citrusAppConfiguration.setTestMethod(methodName);
        }

        try {
            citrusAppConfiguration.setTestClass(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new CitrusRuntimeException("Unable to test class: " + className, e);
        }

        citrusAppConfiguration.setConfigClass(configuration.getConfigClass());

        run(citrusAppConfiguration);
    }

    /**
     * Run tests with default configuration.
     */
    public void run() {
        this.run(configuration);
    }

    /**
     * Run Citrus application with given configuration and cached Citrus instance.
     * @param citrusAppConfiguration
     */
    private void run(CitrusAppConfiguration citrusAppConfiguration) {
        CitrusApp citrusApp = new CitrusApp(citrusAppConfiguration);
        citrusApp.run();
    }
}
