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
import com.consol.citrus.junit.JUnit4TestEngine;
import com.consol.citrus.testng.TestNGEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import java.util.Map;
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

            setDefaultProperties(citrusApp.configuration);
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

        log.info(String.format("Running Citrus %s", Citrus.getVersion()));
        setDefaultProperties(configuration);

        if (ClassUtils.isPresent("org.testng.annotations.Test", getClass().getClassLoader())) {
            new TestNGEngine(configuration).run();
        } else if (ClassUtils.isPresent("org.junit.Test", getClass().getClassLoader())) {
            new JUnit4TestEngine(configuration).run();
        }
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
     * Reads default properties in configuration and sets them as system properties.
     * @param configuration
     */
    private static void setDefaultProperties(CitrusAppConfiguration configuration) {
        for (Map.Entry<String, String> entry : configuration.getDefaultProperties().entrySet()) {
            log.debug(String.format("Setting application property %s=%s", entry.getKey(), entry.getValue()));
            System.setProperty(entry.getKey(), Optional.ofNullable(entry.getValue()).orElse(""));
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
