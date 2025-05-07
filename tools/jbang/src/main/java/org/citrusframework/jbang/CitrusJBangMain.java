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

        commandLine.getCommandSpec().versionProvider(() -> new String[] { "4.7.0-SNAPSHOT" });

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
}
