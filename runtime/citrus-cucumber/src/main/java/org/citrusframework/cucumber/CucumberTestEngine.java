/*
 * Copyright the original author or authors.
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

package org.citrusframework.cucumber;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.eventbus.RandomUuidGenerator;
import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.options.Constants;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.resource.ClasspathSupport;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.snippets.SnippetType;
import org.citrusframework.CitrusSettings;
import org.citrusframework.TestSource;
import org.citrusframework.common.TestSourceHelper;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.base.main.AbstractTestEngine;
import org.citrusframework.api.main.TestRunConfiguration;
import org.citrusframework.spi.ClasspathResourceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.ClassLoaderHelper;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CucumberTestEngine extends AbstractTestEngine {

    private static final Logger logger = LoggerFactory.getLogger(CucumberTestEngine.class);

    public CucumberTestEngine(TestRunConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void run() {
        RuntimeOptions propertiesFileOptions = new CucumberPropertiesParser().parse(CucumberProperties.fromPropertiesFile()).build();

        List<String> features = new ArrayList<>();

        RuntimeOptions annotationOptions;
        Optional<TestSource> javaClass = getConfiguration().getTestSources()
                .stream()
                .filter(source -> "java".equals(source.getType()))
                .filter(source -> {
                    try {
                        Class<?> type = Class.forName(source.getName(), false, ClassLoaderHelper.getClassLoader());
                        return isCucumberOptionsClass(type) || isSuiteClass(type);
                    } catch (Exception e) {
                        try {
                            String sourceContent = FileUtils.readToString(source.getSourceFile());
                            return sourceContent.contains("@CucumberOptions") || sourceContent.contains("@Suite");
                        } catch (Exception ex) {
                            return false;
                        }
                    }
                })
                .findFirst();

        if (javaClass.isPresent()) {
            try {
                Class<?> testClass = Class.forName(javaClass.get().getName(), true, ClassLoaderHelper.getClassLoader());
                if (isSuiteClass(testClass)) {
                    annotationOptions = parseSuiteOptions(testClass, propertiesFileOptions, features);
                } else {
                    annotationOptions = new CucumberOptionsAnnotationParser()
                            .withOptionsProvider(GenericCucumberOptions::new)
                            .parse(testClass)
                            .setUuidGeneratorClass(RandomUuidGenerator.class)
                            .addDefaultGlueIfAbsent()
                            .build(propertiesFileOptions);
                }
            } catch (ClassNotFoundException e) {
                throw new CitrusRuntimeException("Unable to find test class in classpath: " + javaClass.get().getName());
            }
        } else {
            annotationOptions = propertiesFileOptions;
        }

        addToFeatures(features, getConfiguration().getTestSources());

        String previousFeatures = System.getProperty(Constants.FEATURES_PROPERTY_NAME);
        if (!features.isEmpty()) {
            System.setProperty(Constants.FEATURES_PROPERTY_NAME, String.join(",", features));
        }

        RuntimeOptions environmentOptions;
        RuntimeOptions systemOptions;
        try {
            environmentOptions = new CucumberPropertiesParser().parse(CucumberProperties.fromEnvironment()).build(annotationOptions);
            systemOptions = new CucumberPropertiesParser().parse(CucumberProperties.fromSystemProperties()).build(environmentOptions);
        } finally {
            restoreFeaturesProperty(previousFeatures);
        }

        List<String> args = new ArrayList<>();

        List<String> packagesToRun = getConfiguration().getPackages();
        if (packagesToRun == null || packagesToRun.isEmpty()) {
            logger.info("Running all tests in project");
        } else if (StringUtils.hasText(packagesToRun.get(0))) {
            logger.info("Running tests in package {}", packagesToRun.get(0));
            args.add(ClasspathSupport.CLASSPATH_SCHEME_PREFIX + packagesToRun.get(0).replaceAll("\\.", "/"));

            args.add("--glue");
            args.add(packagesToRun.get(0));
        }

        if (StringUtils.hasText(getConfiguration().getWorkDir())) {
            CitrusSettings.setWorkDir(getConfiguration().getWorkDir());
        }

        CommandlineOptionsParser commandlineOptionsParser = new CommandlineOptionsParser(System.out);
        RuntimeOptions runtimeOptions = commandlineOptionsParser.parse(args.toArray(new String[0]))
                .addDefaultGlueIfAbsent()
                .addDefaultFeaturePathIfAbsent()
                .addDefaultSummaryPrinterIfNotDisabled()
                .build(systemOptions);

        Runtime runtime = Runtime.builder()
                .withRuntimeOptions(runtimeOptions)
                .withAdditionalPlugins(new CitrusReporter())
                .build();

        runtime.run();
    }

    private static boolean isCucumberOptionsClass(Class<?> type) {
        return Arrays.stream(type.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().getSimpleName().equals("CucumberOptions"));
    }

    private static boolean isSuiteClass(Class<?> type) {
        return Arrays.stream(type.getAnnotations())
                .anyMatch(annotation -> "org.junit.platform.suite.api.Suite".equals(annotation.annotationType().getName()));
    }

    /**
     * Reads JUnit Platform suite annotations such as {@code @ConfigurationParameter},
     * {@code @SelectClasspathResource} and {@code @SelectPackages} in a generic way using
     * method invocation via reflection.
     * This keeps Citrus Cucumber compatible with classes that use the JUnit Platform suite support
     * instead of the deprecated Cucumber JUnit 4 runner annotations.
     */
    RuntimeOptions parseSuiteOptions(Class<?> testClass, RuntimeOptions propertiesFileOptions, List<String> features) {
        Map<String, String> parameters = new LinkedHashMap<>();

        Arrays.stream(testClass.getAnnotations())
                .filter(annotation -> annotation.annotationType().getName().startsWith("org.junit.platform.suite.api."))
                .forEach(annotation -> {
                    switch (annotation.annotationType().getSimpleName()) {
                        case "ConfigurationParameter" -> parameters.put(
                                annotationValue(annotation, "key"), annotationValue(annotation, "value"));
                        case "ConfigurationParameters" -> Arrays.stream(annotationValues(annotation, "value"))
                                .forEach(parameter -> parameters.put(
                                        annotationValue(parameter, "key"), annotationValue(parameter, "value")));
                        case "SelectClasspathResource" -> features.add(
                                ClasspathSupport.CLASSPATH_SCHEME_PREFIX + annotationValue(annotation, "value"));
                        case "SelectClasspathResources" -> Arrays.stream(annotationValues(annotation, "value"))
                                .forEach(selector -> features.add(
                                        ClasspathSupport.CLASSPATH_SCHEME_PREFIX + annotationValue(selector, "value")));
                        case "SelectPackages" -> Arrays.stream(annotationStringValues(annotation, "value"))
                                .forEach(value -> features.add(
                                        ClasspathSupport.CLASSPATH_SCHEME_PREFIX + value.replace('.', '/')));
                        default -> {
                            // ignore other suite annotations
                        }
                    }
                });

        parameters.putIfAbsent(Constants.GLUE_PROPERTY_NAME, testClass.getPackageName());

        return new CucumberPropertiesParser().parse(parameters)
                .setUuidGeneratorClass(RandomUuidGenerator.class)
                .addDefaultGlueIfAbsent()
                .build(propertiesFileOptions);
    }

    /**
     * Restores the given value of the global cucumber features system property. The property is only required
     * while the runtime options are resolved. Leaving it behind would leak the feature selection of this run into
     * any subsequent Cucumber run within the same JVM.
     */
    private static void restoreFeaturesProperty(String previousValue) {
        if (previousValue != null) {
            System.setProperty(Constants.FEATURES_PROPERTY_NAME, previousValue);
        } else {
            System.clearProperty(Constants.FEATURES_PROPERTY_NAME);
        }
    }

    private static String annotationValue(Annotation annotation, String attribute) {
        try {
            return (String) annotation.annotationType().getMethod(attribute).invoke(annotation);
        } catch (ReflectiveOperationException e) {
            throw new CitrusRuntimeException(String.format("Failed to retrieve %s value on annotation type %s",
                    attribute, annotation.annotationType()), e);
        }
    }

    private static Annotation[] annotationValues(Annotation annotation, String attribute) {
        try {
            return (Annotation[]) annotation.annotationType().getMethod(attribute).invoke(annotation);
        } catch (ReflectiveOperationException e) {
            throw new CitrusRuntimeException(String.format("Failed to retrieve %s value on annotation type %s",
                    attribute, annotation.annotationType()), e);
        }
    }

    private static String[] annotationStringValues(Annotation annotation, String attribute) {
        try {
            return (String[]) annotation.annotationType().getMethod(attribute).invoke(annotation);
        } catch (ReflectiveOperationException e) {
            throw new CitrusRuntimeException(String.format("Failed to retrieve %s value on annotation type %s",
                    attribute, annotation.annotationType()), e);
        }
    }

    private void addToFeatures(List<String> features, List<TestSource> testSources) {
        List<TestSource> directories = testSources.stream()
                .filter(source -> "directory".equals(source.getType()))
                .toList();

        for (TestSource directory : directories) {
            Resource sourceDir = Resources.create(directory.getFilePath());
            if (sourceDir.exists()) {
                if (sourceDir instanceof Resources.ClasspathResource) {
                    try {
                        addToFeatures(features, new ClasspathResourceResolver()
                                .getResources(sourceDir.location())
                                .stream()
                                .map(Path::toString)
                                .map(TestSourceHelper::create)
                                .collect(Collectors.toList()));
                    } catch (IOException e) {
                        throw new CitrusRuntimeException("Failed to resolve files from resource directory '%s'".formatted(sourceDir.location()), e);
                    }
                } else {
                    addToFeatures(features, Optional.ofNullable(sourceDir.file().list())
                            .stream()
                            .flatMap(Arrays::stream)
                            .map(file -> directory.getFilePath() + File.separator + file)
                            .map(TestSourceHelper::create)
                            .collect(Collectors.toList()));
                }
            }
        }

        features.addAll(testSources.stream()
                .filter(source -> "cucumber".equals(source.getType()) ||
                        Optional.ofNullable(source.getFilePath())
                                .filter(it -> it.endsWith(".feature"))
                                .isPresent() ||
                        "feature".equals(FileUtils.getFileExtension(source.getName()))
                )
                .map(TestSource::getFilePath)
                .toList());
    }

    /**
     * Cucumber options reading values from annotation in a generic way using method invocation via reflection.
     */
    private record GenericCucumberOptions(
            Annotation options) implements CucumberOptionsAnnotationParser.CucumberOptions {

        private GenericCucumberOptions(Class<?> options) {
            this(
                    Arrays.stream(options.getAnnotations())
                            .filter(annotation -> annotation.annotationType().getSimpleName().equals("CucumberOptions"))
                            .findFirst()
                            .orElseThrow(() -> new CitrusRuntimeException("Missing CucumberOptions annotation on test class: " + options.getName()))
            );
        }

        @SuppressWarnings("unchecked")
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
        public Class<? extends UuidGenerator> uuidGenerator() {
            return getOptionValue("uuidGenerator");
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
