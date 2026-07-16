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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.citrusframework.jbang.ProcessAndOutput;
import org.citrusframework.jbang.ProcessLauncher;

/**
 * ProcessLauncher implementation that executes Camel CLI commands using the Camel Launcher jar.
 * Commands are constructed as: {@code java [-Dsysprop=val]... -jar <path-to-jar> <command> <args>}
 */
public class CamelLauncherSupport implements ProcessLauncher {

    private final String jarPath;

    private final Map<String, String> systemProperties = new HashMap<>();
    private final Map<String, String> envVars = new HashMap<>();

    private Path workingDir;

    public CamelLauncherSupport(String jarPath) {
        this.jarPath = jarPath;
    }

    @Override
    public ProcessAndOutput run(String command, String... args) {
        return run(command, Arrays.asList(args));
    }

    @Override
    public ProcessAndOutput run(String command, List<String> args) {
        return execute(buildCommand(command, args), workingDir, envVars, null);
    }

    @Override
    public ProcessAndOutput runAsync(String command, String... args) {
        return runAsync(command, Arrays.asList(args));
    }

    @Override
    public ProcessAndOutput runAsync(String command, List<String> args) {
        return executeAsync(buildCommand(command, args), workingDir, envVars, null);
    }

    @Override
    public ProcessAndOutput runAsync(String command, File output, String... args) {
        return runAsync(command, output, Arrays.asList(args));
    }

    @Override
    public ProcessAndOutput runAsync(String command, File output, List<String> args) {
        return executeAsync(buildCommand(command, args), workingDir, output, envVars, null);
    }

    @Override
    public CamelLauncherSupport withSystemProperty(String name, String value) {
        this.systemProperties.put(name, value);
        return this;
    }

    @Override
    public CamelLauncherSupport withSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties.putAll(systemProperties);
        return this;
    }

    @Override
    public CamelLauncherSupport withEnv(String name, String value) {
        this.envVars.put(name, value);
        return this;
    }

    @Override
    public CamelLauncherSupport withEnvs(Map<String, String> envVars) {
        this.envVars.putAll(envVars);
        return this;
    }

    @Override
    public CamelLauncherSupport workingDir(Path workingDir) {
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

        shellCommand.add("java");

        if (!systemProperties.isEmpty()) {
            shellCommand.add(systemProperties.entrySet()
                    .stream()
                    .map(entry -> "-D%s=\"%s\"".formatted(entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining(" ")));
        }

        shellCommand.add("-jar");
        shellCommand.add(jarPath);
        shellCommand.add(command);

        shellCommand.addAll(args);

        return shellCommand;
    }
}
