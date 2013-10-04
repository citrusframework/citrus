/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.admin.launcher.process;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for creating {@link ProcessBuilder}s. To be used for executing a shell command on unix or windows.
 *
 * @author Martin.Maher@consol.de
 * @since 1.3
 */
public class ExecuteCommand {
    private static final Logger LOG = LoggerFactory.getLogger(ExecuteCommand.class);

    private static final String BASH = "bash";
    private static final String BASH_OPTION_C = "-c";
    private static final String CMD = "cmd";
    private static final String CMD_OPTION_C = "/c";

    private String command;
    private File workingDirectory;

    /**
     * Constructor for executing a command.
     *
     * @param command the command to be executed
     * @param workingDirectory the working directory where the command is to be executed from
     */
    public ExecuteCommand(String command, File workingDirectory) {
        this.command = command;
        this.workingDirectory = workingDirectory;
    }

    public ProcessBuilder getProcessBuilder() {
        validateWorkingDirectory(workingDirectory);

        List<String> commands = new ArrayList<String>();
        if (SystemUtils.IS_OS_UNIX) {
            commands.add(BASH);
            commands.add(BASH_OPTION_C);
        } else {
            commands.add(CMD);
            commands.add(CMD_OPTION_C);
        }

        commands.add(buildCommand(command));

        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.directory(workingDirectory);

        LOG.trace("Returning ProcessBuilder for command:" + commands);
        return pb;
    }

    protected String buildCommand(String command) {
        return command;
    }

    private void validateWorkingDirectory(File workingDirectory) {
        if (workingDirectory == null) {
            throw new IllegalStateException("Working directory has not been set");
        }
        if (!workingDirectory.isDirectory()) {
            throw new IllegalStateException(String.format("Invalid working directory '%s'", workingDirectory.getAbsolutePath()));
        }
    }
}

