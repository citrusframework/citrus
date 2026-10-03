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

import java.io.File;

import org.aesh.command.Command;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;
import org.citrusframework.jbang.cli.CitrusJBangMain;
import org.citrusframework.jbang.cli.Printer;

public abstract class CitrusCommand implements Command<CommandInvocation> {

    private CitrusJBangMain main;
    private File userDir;

    protected CitrusCommand() {
    }

    public CitrusCommand(CitrusJBangMain main) {
        this.main = main;
    }

    public CitrusJBangMain getMain() {
        return main;
    }

    protected void setMain(CitrusJBangMain main) {
        this.main = main;
    }

    public File getStatusFile(String pid) {
        return new File(getUserDir(), pid + "-status.json");
    }

    public File getOutputFile(String pid) {
        return new File(getUserDir(), pid + "-output.json");
    }

    protected File getUserDir() {
        if (userDir == null) {
            userDir = new File(System.getProperty("user.home"), ".citrus");
        }

        return userDir;
    }

    protected Printer printer() {
        return getMain().getOut();
    }

    protected CommandResult result(int exitCode) {
        return exitCode == 0 ? CommandResult.SUCCESS : CommandResult.FAILURE;
    }
}
