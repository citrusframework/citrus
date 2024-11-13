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

import org.citrusframework.camel.actions.CamelVerifyIntegrationAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.JBangSupport;
import org.citrusframework.jbang.ProcessAndOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Camel JBang app.
 */
public class CamelJBang {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CamelVerifyIntegrationAction.class);

    private final JBangSupport camelApp = JBangSupport.jbang().app(CamelJBangSettings.getCamelApp());

    private boolean dumpIntegrationOutput = CamelJBangSettings.isDumpIntegrationOutput();

    /**
     * Prevent direct instantiation.
     */
    private CamelJBang() {
        if (!"latest".equals(CamelJBangSettings.getCamelVersion())) {
            camelApp.withSystemProperty("camel.jbang.version", CamelJBangSettings.getCamelVersion());
        }

        if (!CamelJBangSettings.getKameletsVersion().isBlank()) {
            camelApp.withSystemProperty("camel-kamelets.version", CamelJBangSettings.getKameletsVersion());
        }

        for (String url : CamelJBangSettings.getTrustUrl()) {
            camelApp.trust(url);
        }

        logger.info("Camel JBang version: " + version());
    }

    /**
     * Static entrance method to retrieve instance of Camel JBang.
     * @return
     */
    public static CamelJBang camel() {
        return new CamelJBang();
    }

    /**
     * Provides access to the underlying JBang support. For instance to set system properties on the JBang binary.
     * @return
     */
    public JBangSupport camelApp() {
        return camelApp;
    }

    /**
     * Run given integration with JBang Camel app.
     * @param name
     * @param path
     * @param resources
     * @param args
     * @return
     */
    public ProcessAndOutput run(String name, Path path, List<String> resources, String... args) {
        return run(name, path.toAbsolutePath().toString(), resources, args);
    }

    /**
     * Run given integration with JBang Camel app.
     * @param name
     * @param file
     * @param resources
     * @param args
     * @return
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

            return camelApp.runAsync("run", outputFile, runArgs);
        } else {
            return camelApp.runAsync("run", runArgs);
        }
    }

    /**
     * Get details for integration previously run via JBang Camel app. Integration is identified by its process id.
     * @param pid
     */
    public void stop(Long pid) {
        ProcessAndOutput p = camelApp.run("stop", String.valueOf(pid)) ;
        if (p.getProcess().exitValue() != JBangSupport.OK_EXIT_CODE) {
            throw new CitrusRuntimeException(String.format("Failed to stop Camel K integration - exit code %d", p.getProcess().exitValue()));
        }
    }

    /**
     * Get information on running integrations.
     */
    public String ps() {
        ProcessAndOutput p = camelApp.run("ps");
        return p.getOutput();
    }

    /**
     * Get Camel JBang version.
     */
    public String version() {
        ProcessAndOutput p = camelApp.run("--version");
        return p.getOutput();
    }

    /**
     * Get details for integration previously run via JBang Camel app. Integration is identified by its process id.
     * @param pid
     */
    public Map<String, String> get(Long pid) {
        Map<String, String> properties = new HashMap<>();

        String output = ps();
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

}
