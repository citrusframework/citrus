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

package org.citrusframework.jbang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.awaitility.core.ConditionTimeoutException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.awaitility.Awaitility.await;

/**
 * Process wrapper also holds the output that has been produced by the completed process.
 */
public class ProcessAndOutput {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(ProcessAndOutput.class);

    private final Process process;
    private String output;

    private BufferedReader reader;

    ProcessAndOutput(Process process) {
        this(process, "");
    }

    ProcessAndOutput(Process process, String output) {
        this.process = process;
        this.output = output;
    }

    ProcessAndOutput(Process process, File outputFile) {
        this.process = process;
        try {
            this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(outputFile)));
        } catch (FileNotFoundException e) {
            throw new CitrusRuntimeException(String.format("Failed to access process output file %s", outputFile.getName()), e);
        }
    }

    public Process getProcess() {
        return process;
    }

    public String getOutput() {
        if (process.isAlive()) {
            readChunk();
        } else if (reader != null) {
            readAllAndClose();
        }

        return output;
    }

    /**
     * Reads process output until EOF and closes the stream reader.
     */
    private void readAllAndClose() {
        String line;
        StringBuilder builder = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }

            if (!builder.isEmpty()) {
                output += builder;
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to get JBang process output", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                LOG.debug("Failed to close JBang process output reader", e);
            } finally {
                reader = null;
            }
        }
    }

    /**
     * Reads a chunk of process output. Either reads maximum amount of lines or returns when reader is not ready (e.g.
     * no process output available). Chunk is added to the cached process output.
     */
    private void readChunk() {
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        }

        String line;
        int read = 1;
        int maxRead = 100;
        StringBuilder builder = new StringBuilder();
        try {
            while (read <= maxRead && reader.ready() && (line = reader.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
                read++;
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to get JBang process output", e);
        }

        if (!builder.isEmpty()) {
            if (output == null) {
                output = builder.toString();
            } else {
                output += builder.toString().stripTrailing();
            }
        }
    }

    /**
     * Get the process id of first descendant or the parent process itself in case there is no descendant process.
     * On Linux the shell command represents the parent process and the JBang command as descendant process.
     * Typically, we need the JBang command process id.
     * @return
     */
    public Long getProcessId(String app) {
        try {
            if (isUnix()) {
                // wait for descendant process to be available
                await().atMost(5000L, TimeUnit.MILLISECONDS)
                        .until(() -> process.descendants().findAny().isPresent());
                return process.descendants()
                        .filter(p -> p.info().commandLine().orElse("").contains(app))
                        .findFirst()
                        .map(ProcessHandle::pid)
                        .orElse(process.pid());
            }

            return process.pid();
        } catch (ConditionTimeoutException | UnsupportedOperationException | SecurityException e) {
            // not able or not allowed to manage descendant process snapshot
            // return parent process id as a fallback
            return process.pid();
        }
    }

    private static boolean isUnix() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("nix") || os.contains("nux") || os.contains("aix");
    }
}
