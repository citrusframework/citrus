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

package org.citrusframework.agent;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.citrusframework.TestSourceProvider;
import org.citrusframework.main.CitrusAppConfiguration;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.yaml.SchemaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CitrusAgentConfiguration extends CitrusAppConfiguration {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CitrusAgentConfiguration.class);

    /** Server port */
    private int port = CitrusAgentSettings.getServerPort();

    private boolean offline;

    private boolean inspectCode = true;

    /**
     * Gets the port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port.
     */
    @SchemaProperty(required = true, description = "The Http port the agent service is listening on.", defaultValue = "4567")
    public void setPort(int port) {
        this.port = port;
    }

    public boolean isOffline() {
        return offline;
    }

    @SchemaProperty(description = "When enabled there will be no attempts to resolve Maven artifacts via internet connection.", defaultValue = "true")
    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    public boolean isInspectCode() {
        return inspectCode;
    }

    @SchemaProperty(description = "When enabled the source code gets analyzed for required modules and dependencies that are added to the classpath.")
    public void setInspectCode(boolean inspectCode) {
        this.inspectCode = inspectCode;
    }

    /**
     * Applies configuration with settable properties at runtime.
     */
    public void apply(CitrusAppConfiguration configuration) {
        setEngine(configuration.getEngine());
        setPackages(configuration.getPackages());
        setTestSources(configuration.getTestSources());
        setIncludes(configuration.getIncludes());
        setWorkDir(configuration.getWorkDir());
        setVerbose(configuration.isVerbose());
        setReset(configuration.isReset());
        addDefaultProperties(configuration.getDefaultProperties());
        setConfigClass(configuration.getConfigClass());
    }

    public static CitrusAgentConfiguration fromEnvVars(TestSourceProvider provider) {
        CitrusAgentConfiguration configuration = new CitrusAgentConfiguration();

        configuration.setPort(CitrusAgentSettings.getServerPort());

        configuration.setEngine(CitrusAgentSettings.getTestEngine());
        configuration.setIncludes(CitrusAgentSettings.getIncludes());
        configuration.setWorkDir(CitrusAgentSettings.getWorkDir());
        configuration.setSystemExit(CitrusAgentSettings.isSystemExit());
        configuration.setSkipTests(CitrusAgentSettings.isSkipTests());
        configuration.setConfigClass(CitrusAgentSettings.getConfigClass());

        configuration.setPackages(Arrays.asList(CitrusAgentSettings.getPackages()));
        configuration.setTestSources(Arrays.stream(CitrusAgentSettings.getTestSources())
                .map(provider::create)
                .collect(Collectors.toList()));

        configuration.setVerbose(CitrusAgentSettings.isVerbose());
        configuration.setReset(CitrusAgentSettings.isReset());
        configuration.addDefaultProperties(CitrusAgentSettings.getDefaultProperties());

        String testJarPath = CitrusAgentSettings.getTestJar();
        Resource testJar = Resources.create(testJarPath);
        if (testJar.exists()) {
            configuration.setTestJar(testJar.getFile());
        } else {
            logger.debug("Ignore test jar artifact {} - not found", testJarPath);
        }

        configuration.setTimeToLive(CitrusAgentSettings.getTimeToLive());

        configuration.setModules(CitrusAgentSettings.getModules());
        configuration.setDependencies(CitrusAgentSettings.getDependencies());

        configuration.setOffline(CitrusAgentSettings.isOffline());
        configuration.setInspectCode(CitrusAgentSettings.isInspectCode());

        return configuration;
    }
}
