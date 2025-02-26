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

package org.citrusframework.kubernetes;

public final class CitrusAgentSettings {

    private static final String AGENT_PROPERTY_PREFIX = "citrus.agent.";
    private static final String AGENT_ENV_PREFIX = "CITRUS_AGENT_";

    private static final String AGENT_NAME_PROPERTY = AGENT_PROPERTY_PREFIX + "name";
    private static final String AGENT_NAME_ENV = AGENT_ENV_PREFIX + "NAME";
    private static final String AGENT_NAME_DEFAULT = "citrus-agent";

    private static final String AGENT_IMAGE_PROPERTY = AGENT_PROPERTY_PREFIX + "image";
    private static final String AGENT_IMAGE_ENV = AGENT_ENV_PREFIX + "IMAGE";
    private static final String AGENT_IMAGE_DEFAULT = "citrusframework/citrus-agent";

    private static final String AGENT_SERVER_PORT_PROPERTY = AGENT_PROPERTY_PREFIX + "server.port";
    private static final String AGENT_SERVER_PORT_ENV = AGENT_ENV_PREFIX + "SERVER_PORT";
    private static final String AGENT_SERVER_PORT_DEFAULT = "4567";

    private static final String AGENT_VERSION_PROPERTY = AGENT_PROPERTY_PREFIX + "version";
    private static final String AGENT_VERSION_ENV = AGENT_ENV_PREFIX + "VERSION";
    private static final String AGENT_VERSION_DEFAULT = "latest";

    private CitrusAgentSettings() {
        // prevent instantiation of utility class
    }

    /**
     * Citrus agent name.
     * @return
     */
    public static String getAgentName() {
        return System.getProperty(AGENT_NAME_PROPERTY,
                System.getenv(AGENT_NAME_ENV) != null ? System.getenv(AGENT_NAME_ENV) : AGENT_NAME_DEFAULT);
    }

    /**
     * Citrus agent container image.
     * @return
     */
    public static String getImage() {
        return System.getProperty(AGENT_IMAGE_PROPERTY,
                System.getenv(AGENT_IMAGE_ENV) != null ? System.getenv(AGENT_IMAGE_ENV) : AGENT_IMAGE_DEFAULT);
    }

    /**
     * Citrus agent service port.
     * @return
     */
    public static String getServerPort() {
        return System.getProperty(AGENT_SERVER_PORT_PROPERTY,
                System.getenv(AGENT_SERVER_PORT_ENV) != null ? System.getenv(AGENT_SERVER_PORT_ENV) : AGENT_SERVER_PORT_DEFAULT);
    }

    /**
     * Citrus agent container image.
     * @return
     */
    public static String getVersion() {
        return System.getProperty(AGENT_VERSION_PROPERTY,
                System.getenv(AGENT_VERSION_ENV) != null ? System.getenv(AGENT_VERSION_ENV) : AGENT_VERSION_DEFAULT);
    }

}
