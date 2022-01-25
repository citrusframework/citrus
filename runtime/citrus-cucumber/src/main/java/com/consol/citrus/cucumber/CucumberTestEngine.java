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

package com.consol.citrus.cucumber;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.consol.citrus.TestClass;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.main.AbstractTestEngine;
import com.consol.citrus.main.TestRunConfiguration;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.resource.ClasspathSupport;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.snippets.SnippetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
public class CucumberTestEngine extends AbstractTestEngine {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(CucumberTestEngine.class);

    public CucumberTestEngine(TestRunConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void run() {
        RuntimeOptions propertiesFileOptions = new CucumberPropertiesParser().parse(CucumberProperties.fromPropertiesFile()).build();

        RuntimeOptions annotationOptions;
        if (CollectionUtils.isEmpty(getConfiguration().getTestClasses())) {
            annotationOptions = propertiesFileOptions;
        } else {
            TestClass testClass = getConfiguration().getTestClasses().get(0);
            try {
                annotationOptions = new CucumberOptionsAnnotationParser()
                        .withOptionsProvider(GenericCucumberOptions::new)
                        .parse(Class.forName(testClass.getName()))
                        .addDefaultGlueIfAbsent()
                        .build(propertiesFileOptions);
            } catch (ClassNotFoundException e) {
                throw new CitrusRuntimeException("Unable to find test class in classpath: " + testClass.getName());
            }
        }

        RuntimeOptions environmentOptions = new CucumberPropertiesParser().parse(CucumberProperties.fromEnvironment()).build(annotationOptions);
        RuntimeOptions systemOptions = new CucumberPropertiesParser().parse(CucumberProperties.fromSystemProperties()).build(environmentOptions);

        List<String> args = new ArrayList<>();

        List<String> packagesToRun = getConfiguration().getPackages();
        if (CollectionUtils.isEmpty(packagesToRun)) {
            LOG.info("Running all tests in project");
        } else if (StringUtils.hasText(packagesToRun.get(0))) {
            LOG.info(String.format("Running tests in package %s", packagesToRun.get(0)));
            args.add(ClasspathSupport.CLASSPATH_SCHEME_PREFIX + packagesToRun.get(0).replaceAll("\\.", "/"));

            args.add("--glue");
            args.add(packagesToRun.get(0));
        }

        CommandlineOptionsParser commandlineOptionsParser = new CommandlineOptionsParser(System.out);
        RuntimeOptions runtimeOptions = commandlineOptionsParser.parse(args.toArray(new String[0]))
                .addDefaultGlueIfAbsent()
                .addDefaultFeaturePathIfAbsent()
                .addDefaultFormatterIfAbsent()
                .addDefaultSummaryPrinterIfAbsent()
                .build(systemOptions);

        Runtime runtime = Runtime.builder()
                .withRuntimeOptions(runtimeOptions)
                .withAdditionalPlugins(new CitrusReporter())
                .build();

        runtime.run();
    }

    /**
     * Cucumber options reading values from annotation in a generic way using method invocation via reflection.
     */
    private static class GenericCucumberOptions implements CucumberOptionsAnnotationParser.CucumberOptions {
        private final Annotation options;

        public GenericCucumberOptions(Class<?> clazz) {
            options = Arrays.stream(clazz.getAnnotations())
                    .filter(annotation -> annotation.annotationType().getSimpleName().equals("CucumberOptions"))
                    .findFirst()
                    .orElseThrow(() -> new CitrusRuntimeException("Missing CucumberOptions annotation on test class: " + clazz.getName()));
        }

        <T> T getOptionValue(String optionName) {
            try {
                Method method = options.annotationType().getDeclaredMethod(optionName);
                return (T) method.invoke(options);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new CitrusRuntimeException(String.format("Failed to retrieve Cucumber option %s " +
                        "on annotation type %s", optionName, options.annotationType()));
            }
        }

        @Override
        public boolean dryRun() {
            return getOptionValue("dryRun");
        }

        @Override
        public boolean strict() {
            return getOptionValue("strict");
        }

        @Override
        public String[] features() {
            return getOptionValue("features");
        }

        @Override
        public String[] glue() {
            return getOptionValue("glue");
        }

        @Override
        public String[] extraGlue() {
            return getOptionValue("extraGlue");
        }

        @Override
        public String tags() {
            return getOptionValue("tags");
        }

        @Override
        public String[] plugin() {
            return getOptionValue("plugin");
        }

        @Override
        public boolean publish() {
            return getOptionValue("publish");
        }

        @Override
        public boolean monochrome() {
            return getOptionValue("monochrome");
        }

        @Override
        public String[] name() {
            return getOptionValue("name");
        }

        @Override
        public SnippetType snippets() {
            return SnippetType.UNDERSCORE;
        }

        @Override
        public Class<? extends ObjectFactory> objectFactory() {
            Class<? extends ObjectFactory> factoryType = getOptionValue("objectFactory");

            if (factoryType.getSimpleName().equals("NoObjectFactory")) {
                return null;
            }

            return factoryType;
        }
    }
}
