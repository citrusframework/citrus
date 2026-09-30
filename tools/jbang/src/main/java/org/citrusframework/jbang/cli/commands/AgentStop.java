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

import java.util.ArrayList;
import java.util.Arrays;

import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.ParentCommand;
import org.citrusframework.jbang.cli.CitrusJBangMain;
import org.citrusframework.jbang.cli.Printer;
import org.citrusframework.jbang.cli.StringPrinter;

@CommandDefinition(name = "stop", description = "Stop the Citrus agent service", generateHelp = true)
public class AgentStop extends CitrusCommand {

    @ParentCommand
    private Agent parent;

    public AgentStop() {
        super(null);
    }

    public AgentStop(CitrusJBangMain main) {
        super(main);
    }

    @Override
    public CitrusJBangMain getMain() {
        if (super.getMain() == null && parent != null) {
            setMain(parent.getMain());
        }
        return super.getMain();
    }

    @Override
    public CommandResult execute(CommandInvocation invocation) throws org.aesh.command.CommandException, InterruptedException {
        try {
            return result(stop());
        } catch (Exception e) {
            printer().printErr("Failed to stop agent: %s".formatted(e.getMessage()));
            return CommandResult.FAILURE;
        }
    }

    private int stop() throws Exception {
        Printer original = getMain().getOut();
        try {
            StringPrinter printer = new StringPrinter();
            ListTests ls = new ListTests(getMain().withPrinter(printer));
            ls.call();

            Long pid = printer.getLines().stream()
                    .map(line -> new ArrayList<>(Arrays.asList(line.trim().split("\\s+"))))
                    .filter(rows -> "citrus-agent".equals(rows.get(1)))
                    .map(rows -> rows.get(0))
                    .map(Long::parseLong)
                    .findFirst()
                    .orElse(0L);

            if (pid > 0) {
                ProcessHandle.of(pid).ifPresent(ph -> {
                    if (ph.destroyForcibly()) {
                        original.printf("Stopped Citrus agent process (pid: %s)%n", pid);
                    } else {
                        original.printErr("Failed to stop Citrus agent process (pid: %s)".formatted(pid));
                    }
                });
            }
        } finally {
            getMain().withPrinter(original);
        }

        return 0;
    }

}
