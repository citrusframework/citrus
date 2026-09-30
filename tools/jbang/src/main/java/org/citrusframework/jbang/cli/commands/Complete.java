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

import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.ParentCommand;
import org.aesh.util.completer.ShellCompletionGenerator;
import org.citrusframework.jbang.cli.CitrusJBangMain;

@CommandDefinition(name = "completion", aliases = {"complete"}, description = "Generate completion script for bash/zsh", generateHelp = true)
public class Complete extends CitrusCommand {

    @ParentCommand
    CitrusJBangMain parent;

    public Complete() {
        super(null);
    }

    public Complete(CitrusJBangMain main) {
        super(main);
    }

    @Override
    public CitrusJBangMain getMain() {
        if (super.getMain() == null) {
            setMain(parent);
        }
        return super.getMain();
    }

    @Override
    public CommandResult execute(CommandInvocation invocation) throws org.aesh.command.CommandException, InterruptedException {
        try {
            String script = ShellCompletionGenerator.generate(
                    ShellCompletionGenerator.ShellType.BASH, CitrusJBangMain.class, "citrus");

            // not PrintWriter.println: scripts with Windows line separators fail in strange ways!
            printer().print(script);
            printer().print("\n");
            return CommandResult.SUCCESS;
        } catch (Exception e) {
            printer().printErr("Failed to generate completion script: %s".formatted(e.getMessage()));
            return CommandResult.FAILURE;
        }
    }
}
