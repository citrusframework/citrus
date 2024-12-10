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

package org.citrusframework.testcontainers.compose;

import java.util.Optional;

import org.citrusframework.testcontainers.TestContainersSettings;

public class ComposeContainerSettings {

    private static final String COMPOSE_PROPERTY_PREFIX = TestContainersSettings.TESTCONTAINERS_PROPERTY_PREFIX + "compose.";
    private static final String COMPOSE_ENV_PREFIX = TestContainersSettings.TESTCONTAINERS_ENV_PREFIX + "COMPOSE_";

    private static final String CONTAINER_NAME_PROPERTY = COMPOSE_PROPERTY_PREFIX + "container.name";
    private static final String CONTAINER_NAME_ENV = COMPOSE_ENV_PREFIX + "CONTAINER_NAME";

    private static final String USE_COMPOSE_BINARY_PROPERTY = COMPOSE_PROPERTY_PREFIX + "use.compose.binary";
    private static final String USE_COMPOSE_BINARY_ENV = COMPOSE_ENV_PREFIX + "USE_COMPOSE_BINARY";
    public static final String USE_COMPOSE_BINARY_DEFAULT = "true";

    private static final String STARTUP_TIMEOUT_PROPERTY = COMPOSE_PROPERTY_PREFIX + "startup.timeout";
    private static final String STARTUP_TIMEOUT_ENV = COMPOSE_ENV_PREFIX + "STARTUP_TIMEOUT";

    private ComposeContainerSettings() {
        // prevent instantiation of utility class
    }

    /**
     * LocalStack container name.
     * @return the container name.
     */
    public static String getContainerName() {
        return System.getProperty(CONTAINER_NAME_PROPERTY,
                System.getenv(CONTAINER_NAME_ENV) != null ? System.getenv(CONTAINER_NAME_ENV) : "");
    }

    /**
     * Time in seconds to wait for the container to startup and accept connections.
     * @return
     */
    public static int getStartupTimeout() {
        return Optional.ofNullable(System.getProperty(STARTUP_TIMEOUT_PROPERTY, System.getenv(STARTUP_TIMEOUT_ENV)))
                .map(Integer::parseInt)
                .orElseGet(TestContainersSettings::getStartupTimeout);
    }

    /**
     * Uses local Docker compose binary when set to true.
     * If enabled the experience is closest to using the Docker compose commands (e.g. docker compose up).
     * @return
     */
    public static boolean isUseComposeBinary() {
        return Boolean.parseBoolean(System.getProperty(USE_COMPOSE_BINARY_PROPERTY,
                System.getenv(USE_COMPOSE_BINARY_ENV) != null ? System.getenv(USE_COMPOSE_BINARY_ENV) : USE_COMPOSE_BINARY_DEFAULT));
    }
}
