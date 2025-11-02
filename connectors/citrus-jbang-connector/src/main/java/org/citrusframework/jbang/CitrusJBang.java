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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CitrusJBang {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CitrusJBang.class);

    private final JBangSupport app;

    private String version;

    public CitrusJBang() {
        this(null);
    }

    public CitrusJBang(String app) {
        this.app = JBangSupport.jbang().app(
                Optional.ofNullable(app).orElseGet(JBangSettings::getApp));

        String version = version();
        if (logger.isDebugEnabled()) {
            logger.debug("Citrus JBang version: " + version);
        }
    }

    /**
     * Static entrance method to retrieve instance of Citrus JBang.
     * @return
     */
    public static CitrusJBang citrus() {
        return new CitrusJBang();
    }

    /**
     * Sets the current working directory.
     */
    public CitrusJBang workingDir(Path workingDir) {
        app.workingDir(workingDir);
        return this;
    }

    /**
     * Adds system property to command line.
     */
    public CitrusJBang withSystemProperty(String name, String value) {
        app.withSystemProperty(name, value);
        return this;
    }

    /**
     * Adds system properties to command line.
     */
    public CitrusJBang withSystemProperties(Map<String, String> systemProperties) {
        app.withSystemProperties(systemProperties);
        return this;
    }

    /**
     * Adds environment variables to command line.
     */
    public CitrusJBang withEnv(String name, String value) {
        app.withEnv(name, value);
        return this;
    }

    /**
     * Adds environment variables to command line.
     */
    public CitrusJBang withEnvs(Map<String, String> envVars) {
        app.withEnvs(envVars);
        return this;
    }

    /**
     * Adds classpath entries to the command line.
     */
    public CitrusJBang withClasspathEntries(List<String> entries) {
        app.withClasspathEntries(entries);
        return this;
    }

    /**
     * Adds classpath entries to the command line.
     */
    public CitrusJBang addToClasspath(String path) {
        app.addToClasspath(path);
        return this;
    }

    /**
     * Sets the output listener.
     */
    public CitrusJBang withOutputListener(ProcessOutputListener outputListener) {
        app.withOutputListener(outputListener);
        return this;
    }

    /**
     * Explicitly sets the Citrus version that should be used to run the JBang commands.
     */
    public CitrusJBang withVersion(String version) {
        app.withSystemProperty("citrus.jbang.version", version);
        return this;
    }

    /**
     * Run any command with given arguments.
     */
    public ProcessAndOutput run(String command, String... args) {
        return app.run(command, args);
    }

    /**
     * Provide access to the underlying JBang application.
     */
    public JBangSupport app() {
        return app;
    }

    /**
     * Provides access to the underlying JBang support. For instance to set system properties on the JBang binary.
     */
    public Agent agent() {
        return new Agent();
    }

    /**
     * Run test with given arguments.
     */
    public void run(String fileNameOrDir, Map<String, Object> args) {
        List<String> argsList = new ArrayList<>();
        argsList.add(fileNameOrDir);
        args.entrySet().stream()
                .map(entry -> "%s=%s".formatted(entry.getKey(), entry.getValue()))
                .forEach(argsList::add);
        ProcessAndOutput pao = app.run("run", argsList);

        if (pao.getProcess().exitValue() != 0) {
            logger.error("Failed to run JBang command:\n{}", pao.getOutput());
            throw new CitrusRuntimeException("Test run failed - process exited with exit code: " + pao.getProcess().exitValue());
        }
    }

    /**
     * Stops given Citrus process identified by its process id.
     */
    public void stop(Long pid) {
        ProcessHandle.of(pid).ifPresent(ph -> {
            if (ph.destroyForcibly()) {
                logger.debug("Stopped Citrus agent process (pid: %s)".formatted(pid));
            } else {
                logger.warn("Failed to stop Citrus agent process (pid: %s)".formatted(pid));
            }
        });
    }

    /**
     * Get information on running tests.
     */
    public String ls() {
        ProcessAndOutput p = app.run("ls");
        return p.getOutput();
    }

    /**
     * Get details for integration previously run via JBang Camel app. Integration is identified by its process id.
     * @param pid
     */
    public Map<String, String> get(Long pid) {
        Map<String, String> properties = new HashMap<>();

        String output = ls();
        if (output.isBlank()) {
            return properties;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(output.getBytes(StandardCharsets.UTF_8))))) {
            String line = reader.readLine();

            List<String> names = new ArrayList<>(Arrays.asList(line.trim().split("\\s+")));

            while ((line = reader.readLine()) != null) {
                List<String> values = new ArrayList<>(Arrays.asList(line.trim().split("\\s+")));
                if (!values.isEmpty() && values.get(0).equals(String.valueOf(pid))) {
                    for (int i=0; i < names.size(); i++) {
                        if (i < values.size()) {
                            properties.put(names.get(i), values.get(i));
                        } else {
                            properties.put(names.get(i), "");
                        }
                    }
                    break;
                }
            }

            return properties;
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to get integration details from JBang", e);
        }
    }

    public Map<String, String> get(String name) {
        return getAll().stream()
                .filter(row -> row.getOrDefault("NAME", "").equals(name))
                .findFirst()
                .orElseGet(Collections::emptyMap);
    }

    /**
     * Get list of Citrus tests previously run via JBang Citrus app.
     */
    public List<Map<String, String>> getAll() {
        List<Map<String, String>> processes = new ArrayList<>();

        String output = ls();
        if (output.isBlank()) {
            return processes;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(output.getBytes(StandardCharsets.UTF_8))))) {
            String line = reader.readLine();

            List<String> names = new ArrayList<>(Arrays.asList(line.trim().split("\\s+")));

            while ((line = reader.readLine()) != null) {
                Map<String, String> properties = new HashMap<>();
                List<String> values = new ArrayList<>(Arrays.asList(line.trim().split("\\s+")));
                for (int i=0; i < names.size(); i++) {
                    if (i < values.size()) {
                        properties.put(names.get(i), values.get(i));
                    } else {
                        properties.put(names.get(i), "");
                    }
                }

                processes.add(properties);
            }

            return processes;
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to list Citrus processes from JBang", e);
        }
    }

    /**
     * Get Citrus JBang version.
     */
    public String version() {
        if (!StringUtils.hasText(version)) {
            ProcessAndOutput p = app.run("--version");
            version = p.getOutput();
        }

        return version;
    }

    /**
     * Run Citrus agent related commands.
     */
    public class Agent {

        private boolean dumpOutput = false;

        public Agent dumpOutput(boolean enabled) {
            dumpOutput = enabled;
            return this;
        }

        public ProcessAndOutput start() {
            ProcessAndOutput pao;
            if (dumpOutput) {
                Path workDir = Optional.ofNullable(app.getWorkingDir()).orElseGet(JBangSettings::getWorkDir);
                File outputFile = workDir.resolve("citrus-agent-output.txt").toFile();
                pao = app.runAsync("agent", outputFile, "start");
            } else {
                pao = app.runAsync("agent", "start");
            }

            if (!pao.getProcess().isAlive() && pao.getProcess().exitValue() != JBangSupport.OK_EXIT_CODE) {
                logger.error("Failed to start Citrus agent - exit value %d %n%s".formatted(pao.getProcess().exitValue(), pao.getOutput()));
                throw new CitrusRuntimeException("Failed to start Citrus agent - exit value %d".formatted(pao.getProcess().exitValue()));
            }

            return pao;
        }

        public Long stop() {
            return stop("citrus-agent");
        }

        public Long stop(String serverName) {
            Long pid = Optional.of(CitrusJBang.this.get(serverName).getOrDefault("PID", ""))
                    .filter(StringUtils::hasText)
                    .map(Long::parseLong)
                    .orElse(0L);

            if (pid > 0) {
                CitrusJBang.this.stop(pid);
                return pid;
            } else {
                throw new CitrusRuntimeException("Failed to find JBang Citrus agent process");
            }
        }
    }
}
