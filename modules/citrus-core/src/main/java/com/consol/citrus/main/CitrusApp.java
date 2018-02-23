/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.main;

import com.consol.citrus.Citrus;
import com.consol.citrus.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.util.*;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * Main command line application callable via static run methods and command line main method invocation.
 * Command line options are passed to the application for optional arguments. Application will run until the
 * duration time option has passed by or until the JVM terminates.
 *
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class CitrusApp {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusApp.class);

    /** Endpoint configuration */
    private final CitrusAppConfiguration configuration;

    /** Completed future marking completed state */
    protected final CompletableFuture<Boolean> completed = new CompletableFuture<>();

    /**
     * Default constructor using default configuration.
     */
    public CitrusApp() {
        this(new CitrusAppConfiguration());
    }

    /**
     * Construct with given configuration.
     * @param configuration
     */
    public CitrusApp(CitrusAppConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Construct from command line arguments.
     * @param args
     */
    public CitrusApp(String[] args) {
        this(CitrusAppOptions.apply(args));
    }

    /**
     * Main method with command line arguments.
     * @param args
     */
    public static void main(String[] args) {
        CitrusApp citrusApp = new CitrusApp(args);

        if (citrusApp.configuration.getTimeToLive() > 0) {
            CompletableFuture.runAsync(() -> {
                try {
                    new CompletableFuture<Void>().get(citrusApp.configuration.getTimeToLive(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    log.info(String.format("Shutdown Citrus application after %s ms", citrusApp.configuration.getTimeToLive()));
                    citrusApp.stop();
                }
            });
        }

        if (citrusApp.configuration.isSkipTests()) {
            if (citrusApp.configuration.getConfigClass() != null) {
                Citrus.newInstance(citrusApp.configuration.getConfigClass());
            } else {
                Citrus.newInstance();
            }
        } else {
            try {
                citrusApp.run();
            } finally {
                if (citrusApp.configuration.getTimeToLive() == 0) {
                    citrusApp.stop();
                }
            }
        }

        if (citrusApp.configuration.isSystemExit()) {
            if (citrusApp.waitForCompletion()) {
                System.exit(0);
            } else {
                System.exit(-1);
            }
        } else {
            citrusApp.waitForCompletion();
        }
    }

    /**
     * Run application with prepared Citrus instance.
     */
    public void run() {
        if (isCompleted()) {
            log.info("Not executing tests as application state is completed!");
            return;
        }

        TestNG testng = new TestNG();

        XmlSuite suite = new XmlSuite();
        testng.setXmlSuites(Collections.singletonList(suite));

        for (TestClass testClass : configuration.getTestClasses()) {
            log.info(String.format("Running test %s", Optional.ofNullable(testClass.getMethod()).map(method -> testClass.getName() + "#" + method).orElse(testClass.getName())));

            XmlTest test = new XmlTest(suite);
            test.setClasses(new ArrayList<>());

            XmlClass clazz = new XmlClass(testClass.getName());

            if (StringUtils.hasText(testClass.getMethod())) {
                clazz.setIncludedMethods(Collections.singletonList(new XmlInclude(testClass.getMethod())));
            }

            test.getClasses().add(clazz);
        }

        for (String packageName : configuration.getPackages()) {
            log.info(String.format("Running tests in package %s", packageName));

            XmlTest test = new XmlTest(suite);
            XmlPackage xmlPackage = new XmlPackage();
            xmlPackage.setName(packageName);
            test.setPackages(Collections.singletonList(xmlPackage));
        }

        if (CollectionUtils.isEmpty(configuration.getPackages()) && CollectionUtils.isEmpty(configuration.getTestClasses())) {
            log.info("Running all tests in project");

            XmlTest test = new XmlTest(suite);
            ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
            provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(Optional.ofNullable(configuration.getTestNamePattern()).orElse("^.*" + configuration.getTestNameSuffix() + "\\.class$"))));
            provider.addIncludeFilter(new AbstractClassTestingTypeFilter() {
                @Override
                protected boolean match(ClassMetadata metadata) {
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


            provider.findCandidateComponents("")
                    .stream()
                    .map(BeanDefinition::getBeanClassName)
                    .distinct()
                    .map(className -> {
                        try {
                            return Class.forName(className);
                        } catch (NoClassDefFoundError | ClassNotFoundException e) {
                            log.warn("Unable to access test class: " + className);
                            return Void.class;
                        }
                    })
                    .filter(clazz -> !clazz.equals(Void.class))
                    .map(clazz -> new XmlClass(clazz.getName()))
                    .forEach(test.getClasses()::add);
        }

        testng.run();
    }

    /**
     * Completes this application.
     */
    public void complete() {
        completed.complete(true);
    }

    /**
     * Waits for completed state of application.
     * @return
     */
    public boolean waitForCompletion() {
        try {
            return completed.get();
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Failed to wait for application completion", e);
        }

        return false;
    }

    /**
     * Stop application by setting completed state and stop application context.
     */
    private void stop() {
        complete();

        Citrus citrus = Citrus.CitrusInstanceManager.getSingleton();
        if (citrus != null) {
            log.info("Closing Citrus and its application context");
            citrus.close();
        }
    }

    /**
     * Gets the value of the completed property.
     *
     * @return the completed
     */
    public boolean isCompleted() {
        return completed.isDone();
    }
}
