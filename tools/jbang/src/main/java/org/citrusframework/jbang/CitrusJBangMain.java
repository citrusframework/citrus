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

package org.citrusframework.jbang;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import org.citrusframework.CitrusSettings;
import org.citrusframework.jbang.commands.Agent;
import org.citrusframework.jbang.commands.AgentStart;
import org.citrusframework.jbang.commands.AgentStop;
import org.citrusframework.jbang.commands.Complete;
import org.citrusframework.jbang.commands.Init;
import org.citrusframework.jbang.commands.Inspect;
import org.citrusframework.jbang.commands.ListTests;
import org.citrusframework.jbang.commands.Run;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "citrus", description = "Citrus JBang CLI", mixinStandardHelpOptions = true)
public class CitrusJBangMain implements Callable<Integer> {
    private static CommandLine commandLine;

    private Printer out = new Printer.SystemOutPrinter();

    public static void run(String... args) {
        CitrusJBangMain main = new CitrusJBangMain();
        commandLine = new CommandLine(main)
                .addSubcommand("init", new CommandLine(new Init(main)))
                .addSubcommand("inspect", new CommandLine(new Inspect(main)))
                .addSubcommand("run", new CommandLine(new Run(main)))
                .addSubcommand("ls", new CommandLine(new ListTests(main)))
                .addSubcommand("agent", new CommandLine(new Agent(main))
                        .addSubcommand("start", new CommandLine(new AgentStart(main)))
                        .addSubcommand("stop", new CommandLine(new AgentStop(main))))
                .addSubcommand("completion", new CommandLine(new Complete(main)));

        commandLine.getCommandSpec().versionProvider(() -> new String[] { "4.10.0-SNAPSHOT" });

        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        commandLine.execute("--help");
        return 0;
    }

    /**
     * Uses this printer for writing command output.
     *
     * @param out to use with this main.
     */
    public CitrusJBangMain withPrinter(Printer out) {
        this.out = out;
        return this;
    }

    public Printer getOut() {
        return out;
    }

    public static class Settings {
        private static final String JBANG_PROPERTY_PREFIX = "citrus.jbang.";
        private static final String JBANG_ENV_PREFIX = "CITRUS_JBANG_";

        private static final String JBANG_TEST_SOURCE_FILE_EXT_PROPERTY = JBANG_PROPERTY_PREFIX + "test.source.file.ext";
        private static final String JBANG_TEST_SOURCE_FILE_EXT_ENV = JBANG_ENV_PREFIX + "TEST_SOURCE_FILE_EXT";
        private static final String[] JBANG_TEST_SOURCE_FILE_EXT_DEFAULT = new String[] {
                ".citrus.groovy", ".citrus.test.groovy", ".citrus.it.groovy", ".citrus-test.groovy", ".citrus-it.groovy",
                ".citrus.yaml", ".citrus.test.yaml", ".citrus.it.yaml", ".citrus-test.yaml", ".citrus-it.yaml",
                ".citrus.xml", ".citrus.test.xml", ".citrus.it.xml", ".citrus-test.xml", ".citrus-it.xml",
                ".feature", ".citrus.feature",
                "Test.java", "IT.java", "TestCase.java", "ITCase.java",
                "Test.xml", "IT.xml"
        };

        private static final String WORK_DIR_PROPERTY = JBANG_PROPERTY_PREFIX + "work.dir";
        private static final String WORK_DIR_ENV = JBANG_ENV_PREFIX + "WORK_DIR";
        private static final String WORK_DIR_DEFAULT = ".citrus-jbang"; // must be in sync with JBangSettings in citrus-jbang-connector module

        private static final String CLIPBOARD_GENERATED_FILE_PROPERTY = JBANG_PROPERTY_PREFIX + "clipboard.generated.file";
        private static final String CLIPBOARD_GENERATED_FILE_ENV = JBANG_ENV_PREFIX + "CLIPBOARD_GENERATED_FILE";
        private static final String CLIPBOARD_GENERATED_FILE_DEFAULT = getWorkDir() + "/generated-clipboard";

