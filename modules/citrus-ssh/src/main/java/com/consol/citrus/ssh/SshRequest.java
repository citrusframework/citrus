package com.consol.citrus.ssh;

/**
 * POJO encapsulating an SSH request
 * @author roland
 * @since 05.09.12
 */
public class SshRequest {

    private String command;
    private String stdin;

    /**
     * Constructor using fields.
     * @param pCommand
     * @param pInput
     */
    public SshRequest(String pCommand, String pInput) {
        command = pCommand;
        stdin = pInput;
    }

    /**
     * Gets the command.
     * @return the command the command to get.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Sets the command.
     * @param command the command to set
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * Gets the stdin.
     * @return the stdin the stdin to get.
     */
    public String getStdin() {
        return stdin;
    }

    /**
     * Sets the stdin.
     * @param stdin the stdin to set
     */
    public void setStdin(String stdin) {
        this.stdin = stdin;
    }

}
