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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import org.testng.TestNG;
import org.testng.xml.*;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.*;

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

    /** Citrus framework instance */
    private Citrus instance;

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

        Citrus citrus = citrusApp.createInstance(citrusApp.configuration);
        if (!citrusApp.configuration.isSkipTests()) {
            citrusApp.run(citrus);
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
     * Run application with new/created Citrus instance.
     */
    public void run() {
        this.run(createInstance(configuration));
    }

    /**
     * Run application with prepared Citrus instance.
     */
    public void run(Citrus citrus) {
        try {
            if (isCompleted()) {
                log.info("Not executing tests as application state is completed!");
                return;
            }

            TestNG testng = new TestNG();

            XmlSuite suite = new XmlSuite();
            testng.setXmlSuites(Collections.singletonList(suite));
            XmlTest test = null;

            if (configuration.getTestClass() != null) {
                log.info(String.format("Running test %s", Optional.ofNullable(configuration.getTestMethod()).map(method -> configuration.getTestClass().getName() + "#" + method).orElse(configuration.getTestClass().getName())));

                test = new XmlTest(suite);
                XmlClass clazz = new XmlClass();
                clazz.setClass(configuration.getTestClass());

                if (StringUtils.hasText(configuration.getTestMethod())) {
                    clazz.setIncludedMethods(Collections.singletonList(new XmlInclude(configuration.getTestMethod())));
                }

                test.setClasses(Collections.singletonList(clazz));
            }

            if (StringUtils.hasText(configuration.getPackageName())) {
                log.info(String.format("Running tests in package %s", configuration.getPackageName()));

                test = new XmlTest(suite);
                XmlPackage xmlPackage = new XmlPackage();
                xmlPackage.setName(configuration.getPackageName());
                test.setPackages(Collections.singletonList(xmlPackage));
            }

            if (test == null) {
                log.info("Running all tests in project");
                
                test = new XmlTest(suite);
                XmlPackage xmlPackage = new XmlPackage();
                xmlPackage.setName(".*");
                test.setPackages(Collections.singletonList(xmlPackage));
            }

            testng.run();
        } finally {
            if (configuration.getTimeToLive() == 0) {
                stop();
            }
        }
    }

    /**
     * Create new Citrus instance if not already set.
     */
    public Citrus createInstance(CitrusAppConfiguration configuration) {
        if (instance == null) {
            if (configuration.getConfigClass() != null) {
                instance = Citrus.newInstance(configuration.getConfigClass());
            } else {
                instance = Citrus.newInstance();
            }
        }

        return instance;
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

        if (instance != null && instance.getApplicationContext() instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) instance.getApplicationContext()).close();
        }
    }

    /**
     * Gets the value of the instance property.
     *
     * @return the instance
     */
    public Citrus getInstance() {
        return instance;
    }

    /**
     * Sets the instance property.
     *
     * @param instance
     */
    public void setInstance(Citrus instance) {
        this.instance = instance;
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
