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
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main command line application callable via static run methods and command line main method invocation.
 * Command line options are passed to the application for optional arguments. Application will run until the
 * duration time option has passed by or until the JVM terminates.
 *
 * @author Christoph Deppisch
 * @since 2.5
 */
public class CitrusApplication {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusApplication.class);

    /** Citrus framework instance */
    private Citrus instance;

    /** Optional custom configuration class for Spring application context */
    private Class configClass;

    protected long duration = -1;
    protected final AtomicBoolean completed = new AtomicBoolean(false);

    protected final List<CliOption> options = new ArrayList<>();

    /** Count down latch */
    protected final CountDownLatch latch = new CountDownLatch(1);

    /**
     * Citrus application with given Citrus instance.
     */
    public CitrusApplication() {
        addOption(new CliOption("h", "help", "Displays cli option usage") {
            @Override
            protected void doProcess(String arg, String value, LinkedList<String> remainingArgs) {
                StringBuilder builder = new StringBuilder();
                builder.append(System.lineSeparator() + "Citrus application option usage:" + System.lineSeparator());
                for (CliOption option : options) {
                    builder.append(option.getInformation() + System.lineSeparator());
                }

                log.info(builder.toString());
                complete();
            }
        });

        addOption(new CliOption("d", "duration", "Maximum time in milliseconds the application should be up and running - application will terminate automatically when time has passed by") {
            @Override
            protected void doProcess(String arg, String value, LinkedList<String> remainingArgs) {
                if (StringUtils.hasText(value)) {
                    setDuration(Long.valueOf(value));
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -d/-duration option");
                }
            }
        });

        addOption(new CliOption("c", "config", "Custom Spring configuration class") {
            @Override
            protected void doProcess(String arg, String value, LinkedList<String> remainingArgs) {
                if (StringUtils.hasText(value)) {
                    try {
                        setConfigClass(Class.forName(value));
                    } catch (ClassNotFoundException e) {
                        throw new CitrusRuntimeException("Unable to access config class type: " + value, e);
                    }
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -c/-config option");
                }
            }
        });
    }

    /**
     * Main method with command line arguments.
     * @param args
     */
    public static void main(String[] args) {
        CitrusApplication.run(args);
    }

    /**
     * Run application with given command line arguments.
     * @param args
     */
    public static void run(String ... args) {
        new CitrusApplication().doRun(args);
    }

    /**
     * Run application with prepared Citrus instance.
     * @param citrus
     * @param args
     */
    public static void run(Citrus citrus, String ... args) {
        CitrusApplication app = new CitrusApplication();
        app.setInstance(citrus);
        app.doRun(args);
    }

    /**
     * Wait for application completion or JVM to terminate.
     */
    protected void doRun(String[] args) {
        parseArgs(args);

        if (!isCompleted()) {
            createInstance();

            try {
                if (duration > 0) {
                    latch.await(duration, TimeUnit.MILLISECONDS);
                    completed.set(true);
                } else {
                    latch.await();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Create new Citrus instance if not already set.
     */
    private void createInstance() {
        if (instance == null) {
            if (configClass != null) {
                instance = Citrus.newInstance(configClass);
            } else {
                instance = Citrus.newInstance();
            }
        }
    }

    /**
     * Completes this application.
     */
    public void complete() {
        completed.set(true);
        latch.countDown();
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
     * Parses the command line arguments.
     * @param arguments
     */
    public void parseArgs(String[] arguments) {
        LinkedList<String> args = new LinkedList<String>(Arrays.asList(arguments));

        while (!args.isEmpty()) {
            String arg = args.removeFirst();

            for (CliOption option : options) {
                if (option.processOption(arg, args)) {
                    break;
                }
            }
        }
    }

    public void addOption(CliOption option) {
        options.add(option);
    }

    /**
     * Gets the value of the configClass property.
     *
     * @return the configClass
     */
    public Class getConfigClass() {
        return configClass;
    }

    /**
     * Sets the configClass property.
     *
     * @param configClass
     */
    public void setConfigClass(Class configClass) {
        this.configClass = configClass;
    }

    /**
     * Gets the value of the duration property.
     *
     * @return the duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Sets the duration property.
     *
     * @param duration
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * Gets the value of the completed property.
     *
     * @return the completed
     */
    public boolean isCompleted() {
        return completed.get();
    }

    /**
     * Command line option represented with either short of full name.
     */
    public abstract class CliOption {
        private String shortName;
        private String fullName;
        private String description;

        protected CliOption(String shortName, String fullName, String description) {
            this.shortName = "-" + shortName;
            this.fullName = "-" + fullName;
            this.description = description;
        }

        public boolean processOption(String arg, LinkedList<String> remainingArgs) {
            if (arg.equalsIgnoreCase(shortName) || fullName.startsWith(arg)) {
                if (remainingArgs.isEmpty()) {
                    doProcess(arg, null, remainingArgs);
                } else {
                    doProcess(arg, remainingArgs.removeFirst(), remainingArgs);
                }
                return true;
            }
            return false;
        }

        public String getShortName() {
            return shortName;
        }

        public String getDescription() {
            return description;
        }

        public String getFullName() {
            return fullName;
        }

        public String getInformation() {
            return "  " + getShortName() + " or " + getFullName() + " = " + getDescription();
        }

        protected abstract void doProcess(String arg, String value, LinkedList<String> remainingArgs);
    }
}
