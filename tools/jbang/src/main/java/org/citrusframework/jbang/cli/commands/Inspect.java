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
import org.aesh.command.option.Argument;
import org.aesh.command.option.ParentCommand;
import org.citrusframework.jbang.cli.CitrusJBangMain;
import org.citrusframework.jbang.cli.JsonSupport;
import org.citrusframework.jbang.cli.util.CodeAnalyzer;
import org.citrusframework.jbang.cli.util.DelegatingCodeAnalyzer;
import org.citrusframework.message.MessagePayloadUtils;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;

@CommandDefinition(name = "inspect", description = "Inspect a Citrus test and its source code in order to provide detailed information " +
        "such as used endpoints as well as required modules and dependencies.", generateHelp = true)
public class Inspect extends CitrusCommand {

    @Argument(description = "Path to the test file (or a github link)", paramLabel = "<file>", required = true)
    String file;

    @ParentCommand
    CitrusJBangMain parent;

    public Inspect() {
        super(null);
    }

    public Inspect(CitrusJBangMain main) {
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
            return result(call());
        } catch (Exception e) {
            printer().printErr("Failed to inspect test: %s".formatted(e.getMessage()));
            return CommandResult.FAILURE;
        }
    }

    int call() throws Exception {
        Resource sourceFile = Resources.create(file);
        CodeAnalyzer.ScanResult result = new DelegatingCodeAnalyzer().scan(sourceFile);

        printer().println(MessagePayloadUtils.prettyPrintJson(JsonSupport.json().writeValueAsString(result)));

        return 0;
    }
}
