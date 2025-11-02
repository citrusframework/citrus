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

import org.citrusframework.main.CitrusAppConfiguration;
import org.citrusframework.yaml.SchemaProperty;

public class CitrusAgentConfiguration extends CitrusAppConfiguration {

    /** Server port */
    private int port = 4567;

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
}
