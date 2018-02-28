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

import com.consol.citrus.TestClass;
import com.consol.citrus.main.CitrusApp;
import com.consol.citrus.main.CitrusAppConfiguration;
import com.consol.citrus.remote.CitrusRemoteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

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
        runPackage("");
    }

    /**
     * Run Citrus application with given base test package name.
     * @param basePackage
     */
    public void runPackage(String basePackage) {
        CitrusAppConfiguration citrusAppConfiguration = new CitrusAppConfiguration();
        
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(Optional.ofNullable(configuration.getTestNamePattern()).orElse("^.*" + configuration.getTestNameSuffix() + "$"))) {
            @Override
            protected boolean match(ClassMetadata metadata) {
                if (!super.match(metadata)) {
                    return false;
                }

                try {
                    Class<?> clazz = Class.forName(metadata.getClassName());
                    if (clazz.isAnnotationPresent(Test.class)) {
                        return true;
                    }

                    AtomicBoolean hasTestMethod = new AtomicBoolean(false);
                    ReflectionUtils.doWithMethods(clazz, method -> hasTestMethod.set(true), method -> AnnotationUtils.findAnnotation(method, Test.class) != null);
                    return hasTestMethod.get();
                } catch (NoClassDefFoundError | ClassNotFoundException e) {
                    log.warn("Unable to access class: " + metadata.getClassName());
                    return false;
                }
            }
        });

        provider.findCandidateComponents(basePackage)
                .stream()
                .map(BeanDefinition::getBeanClassName)
                .distinct()
                .map(className -> {
                    try {
                        Class<?> clazz = Class.forName(className);
                        log.debug("Found test candidate: " + className);
                        return clazz;
                    } catch (NoClassDefFoundError | ClassNotFoundException e) {
                        log.warn("Unable to access test class: " + className);
                        return Void.class;
                    }
                })
                .filter(clazz -> !clazz.equals(Void.class))
                .map(clazz -> new TestClass(clazz.getName()))
                .forEach(citrusAppConfiguration.getTestClasses()::add);

        log.info(String.format("Found %s test classes to execute", citrusAppConfiguration.getTestClasses().size()));

        if (citrusAppConfiguration.getTestClasses().isEmpty()) {
            citrusAppConfiguration.getPackages().add(basePackage);
        }

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

        TestClass test = new TestClass(className);
        if (StringUtils.hasText(methodName)) {
            test.setMethod(methodName);
        }

        citrusAppConfiguration.getTestClasses().add(test);
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
