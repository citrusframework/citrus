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

package org.citrusframework.agent.connector;

public final class CitrusAgentSettings {

    private static final String AGENT_PROPERTY_PREFIX = "citrus.agent.";
    private static final String AGENT_ENV_PREFIX = "CITRUS_AGENT_";

    private static final String AGENT_NAME_PROPERTY = AGENT_PROPERTY_PREFIX + "name";
    private static final String AGENT_NAME_ENV = AGENT_ENV_PREFIX + "NAME";
    private static final String AGENT_NAME_DEFAULT = "citrus-agent";

    private static final String AGENT_SERVER_PORT_PROPERTY = AGENT_PROPERTY_PREFIX + "server.port";
    private static final String AGENT_SERVER_PORT_ENV = AGENT_ENV_PREFIX + "SERVER_PORT";
    private static final String AGENT_SERVER_PORT_DEFAULT = "4567";

    private CitrusAgentSettings() {
        // prevent instantiation of utility class
    }

    /**
     * Citrus agent name.
     */
    public static String getAgentName() {
        return System.getProperty(AGENT_NAME_PROPERTY,
                System.getenv(AGENT_NAME_ENV) != null ? System.getenv(AGENT_NAME_ENV) : AGENT_NAME_DEFAULT);
    }

    /**
     * Citrus agent server port.
     */
    public static int getAgentServerPort() {
        return Integer.parseInt(System.getProperty(AGENT_SERVER_PORT_PROPERTY,
                System.getenv(AGENT_SERVER_PORT_ENV) != null ? System.getenv(AGENT_SERVER_PORT_ENV) : AGENT_SERVER_PORT_DEFAULT));
    }

}
