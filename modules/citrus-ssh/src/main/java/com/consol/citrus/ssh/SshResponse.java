package com.consol.citrus.ssh;

/**
 * POJO encapsulate a SSH response
 *
 * @author roland
 * @since 05.09.12
 */
public class SshResponse {

    private String stdout;
    private String stderr;
    private int exit;

    /**
     * Default constructor using fields.
     * @param pStdout
     * @param pStderr
     * @param pExit
     */
    public SshResponse(String pStdout, String pStderr, int pExit) {
        stdout = pStdout;
        stderr = pStderr;
        exit = pExit;
    }

    /**
     * Gets the stdout.
     * @return the stdout the stdout to get.
     */
    public String getStdout() {
        return stdout;
    }

    /**
     * Sets the stdout.
     * @param stdout the stdout to set
     */
    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    /**
     * Gets the stderr.
     * @return the stderr the stderr to get.
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * Sets the stderr.
     * @param stderr the stderr to set
     */
    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    /**
     * Gets the exit.
     * @return the exit the exit to get.
     */
    public int getExit() {
        return exit;
    }

    /**
     * Sets the exit.
     * @param exit the exit to set
     */
    public void setExit(int exit) {
        this.exit = exit;
    }

}
