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

package org.citrusframework.camel.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CamelCliLauncher implementation that executes Camel CLI commands using the Camel Launcher jar.
 * Commands are constructed as: {@code java [-Dsysprop=val]... -jar <path-to-jar> <command> <args>}
 */
public class LauncherJarCamelLauncher implements CamelCliLauncher {

    private static final Logger LOG = LoggerFactory.getLogger(LauncherJarCamelLauncher.class);

    private static final boolean IS_OS_CYGWIN = Optional.ofNullable(System.getenv("OSTYPE"))
            .map(String::toLowerCase)
            .orElse("")
            .equals("cygwin");
    private static final boolean IS_OS_WINDOWS = System.getProperty("os.name")
            .toLowerCase(Locale.ENGLISH)
            .contains("win");

    private static final int OK_EXIT_CODE = 0;

    private final String jarPath;

    private final Map<String, String> systemProperties = new HashMap<>();
    private final Map<String, String> envVars = new HashMap<>();

    private Path workingDir;

    public LauncherJarCamelLauncher(String jarPath) {
        this.jarPath = jarPath;
    }

    @Override
    public ProcessAndOutput run(String command, String... args) {
        return run(command, Arrays.asList(args));
    }

    @Override
    public ProcessAndOutput run(String command, List<String> args) {
        return execute(buildCommand(command, args), workingDir, envVars);
    }

    @Override
    public ProcessAndOutput runAsync(String command, String... args) {
        return runAsync(command, Arrays.asList(args));
    }

    @Override
    public ProcessAndOutput runAsync(String command, List<String> args) {
        return executeAsync(buildCommand(command, args), workingDir, null, envVars);
    }

    @Override
    public ProcessAndOutput runAsync(String command, File output, String... args) {
        return runAsync(command, output, Arrays.asList(args));
    }

    @Override
    public ProcessAndOutput runAsync(String command, File output, List<String> args) {
        return executeAsync(buildCommand(command, args), workingDir, output, envVars);
    }

    @Override
    public CamelCliLauncher withSystemProperty(String name, String value) {
        this.systemProperties.put(name, value);
        return this;
    }

    @Override
    public CamelCliLauncher withSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties.putAll(systemProperties);
        return this;
    }

    @Override
    public CamelCliLauncher withEnv(String name, String value) {
        this.envVars.put(name, value);
        return this;
    }

    @Override
    public CamelCliLauncher withEnvs(Map<String, String> envVars) {
        this.envVars.putAll(envVars);
        return this;
    }

    @Override
    public CamelCliLauncher workingDir(Path workingDir) {
        this.workingDir = workingDir;
        return this;
    }

    @Override
    public Path getWorkingDir() {
        return workingDir;
    }

    List<String> buildCommand(String command, List<String> args) {
        List<String> shellCommand = new ArrayList<>();
        if (IS_OS_CYGWIN) {
            shellCommand.add("/bin/bash");
            shellCommand.add("-c");
        } else if (IS_OS_WINDOWS) {
            shellCommand.add("cmd.exe");
            shellCommand.add("/c");
        } else {
            shellCommand.add("sh");
            shellCommand.add("-c");
        }

        StringBuilder javaCommand = new StringBuilder("java");

        if (!systemProperties.isEmpty()) {
            javaCommand.append(" ");
            javaCommand.append(systemProperties.entrySet()
                    .stream()
                    .map(entry -> "-D%s=\"%s\"".formatted(entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining(" ")));
        }

        javaCommand.append(" -jar ").append(jarPath);
        javaCommand.append(" ").append(command);

        if (!args.isEmpty()) {
            javaCommand.append(" ").append(String.join(" ", args));
        }

        shellCommand.add(javaCommand.toString());
        return shellCommand;
    }

    private static ProcessAndOutput execute(List<String> command, Path workingDir, Map<String, String> envVars) {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing Camel Launcher command: {}", String.join(" ", command));
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
            ProcessAndOutput pao = new ProcessAndOutput(p);
            String output = pao.waitFor();

            if (LOG.isDebugEnabled() && p.exitValue() != OK_EXIT_CODE) {
                LOG.debug("Command failed: {}", String.join(" ", command));
                LOG.debug(output);
            }

            return pao;
        } catch (IOException | InterruptedException e) {
            throw new CitrusRuntimeException("Error while executing Camel Launcher jar", e);
        }
    }

    private static ProcessAndOutput executeAsync(List<String> command, Path workingDir,
                                                  File outputFile, Map<String, String> envVars) {
        try {
            if (outputFile != null && !outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
                throw new CitrusRuntimeException("Unable to create process output directory: " + outputFile.getParent());
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing Camel Launcher command: {}", String.join(" ", command));
            }

            ProcessBuilder pBuilder = new ProcessBuilder(command)
                    .redirectErrorStream(true);

            if (outputFile != null) {
                pBuilder.redirectOutput(outputFile);
            }

            if (envVars != null) {
                pBuilder.environment().putAll(envVars);
            }

            if (workingDir != null) {
                pBuilder.directory(workingDir.toFile());
            }

            Process p = pBuilder.start();
            if (outputFile != null) {
                return new ProcessAndOutput(p, outputFile);
            }
            return new ProcessAndOutput(p);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Error while executing Camel Launcher jar", e);
        }
    }
}
