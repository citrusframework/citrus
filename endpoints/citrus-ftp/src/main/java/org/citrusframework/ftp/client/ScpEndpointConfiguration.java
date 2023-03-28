/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.ftp.client;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class ScpEndpointConfiguration extends SftpEndpointConfiguration {

    private String portOption = "-P";

    private InputStream stdin;
    private PrintStream stdout;
    private PrintStream stderr;

    /**
     * Gets the stdin.
     *
     * @return
     */
    public InputStream getStdin() {
        return stdin;
    }

    /**
     * Sets the stdin.
     *
     * @param stdin
     */
    public void setStdin(InputStream stdin) {
        this.stdin = stdin;
    }

    /**
     * Gets the stdout.
     *
     * @return
     */
    public PrintStream getStdout() {
        return stdout;
    }

    /**
     * Sets the stdout.
     *
     * @param stdout
     */
    public void setStdout(PrintStream stdout) {
        this.stdout = stdout;
    }

    /**
     * Gets the stderr.
     *
     * @return
     */
    public PrintStream getStderr() {
        return stderr;
    }

    /**
     * Sets the stderr.
     *
     * @param stderr
     */
    public void setStderr(PrintStream stderr) {
        this.stderr = stderr;
    }

    /**
     * Gets the portOption.
     *
     * @return
     */
    public String getPortOption() {
        return portOption;
    }

    /**
     * Sets the portOption.
     *
     * @param portOption
     */
    public void setPortOption(String portOption) {
        this.portOption = portOption;
    }
}
