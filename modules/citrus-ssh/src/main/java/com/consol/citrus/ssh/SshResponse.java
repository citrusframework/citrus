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

    public SshResponse(String pStdout, String pStderr, int pExit) {
        stdout = pStdout;
        stderr = pStderr;
        exit = pExit;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public int getExit() {
        return exit;
    }
}
