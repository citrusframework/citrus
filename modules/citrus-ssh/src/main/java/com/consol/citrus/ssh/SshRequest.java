/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.ssh;

/**
 * POJO encapsulating an SSH request
 *
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
