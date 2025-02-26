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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.eventbus.RandomUuidGenerator;
import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.resource.ClasspathSupport;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.snippets.SnippetType;
import org.citrusframework.TestSource;
import org.citrusframework.common.TestSourceHelper;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.main.AbstractTestEngine;
import org.citrusframework.main.TestRunConfiguration;
import org.citrusframework.spi.ClasspathResourceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CucumberTestEngine extends AbstractTestEngine {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CucumberTestEngine.class);

    public CucumberTestEngine(TestRunConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void run() {
        RuntimeOptions propertiesFileOptions = new CucumberPropertiesParser().parse(CucumberProperties.fromPropertiesFile()).build();

        RuntimeOptions annotationOptions;
        Optional<TestSource> javaClass = getConfiguration().getTestSources()
                .stream()
                .filter(source -> "java".equals(source.getType()))
                .findFirst();

        if (javaClass.isEmpty()) {
            annotationOptions = propertiesFileOptions;
        } else {
            try {
                annotationOptions = new CucumberOptionsAnnotationParser()
                        .withOptionsProvider(GenericCucumberOptions::new)
                        .parse(Class.forName(javaClass.get().getName()))
                        .setUuidGeneratorClass(RandomUuidGenerator.class)
                        .addDefaultGlueIfAbsent()
                        .build(propertiesFileOptions);
            } catch (ClassNotFoundException e) {
                throw new CitrusRuntimeException("Unable to find test class in classpath: " + javaClass.get().getName());
            }
        }

        List<String> features = new ArrayList<>();
        addToFeatures(features, getConfiguration().getTestSources());

        if (!features.isEmpty()) {
            System.setProperty("cucumber.features", String.join(",", features));
        }

        RuntimeOptions environmentOptions = new CucumberPropertiesParser().parse(CucumberProperties.fromEnvironment()).build(annotationOptions);
        RuntimeOptions systemOptions = new CucumberPropertiesParser().parse(CucumberProperties.fromSystemProperties()).build(environmentOptions);

        List<String> args = new ArrayList<>();

        List<String> packagesToRun = getConfiguration().getPackages();
        if (packagesToRun == null || packagesToRun.isEmpty()) {
            logger.info("Running all tests in project");
        } else if (StringUtils.hasText(packagesToRun.get(0))) {
            logger.info(String.format("Running tests in package %s", packagesToRun.get(0)));
            args.add(ClasspathSupport.CLASSPATH_SCHEME_PREFIX + packagesToRun.get(0).replaceAll("\\.", "/"));

            args.add("--glue");
            args.add(packagesToRun.get(0));
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
                                .getResources(sourceDir.getLocation())
                                .stream()
                                .map(Path::toString)
                                .map(TestSourceHelper::create)
                                .collect(Collectors.toList()));
                    } catch (IOException e) {
                        throw new CitrusRuntimeException("Failed to resolve files from resource directory '%s'".formatted(sourceDir.getLocation()), e);
                    }
                } else {
                    addToFeatures(features, Optional.ofNullable(sourceDir.getFile().list())
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
                .map(TestSource::getName)
                .toList());
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
