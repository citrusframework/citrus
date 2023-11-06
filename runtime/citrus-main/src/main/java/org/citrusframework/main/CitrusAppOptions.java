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

package org.citrusframework.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.citrusframework.TestClass;
import org.citrusframework.TestSource;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class CitrusAppOptions<T extends CitrusAppConfiguration> {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CitrusAppOptions.class);

    protected final List<CliOption<T>> options = new ArrayList<>();

    protected CitrusAppOptions() {
        options.add(new CliOption<>("h", "help", "Displays cli option usage") {
            @Override
            protected void doProcess(T configuration, String arg, String value, LinkedList<String> remainingArgs) {
                StringBuilder builder = new StringBuilder();
                builder.append(System.lineSeparator()).append("Citrus application option usage:").append(System.lineSeparator());
                for (CliOption<?> option : options) {
                    builder.append(option.getInformation()).append(System.lineSeparator());
                }

                logger.info(builder.toString());
                configuration.setTimeToLive(1000);
                configuration.setSkipTests(true);
            }
        });

        options.add(new CliOption<>("d", "duration", "Maximum time in milliseconds the server should be up and running - server will terminate automatically when time exceeds") {
            @Override
            protected void doProcess(T configuration, String arg, String value, LinkedList<String> remainingArgs) {
                if (value != null && value.length() > 0) {
                    configuration.setTimeToLive(Long.parseLong(value));
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -d/--duration option");
                }
            }
        });

        options.add(new CliOption<>("c", "config", "Custom configuration class") {
            @Override
            protected void doProcess(T configuration, String arg, String value, LinkedList<String> remainingArgs) {
                if (StringUtils.hasText(value)) {
                    try {
                        Class.forName(value);
                    } catch (ClassNotFoundException e) {
                        throw new CitrusRuntimeException("Unable to access config class type: " + value, e);
                    }
                    configuration.setConfigClass(value);
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -c/--config option");
                }
            }
        });

        options.add(new CliOption<>("s", "skipTests", "Skip test execution") {
            @Override
            protected void doProcess(T configuration, String arg, String value, LinkedList<String> remainingArgs) {
                if (StringUtils.hasText(value)) {
                    configuration.setSkipTests(Boolean.parseBoolean(value));
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -s/--skipTests option");
                }
            }
        });

        options.add(new CliOption<>("p", "package", "Test package to execute") {
            @Override
            protected void doProcess(T configuration, String arg, String value, LinkedList<String> remainingArgs) {
                if (StringUtils.hasText(value)) {
                    configuration.getPackages().add(value);
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -p/--package option");
                }
            }
        });

        options.add(new CliOption<>("D", "properties", "Default system properties to set") {
            @Override
            protected void doProcess(T configuration, String arg, String value, LinkedList<String> remainingArgs) {
                if (StringUtils.hasText(value)) {
                    configuration.getDefaultProperties().putAll(Arrays.stream(value.split(","))
                            .map(keyValue -> keyValue.split("="))
                            .filter(keyValue -> StringUtils.hasText(keyValue[0]))
                            .map(keyValue -> {
                                if (keyValue.length < 2) {
                                    return new String[]{keyValue[0], ""};
                                }
                                return keyValue;
                            })
                            .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1])));
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -D/--properties option");
                }
            }
        });

        options.add(new CliOption<>("", "exit", "Force system exit when finished") {
            @Override
            protected void doProcess(T configuration, String arg, String value, LinkedList<String> remainingArgs) {
                if (StringUtils.hasText(value)) {
                    configuration.setSystemExit(Boolean.parseBoolean(value));
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -e/--exit option");
                }
            }
        });

        options.add(new CliOption<>("e", "engine", "Set test engine name used to run the tests") {
            @Override
            protected void doProcess(T configuration, String arg, String value, LinkedList<String> remainingArgs) {
                if (StringUtils.hasText(value)) {
                    configuration.setEngine(value);
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -e/--exit option");
                }
            }
        });

        options.add(new CliOption<>("t", "test", "Test class/method to execute") {
            @Override
            protected void doProcess(T configuration, String arg, String value, LinkedList<String> remainingArgs) {
                if (StringUtils.hasText(value)) {

                    TestSource source;
                    if (FileUtils.getFileExtension(value).isEmpty()) {
                        // no file extension assume it is a Java class name
                        source = TestClass.fromString(value);
                    } else {
                        source = FileUtils.getTestSource(value);
                    }

                    configuration.getTestSources().add(source);
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -t/--test option");
                }
            }
        });

        options.add(new CliOption<>("j", "jar", "External test jar to load tests from") {
            @Override
            protected void doProcess(T configuration, String arg, String value, LinkedList<String> remainingArgs) {
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
    public T apply(String[] arguments) {
        return apply((T) new CitrusAppConfiguration(), arguments);
    }

    /**
     * Apply options based on given argument line.
     * @param configuration
     * @param arguments
     */
    public T apply(T configuration, String[] arguments) {
        LinkedList<String> args = new LinkedList<>(Arrays.asList(arguments));

        CitrusAppOptions<T> options = new CitrusAppOptions<>();
        while (!args.isEmpty()) {
            String arg = args.removeFirst();

            for (CliOption<T> option : options.options) {
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
    public abstract static class CliOption<T extends CitrusAppConfiguration> {
        private final String shortName;
        private final String fullName;
        private final String description;

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
            if (getShortName().equals("-")) {
                return "  " + getFullName() + " = " + getDescription();
            } else {
                return "  " + getShortName() + " or " + getFullName() + " = " + getDescription();
            }
        }

        protected abstract void doProcess(T configuration, String arg, String value, LinkedList<String> remainingArgs);
    }
}
