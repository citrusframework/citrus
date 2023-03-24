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

package org.citrusframework.generate;

import org.citrusframework.CitrusSettings;
import org.citrusframework.generate.xml.XmlTestGenerator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class TestGeneratorMain {

    /**
     * Main CLI method.
     * @param args
     */
    public static void main(String[] args) {
        Options options = new TestGeneratorCliOptions();

        try {
            CommandLineParser cliParser = new GnuParser();
            CommandLine cmd = cliParser.parse(options, args);

            if (cmd.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("CITRUS test creation", options);
                return;
            }

            XmlTestGenerator generator = (XmlTestGenerator) new XmlTestGenerator()
                    .withName(cmd.getOptionValue("name"))
                    .withAuthor(cmd.getOptionValue("author", "Unknown"))
                    .withDescription(cmd.getOptionValue("description", "TODO: Description"))
                    .usePackage(cmd.getOptionValue("package", "org.citrusframework"))
                    .useSrcDirectory(cmd.getOptionValue("srcdir", CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY))
                    .withFramework(UnitFramework.fromString(cmd.getOptionValue("framework", "testng")));

            generator.create();
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("\n **** CITRUS TEST GENERATOR ****", "\n CLI options:", options, "");
        }
    }

    /**
     * CLI options for test creation
     */
    private static class TestGeneratorCliOptions extends Options {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("static-access")
        public TestGeneratorCliOptions() {
            this.addOption(new Option("help", "print usage help"));

            this.addOption(OptionBuilder.withArgName("name")
                    .hasArg()
                    .withDescription("the test name (required)")
                    .isRequired(true)
                    .create("name"));

            this.addOption(OptionBuilder.withArgName("author")
                    .hasArg()
                    .withDescription("the author of the test (optional)")
                    .isRequired(false)
                    .create("author"));

            this.addOption(OptionBuilder.withArgName("description")
                    .hasArg()
                    .withDescription("describes the test (optional)")
                    .isRequired(false)
                    .create("description"));

            this.addOption(OptionBuilder.withArgName("package")
                    .hasArg()
                    .withDescription("the package to use (optional)")
                    .isRequired(false)
                    .create("package"));

            this.addOption(OptionBuilder.withArgName("srcdir")
                    .hasArg()
                    .withDescription("the test source directory to use (optional)")
                    .isRequired(false)
                    .create("srcdir"));

            this.addOption(OptionBuilder.withArgName("framework")
                    .hasArg()
                    .withDescription("the framework to use (optional) [testng, junit4, junit3]")
                    .isRequired(false)
                    .create("framework"));
        }
    }
}
