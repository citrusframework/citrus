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

package com.consol.citrus.main;

import com.consol.citrus.TestClass;
import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class CitrusAppOptions {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusAppOptions.class);

    protected final List<CliOption<? extends CitrusAppConfiguration>> options = new ArrayList<>();

    protected CitrusAppOptions() {
        options.add(new CliOption<CitrusAppConfiguration>("h", "help", "Displays cli option usage") {
            @Override
            protected void doProcess(CitrusAppConfiguration configuration, String arg, String value, LinkedList<String> remainingArgs) {
                StringBuilder builder = new StringBuilder();
                builder.append(System.lineSeparator()).append("Citrus application option usage:").append(System.lineSeparator());
                for (CliOption option : options) {
                    builder.append(option.getInformation()).append(System.lineSeparator());
                }

                log.info(builder.toString());
                configuration.setTimeToLive(1000);
                configuration.setSkipTests(true);
            }
        });

        options.add(new CliOption<CitrusAppConfiguration>("d", "duration", "Maximum time in milliseconds the server should be up and running - server will terminate automatically when time exceeds") {
            @Override
            protected void doProcess(CitrusAppConfiguration configuration, String arg, String value, LinkedList<String> remainingArgs) {
                if (value != null && value.length() > 0) {
                    configuration.setTimeToLive(Long.valueOf(value));
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -d/--duration option");
                }
            }
        });

        options.add(new CliOption<CitrusAppConfiguration>("c", "config", "Custom Spring configuration class") {
            @Override
            protected void doProcess(CitrusAppConfiguration configuration, String arg, String value, LinkedList<String> remainingArgs) {
                if (StringUtils.hasText(value)) {
                    try {
                        configuration.setConfigClass((Class<? extends CitrusSpringConfig>) Class.forName(value));
                    } catch (ClassNotFoundException e) {
                        throw new CitrusRuntimeException("Unable to access config class type: " + value, e);
                    }
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -c/--config option");
                }
            }
        });

        options.add(new CliOption<CitrusAppConfiguration>("s", "skipTests", "Skip test execution") {
            @Override
            protected void doProcess(CitrusAppConfiguration configuration, String arg, String value, LinkedList<String> remainingArgs) {
                if (StringUtils.hasText(value)) {
                    configuration.setSkipTests(Boolean.valueOf(value));
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -s/--skipTests option");
                }
            }
        });

        options.add(new CliOption<CitrusAppConfiguration>("p", "package", "Test package to execute") {
            @Override
            protected void doProcess(CitrusAppConfiguration configuration, String arg, String value, LinkedList<String> remainingArgs) {
                if (StringUtils.hasText(value)) {
                    configuration.getPackages().add(value);
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -p/--package option");
                }
            }
        });

        options.add(new CliOption<CitrusAppConfiguration>("D", "properties", "Default system properties to set") {
            @Override
            protected void doProcess(CitrusAppConfiguration configuration, String arg, String value, LinkedList<String> remainingArgs) {
                if (StringUtils.hasText(value)) {
                    configuration.getDefaultProperties().putAll(StringUtils.commaDelimitedListToSet(value)
                                                                            .stream()
                                                                            .map(keyValue -> Optional.ofNullable(StringUtils.split(keyValue, "=")).orElse(new String[] {keyValue, ""}))
                                                                            .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1])));
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -D/--properties option");
                }
            }
        });

        options.add(new CliOption<CitrusAppConfiguration>("e", "exit", "Force system exit when finished") {
            @Override
            protected void doProcess(CitrusAppConfiguration configuration, String arg, String value, LinkedList<String> remainingArgs) {
                if (StringUtils.hasText(value)) {
                    configuration.setSystemExit(Boolean.valueOf(value));
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -e/--exit option");
                }
            }
        });

        options.add(new CliOption<CitrusAppConfiguration>("t", "test", "Test class/method to execute") {
            @Override
            protected void doProcess(CitrusAppConfiguration configuration, String arg, String value, LinkedList<String> remainingArgs) {
                if (StringUtils.hasText(value)) {

                    String className = value;
                    String methodName = null;
                    if (value.contains("#")) {
                        className = value.substring(0, value.indexOf("#"));
                        methodName = value.substring(value.indexOf("#") + 1);
                    }

                    TestClass testClass = new TestClass(className);
                    if (StringUtils.hasText(methodName)) {
                        testClass.setMethod(methodName);
                    }

                    configuration.getTestClasses().add(testClass);
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -t/--test option");
                }
            }
        });

        options.add(new CliOption<CitrusAppConfiguration>("j", "jar", "External test jar to load tests from") {
            @Override
            protected void doProcess(CitrusAppConfiguration configuration, String arg, String value, LinkedList<String> remainingArgs) {
                if (StringUtils.hasText(value)) {
                    configuration.setTestJar(new File(value));
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -j/--jar option");
                }
            }
        });
    }

    /**
     * Apply options based on given argument line.
     * @param arguments
     */
    public static CitrusAppConfiguration apply(String[] arguments) {
        return apply(new CitrusAppConfiguration(), arguments);
    }

    /**
     * Apply options based on given argument line.
     * @param configuration
     * @param arguments
     */
    public static <T extends CitrusAppConfiguration> T apply(T configuration, String[] arguments) {
        LinkedList<String> args = new LinkedList<>(Arrays.asList(arguments));

        CitrusAppOptions options = new CitrusAppOptions();
        while (!args.isEmpty()) {
            String arg = args.removeFirst();

            for (CliOption option : options.options) {
                if (option.processOption(configuration, arg, args)) {
                    break;
                }
            }
        }

        return configuration;
    }

    /**
     * Command line option represented with either short of full name.
     */
    public abstract class CliOption<T extends CitrusAppConfiguration> {
        private String shortName;
        private String fullName;
        private String description;

        protected CliOption(String shortName, String fullName, String description) {
            this.shortName = "-" + shortName;
            this.fullName = "--" + fullName;
            this.description = description;
        }

        public boolean processOption(T configuration, String arg, LinkedList<String> remainingArgs) {
            if (arg.equals(shortName) || fullName.startsWith(arg)) {
                if (remainingArgs.isEmpty()) {
                    doProcess(configuration, arg, null, remainingArgs);
                } else {
                    doProcess(configuration, arg, remainingArgs.removeFirst(), remainingArgs);
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

        protected abstract void doProcess(T configuration, String arg, String value, LinkedList<String> remainingArgs);
    }
}