        private static final String REPORT_DIRECTORY_PROPERTY = JBANG_PROPERTY_PREFIX + "report.directory";
        private static final String REPORT_DIRECTORY_ENV = JBANG_ENV_PREFIX + "REPORT_DIRECTORY";
        private static final String REPORT_DIRECTORY_DEFAULT = getWorkDir() + "/citrus-reports";

        private static final String CAMEL_VERSION_PROPERTY = "citrus.camel.jbang.version";
        private static final String CAMEL_VERSION_ENV = "CITRUS_CAMEL_JBANG_VERSION";
        private static final String CAMEL_VERSION_DEFAULT = "4.18.0";

        private static final String MODULES_PROPERTY = JBANG_PROPERTY_PREFIX + "modules";
        private static final String MODULES_ENV = JBANG_ENV_PREFIX + "MODULES";

        private static final String DEPENDENCIES_PROPERTY = JBANG_PROPERTY_PREFIX + "dependencies";
        private static final String DEPENDENCIES_ENV = JBANG_ENV_PREFIX + "DEPENDENCIES";

        private static final Pattern PACKAGE_PATTERN = Pattern.compile(
                "^\\s*package\\s+([a-zA-Z][\\.\\w]*)\\s*;.*$", Pattern.MULTILINE);

        private static final Pattern CLASS_PATTERN = Pattern.compile(
                "^\\s*public class\\s+([a-zA-Z0-9]*)[\\s+|;].*$", Pattern.MULTILINE);

        private Settings() {
            // prevent instantiation of utility class
        }

        /**
         * Gets the list of accepted file extensions for Citrus test source files.
         * Values may be set as a comma-delimited String of supported file name patterns.
         */
        public static String[] getTestSourceFileExt() {
            return Optional.ofNullable(System.getProperty(JBANG_TEST_SOURCE_FILE_EXT_PROPERTY, System.getenv(JBANG_TEST_SOURCE_FILE_EXT_ENV)))
                    .map(value -> value.replaceAll("\\s", "").split(","))
                    .orElse(JBANG_TEST_SOURCE_FILE_EXT_DEFAULT);
        }

        /**
         * Gets the current working directory for the JBang command.
         * File resources are read from this directory.
         */
        public static String getWorkDir() {
            return CitrusSettings.getPropertyEnvOrDefault(WORK_DIR_PROPERTY, WORK_DIR_ENV, WORK_DIR_DEFAULT);
        }

        /**
         * Test sources generated from clipboard are stored temporarily to this directory.
         */
        public static String getClipboardGeneratedFile() {
            return CitrusSettings.getPropertyEnvOrDefault(CLIPBOARD_GENERATED_FILE_PROPERTY, CLIPBOARD_GENERATED_FILE_ENV, CLIPBOARD_GENERATED_FILE_DEFAULT);
        }

        /**
         * The directory where Citrus generates test reports for the Citrus JBang test execution.
         */
        public static String getReportDirectory() {
            return CitrusSettings.getPropertyEnvOrDefault(REPORT_DIRECTORY_PROPERTY, REPORT_DIRECTORY_ENV, REPORT_DIRECTORY_DEFAULT);
        }

        /**
         * Gets Citrus modules that should be loaded as additional dependencies and added to the classpath.
         */
        public static String[] getModules() {
            return CitrusSettings.getPropertyEnvOrDefault(MODULES_PROPERTY, MODULES_ENV, "").split(",");
        }

        /**
         * Gets additional dependencies in the form of Maven GAVs that should be added to the classpath.
         */
        public static String[] getDependencies() {
            return CitrusSettings.getPropertyEnvOrDefault(DEPENDENCIES_PROPERTY, DEPENDENCIES_ENV, "").split(",");
        }

        public static Pattern getClassPattern() {
            return CLASS_PATTERN;
        }

        public static Pattern getPackagePattern() {
            return PACKAGE_PATTERN;
        }

        public static String getCamelVersion() {
            return CitrusSettings.getPropertyEnvOrDefault(CAMEL_VERSION_PROPERTY, CAMEL_VERSION_ENV,
                    CitrusSettings.getPropertyEnvOrDefault("camel.jbang.version", "CAMEL_JBANG_VERSION", CAMEL_VERSION_DEFAULT));
        }
    }
}
