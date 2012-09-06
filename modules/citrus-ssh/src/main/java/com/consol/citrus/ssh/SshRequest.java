package com.consol.citrus.ssh;

/**
 * POJO encapsulating an SSH request
 * @author roland
 * @since 05.09.12
 */
public class SshRequest {

    private String command;
    private String stdin;

    public SshRequest(String pCommand, String pInput) {
        command = pCommand;
        stdin = pInput;
    }

}
