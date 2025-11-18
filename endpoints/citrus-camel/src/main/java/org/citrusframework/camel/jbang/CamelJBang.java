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

package org.citrusframework.camel.jbang;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.JBangSupport;
import org.citrusframework.jbang.ProcessAndOutput;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.citrusframework.jbang.JBangSupport.OK_EXIT_CODE;

/**
 * Camel JBang app.
 */
public class CamelJBang {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CamelJBang.class);

    private final JBangSupport app = JBangSupport.jbang().app(CamelJBangSettings.getCamelApp());

    private boolean dumpIntegrationOutput = CamelJBangSettings.isDumpIntegrationOutput();

    private String version;

    /**
     * Prevent direct instantiation.
     */
    private CamelJBang() {
        if (!"latest".equals(CamelJBangSettings.getCamelVersion())) {
            app.withSystemProperty("camel.jbang.version", CamelJBangSettings.getCamelVersion());
        }

        if (!CamelJBangSettings.getKameletsVersion().isBlank()) {
            app.withSystemProperty("camel-kamelets.version", CamelJBangSettings.getKameletsVersion());
        }

        for (String url : CamelJBangSettings.getTrustUrl()) {
            app.trust(url);
        }

        String version = version();
        if (logger.isDebugEnabled()) {
            logger.debug("Camel JBang version: " + version);
        }
    }

    /**
     * Static entrance method to retrieve instance of Camel JBang.
     */
    public static CamelJBang camel() {
        return new CamelJBang();
    }

    /**
     * Sets the current working directory.
     */
    public CamelJBang workingDir(Path workingDir) {
        app.workingDir(workingDir);
        return this;
    }

    /**
     * Adds system property to command line.
     */
    public CamelJBang withSystemProperty(String name, String value) {
        app.withSystemProperty(name, value);
        return this;
    }

    /**
     * Adds system properties to command line.
     */
    public CamelJBang withSystemProperties(Map<String, String> systemProperties) {
        app.withSystemProperties(systemProperties);
        return this;
    }

    /**
     * Adds environment variables to command line.
     */
    public CamelJBang withEnv(String name, String value) {
        app.withEnv(name, value);
        return this;
    }

    /**
     * Adds environment variables to command line.
     */
    public CamelJBang withEnvs(Map<String, String> envVars) {
        app.withEnvs(envVars);
        return this;
    }

    /**
     * Run given integration with JBang Camel app.
     */
    public ProcessAndOutput run(String name, String file, List<String> resources, String... args) {
        List<String> runArgs = new ArrayList<>();
        runArgs.add("--name");
        runArgs.add(name);

        if (CamelJBangSettings.getKameletsLocalDir() != null) {
            runArgs.add("--local-kamelet-dir");
            runArgs.add(CamelJBangSettings.getKameletsLocalDir().toString());
        }

        runArgs.addAll(Arrays.asList(args));

        runArgs.add(file);
        runArgs.addAll(resources);

        if (dumpIntegrationOutput) {
            Path workDir = CamelJBangSettings.getWorkDir();
            File outputFile = workDir.resolve(String.format("i-%s-output.txt", name)).toFile();

            if (Stream.of(args).noneMatch(it -> it.contains("--logging-color"))) {
                // disable logging colors when writing logs to file
                runArgs.add("--logging-color=false");
            }

            return app.runAsync("run", outputFile, runArgs);
        } else {
            return app.runAsync("run", runArgs);
        }
    }

    /**
     * Execute custom integration with JBang Camel app.
     */
    public ProcessAndOutput custom(String cmd, String workDir, List<String> integrations, List<String> resourcePaths, String... args) {
        List<String> runArgs = new ArrayList<>(integrations);

        if (CamelJBangSettings.getKameletsLocalDir() != null) {
            runArgs.add("--local-kamelet-dir");
            runArgs.add(CamelJBangSettings.getKameletsLocalDir().toString());
        }

        runArgs.addAll(resourcePaths);

        runArgs.addAll(Arrays.asList(args));

        if (dumpIntegrationOutput) {
            Path workDirP = Path.of(workDir);
            File outputFile = workDirP.resolve(String.format("i-%s-output.txt", integrations.get(0))).toFile();

            if (Stream.of(args).noneMatch(it -> it.contains("--logging-color"))) {
                // disable logging colors when writing logs to file
                runArgs.add("--logging-color=false");
            }

            return app.runAsync(cmd, outputFile, runArgs);
        } else {
            return app.runAsync(cmd, runArgs);
        }
    }

    /**
     * Stops all Camel integrations run via Came JBang.
     */
    public void stop() {
        ProcessAndOutput p = app.run("stop") ;
        if (p.getProcess().exitValue() != OK_EXIT_CODE) {
            throw new CitrusRuntimeException(String.format("Failed to stop Camel integration - exit code %d", p.getProcess().exitValue()));
        }
    }

    /**
     * Stops given Camel integration identified by its process id.
     */
    public void stop(Long pid) {
        ProcessAndOutput p = app.run("stop", String.valueOf(pid)) ;
        if (p.getProcess().exitValue() != OK_EXIT_CODE) {
            throw new CitrusRuntimeException(String.format("Failed to stop Camel integration - exit code %d", p.getProcess().exitValue()));
        }
    }

    /**
     * Get information on running integrations.
     */
    public String ps() {
        ProcessAndOutput p = app.run("ps");
        return p.getOutput();
    }

    /**
     * Get Camel JBang version.
     */
    public String version() {
        if (!StringUtils.hasText(version)) {
            ProcessAndOutput p = app.run("--version");
            version = p.getOutput();
        }

        return version;
    }

    /**
     * Get details for integration previously run via JBang Camel app. Integration is identified by its process id.
     */
    public Map<String, String> get(Long pid) {
        Map<String, String> properties = new HashMap<>();

        String output = ps();
        if (output.isBlank()) {
            return properties;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(output.getBytes(StandardCharsets.UTF_8))))) {
            String line = reader.readLine();
            //on Fedora system, the first line contains some timestamps and not property names, in this case, skip it
            if(line != null && !line.trim().matches("^\\w+(\\s+\\w+)*$")) {
                line = reader.readLine();
                if (line == null) {
                    return properties;
                }
            }

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

    /**
     * Get list of integrations previously run via JBang Camel app.
     */
    public List<Map<String, String>> getAll() {
        List<Map<String, String>> integrations = new ArrayList<>();

        String output = ps();
        if (output.isBlank()) {
            return integrations;
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

                integrations.add(properties);
            }

            return integrations;
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to list integrations from JBang", e);
        }
    }

    public CamelJBang dumpIntegrationOutput(boolean enabled) {
        this.dumpIntegrationOutput = enabled;
        return this;
    }

    public List<String> getPlugins() {
        ProcessAndOutput p = app.run("plugin","get");
        String output = p.getOutput();
        List<String> installedPlugins = new ArrayList<>();
        if (output.isBlank()) {
            return installedPlugins;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(output.getBytes(StandardCharsets.UTF_8))))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> values = new ArrayList<>(Arrays.asList(line.trim().split("\\s+")));
                installedPlugins.add(values.get(0));
            }
            return installedPlugins;
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to list plugins from JBang", e);
        }
    }

    public void addPlugin(String pluginName, String ... args) {
        List<String> fullArgs = new ArrayList<>();
        fullArgs.add("add");
        fullArgs.add(pluginName);
        fullArgs.addAll(Arrays.asList(args));

        ProcessAndOutput pao = app.run("plugin", fullArgs);
        int exitValue = pao.getProcess().exitValue();
        if (exitValue != OK_EXIT_CODE && exitValue != 1) {
            throw new CitrusRuntimeException("Error while adding Camel JBang plugin. Exit code: " + exitValue);
        }
    }

    public ProcessAndOutput send(String ... args) {
        List<String> fullArgs = new ArrayList<>();
        fullArgs.add("send");
        fullArgs.addAll(Arrays.asList(args));

        ProcessAndOutput pao = app.run("cmd", fullArgs);
        int exitValue = pao.getProcess().exitValue();
        if (exitValue != OK_EXIT_CODE && exitValue != 1) {
            logger.warn("Failed to send message via Camel JBang command:%n\t camel cmd %s".formatted(String.join(" ", fullArgs)));
            throw new CitrusRuntimeException("Error while sending message via Camel JBang: '%s' Exit code: %d"
                    .formatted(pao.getOutput(), exitValue));
        }

        if (pao.getOutput().contains("Send timeout")) {
            throw new CitrusRuntimeException("Send timeout while sending message via Camel JBang");
        }

        return pao;
    }

    public ProcessAndOutput receive(String ... args) {
        List<String> fullArgs = new ArrayList<>();
        fullArgs.add("receive");
        fullArgs.addAll(Arrays.asList(args));

        return app.runAsync("cmd", fullArgs);
    }

    public KubernetesPlugin kubernetes() {
        return new KubernetesPlugin(app);
    }

    public InfraPlugin infra() {
        return new InfraPlugin(app);
    }

}
