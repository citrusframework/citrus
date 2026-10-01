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

package org.citrusframework.jbang.cli.commands;

import java.nio.file.Path;

import org.citrusframework.jbang.cli.CitrusJBangMain;
import org.citrusframework.spi.Resources;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * CLI parity: command tree, help flags and exit codes after the CLI framework migration.
 */
public class CommandParityTest extends CommandTest {

    @Test
    public void shouldPrintRootHelp() {
        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("--help");
        main.execute("-h");
    }

    @Test
    public void shouldPrintSubcommandHelp() {
        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("init", "--help");
        main.execute("inspect", "--help");
        main.execute("run", "--help");
        main.execute("ls", "--help");
        main.execute("agent", "--help");
        main.execute("agent", "start", "--help");
        main.execute("agent", "run", "--help");
        main.execute("agent", "stop", "--help");
    }

    @Test
    public void shouldFailOnUnknownCommand() {
        CitrusJBangMain main = createCitrusJBangMain(2);
        main.execute("bogus-command");
    }

    @Test
    public void shouldFailOnUnknownAgentSubcommand() {
        CitrusJBangMain main = createCitrusJBangMain(2);
        main.execute("agent", "bogus");
    }

    @Test
    public void shouldFailOnMissingRequiredArgument() {
        CitrusJBangMain main = createCitrusJBangMain(2);
        main.execute("init");
    }

    @Test
    public void shouldFailOnUnknownOption() {
        CitrusJBangMain main = createCitrusJBangMain(2);
        main.execute("run", "--unknown-flag");
    }

    @Test
    public void shouldGenerateCompletionScript() {
        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("completion");

        Assert.assertFalse(printer.getOutput().isBlank(), "completion script expected on printer");
        Assert.assertTrue(printer.getOutput().contains("citrus"), "completion script expected to mention citrus");
    }

    @Test
    public void shouldListWithSortOption() {
        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("ls", "--sort", "name");

        Assert.assertTrue(printer.getOutput().contains("PID"), "ls table header expected");
    }

    @Test
    public void shouldPrintVersion() {
        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("--version");

        Assert.assertFalse(printer.getOutput().isBlank(), "version output expected");

        main.execute("-V");

        Assert.assertFalse(printer.getOutput().isBlank(), "version output expected");
    }

    @Test
    public void shouldAcceptMultiValueOptions() {
        Path source = Resources.fromClasspath("runnable/yaml-sample.citrus.it.yaml").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("run", source.toString(), "--dep", "foo:bar:1.0", "--dependency", "baz:qux:2.0",
                "--property", "parityA=1,parityB=2", "--offline");
    }

    @Test
    public void shouldAcceptExplicitBooleanValues() {
        Path source = Resources.fromClasspath("runnable/yaml-sample.citrus.it.yaml").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("run", source.toString(), "--logging", "false", "--inspect-code", "false", "--offline");
    }

    @Test
    public void shouldAcceptBareToggleFlags() {
        Path source = Resources.fromClasspath("runnable/yaml-sample.citrus.it.yaml").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("run", source.toString(), "--offline");
    }
}
