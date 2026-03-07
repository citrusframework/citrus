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

import org.citrusframework.jbang.commands.Agent;
import org.citrusframework.jbang.commands.AgentStart;
import org.citrusframework.jbang.commands.AgentStop;
import org.citrusframework.jbang.commands.Complete;
import org.citrusframework.jbang.commands.Init;
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
    }
}
