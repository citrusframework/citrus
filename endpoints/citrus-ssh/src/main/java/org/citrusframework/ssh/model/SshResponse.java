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

package org.citrusframework.ssh.model;

import jakarta.xml.bind.annotation.*;

/**
 * POJO encapsulate a SSH response. It is immutable.
 *
 * @author Roland Huss
 * @since 1.3
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "stdout",
        "stderr",
        "exit"
})
@XmlRootElement(name = "ssh-response")
public class SshResponse implements SshMessage {

    @XmlElement(required = true)
    protected String stdout;
    @XmlElement(required = true)
    protected String stderr;
    protected int exit;

    /**
     * Default constructor.
     */
    public SshResponse() {
    }

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
     * Gets the stderr.
     * @return the stderr the stderr to get.
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * Gets the exit.
     * @return the exit the exit to get.
     */
    public int getExit() {
        return exit;
    }
}
