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
 * @version $Id$
 * @since 2012.11.30
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

        commands.add(command);

        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.directory(workingDirectory);

        LOG.trace("Returning ProcessBuilder for command:" + commands);
        return pb;
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

