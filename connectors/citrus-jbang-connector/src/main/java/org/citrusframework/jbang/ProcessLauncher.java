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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstraction for launching CLI commands via a spawned process. Implementations decide how to
 * construct the underlying command (JBang, Camel Launcher jar, etc.) while sharing common
 * process execution logic provided by the static helper methods on this interface.
 */
public interface ProcessLauncher {

    boolean IS_OS_CYGWIN = Optional.ofNullable(System.getenv("OSTYPE"))
            .map(String::toLowerCase)
            .orElse("")
            .equals("cygwin");
    boolean IS_OS_WINDOWS = System.getProperty("os.name")
            .toLowerCase(Locale.ENGLISH)
            .contains("win");

    ProcessAndOutput run(String command, String... args);

    ProcessAndOutput run(String command, List<String> args);

    ProcessAndOutput runAsync(String command, String... args);

    ProcessAndOutput runAsync(String command, List<String> args);

    ProcessAndOutput runAsync(String command, File output, String... args);

    ProcessAndOutput runAsync(String command, File output, List<String> args);

    ProcessLauncher withSystemProperty(String name, String value);

    ProcessLauncher withSystemProperties(Map<String, String> systemProperties);

    ProcessLauncher withEnv(String name, String value);

    ProcessLauncher withEnvs(Map<String, String> envVars);

    ProcessLauncher workingDir(Path workingDir);

    Path getWorkingDir();

    /**
     * Execute a command synchronously. Waits for the process to complete and returns the process
     * instance so the caller can access the exit code and output.
     */
    default ProcessAndOutput execute(List<String> command, Path workingDir,
                                    Map<String, String> envVars, ProcessOutputListener outputListener) {
        Logger logger = LoggerFactory.getLogger(ProcessLauncher.class);
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Executing command: {}", String.join(" ", command));
            }

            ProcessBuilder pBuilder = new ProcessBuilder(command)
                    .redirectErrorStream(true);

            if (envVars != null) {
                pBuilder.environment().putAll(envVars);
            }

            if (workingDir != null) {
                pBuilder.directory(workingDir.toFile());
            }

            Process p = pBuilder.start();
            ProcessAndOutput pao = new ProcessAndOutput(p, outputListener);
            pao.waitFor();

            if (logger.isDebugEnabled() && p.exitValue() != 0) {
                logger.debug("Command failed: {}", String.join(" ", command));
                logger.debug(pao.getOutput());
            }

            return pao;
        } catch (IOException | InterruptedException e) {
            throw new CitrusRuntimeException("Error while executing command", e);
        }
    }

    /**
     * Execute a command asynchronously. Does not wait for the process to complete.
     */
    default ProcessAndOutput executeAsync(List<String> command, Path workingDir,
                                         Map<String, String> envVars, ProcessOutputListener outputListener) {
        Logger logger = LoggerFactory.getLogger(ProcessLauncher.class);
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Executing command: {}", String.join(" ", command));
            }

            ProcessBuilder pBuilder = new ProcessBuilder(command)
                    .redirectErrorStream(true);

            if (envVars != null) {
                pBuilder.environment().putAll(envVars);
            }

            if (workingDir != null) {
                pBuilder.directory(workingDir.toFile());
            }

            Process p = pBuilder.start();
            return new ProcessAndOutput(p, outputListener);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Error while executing command", e);
        }
    }

    /**
     * Execute a command asynchronously with output redirected to a file.
     * Does not wait for the process to complete.
     */
    default ProcessAndOutput executeAsync(List<String> command, Path workingDir, File outputFile,
                                         Map<String, String> envVars, ProcessOutputListener outputListener) {
        Logger logger = LoggerFactory.getLogger(ProcessLauncher.class);
        try {
            if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
                throw new CitrusRuntimeException("Unable to create process output directory: " + outputFile.getParent());
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Executing command: {}", String.join(" ", command));
            }

            ProcessBuilder pBuilder = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .redirectOutput(outputFile);

            if (envVars != null) {
                pBuilder.environment().putAll(envVars);
            }

            if (workingDir != null) {
                pBuilder.directory(workingDir.toFile());
            }

            Process p = pBuilder.start();
            return new ProcessAndOutput(p, outputFile, outputListener);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Error while executing command", e);
        }
    }
}
