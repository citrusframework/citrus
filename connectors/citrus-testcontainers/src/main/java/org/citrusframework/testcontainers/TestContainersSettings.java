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

package org.citrusframework.testcontainers;

import java.util.Optional;

public final class TestContainersSettings {

    public static final String TESTCONTAINERS_VARIABLE_PREFIX = "CITRUS_TESTCONTAINERS_";

    public static final String TESTCONTAINERS_PROPERTY_PREFIX = "citrus.testcontainers.";
    public static final String TESTCONTAINERS_ENV_PREFIX = "CITRUS_TESTCONTAINERS_";

    private static final String ENABLED_PROPERTY = TESTCONTAINERS_PROPERTY_PREFIX + "enabled";
    private static final String ENABLED_ENV = TESTCONTAINERS_ENV_PREFIX + "ENABLED";
    private static final String ENABLED_DEFAULT = "true";

    public static final String DOCKER_REGISTRY = "docker.io";
    private static final String REGISTRY_PROPERTY = TESTCONTAINERS_PROPERTY_PREFIX + "registry";
    private static final String REGISTRY_ENV = TESTCONTAINERS_ENV_PREFIX + "REGISTRY";
    private static final String REGISTRY_DEFAULT = DOCKER_REGISTRY;

    private static final String REGISTRY_MIRROR_ENABLED_PROPERTY = TESTCONTAINERS_PROPERTY_PREFIX + "registry.mirror.enabled";
    private static final String REGISTRY_MIRROR_ENABLED_ENV = TESTCONTAINERS_ENV_PREFIX + "REGISTRY_MIRROR_ENABLED";
    private static final String REGISTRY_MIRROR_ENABLED_DEFAULT = "false";

    private static final String REGISTRY_MIRROR_PROPERTY = TESTCONTAINERS_PROPERTY_PREFIX + "registry.mirror";
    private static final String REGISTRY_MIRROR_ENV = TESTCONTAINERS_ENV_PREFIX + "REGISTRY_MIRROR";
    private static final String REGISTRY_MIRROR_DEFAULT = "mirror.gcr.io";

    private static final String AUTO_REMOVE_RESOURCES_PROPERTY = TESTCONTAINERS_PROPERTY_PREFIX + "auto.remove.resources";
    private static final String AUTO_REMOVE_RESOURCES_ENV = TESTCONTAINERS_ENV_PREFIX + "AUTO_REMOVE_RESOURCES";
    private static final String AUTO_REMOVE_RESOURCES_DEFAULT = "true";

    private static final String KUBEDOCK_ENABLED_PROPERTY = TESTCONTAINERS_PROPERTY_PREFIX + "kubedock.enabled";
    private static final String KUBEDOCK_ENABLED_ENV = TESTCONTAINERS_ENV_PREFIX + "KUBEDOCK_ENABLED";
    private static final String KUBEDOCK_ENABLED_DEFAULT = "false";

    private static final String TEST_ID_PROPERTY = "citrus.test.id";
    private static final String TEST_ID_ENV = "CITRUS_TEST_ID";
    private static final String TEST_ID_DEFAULT = "citrus-test";

    private static final String TEST_NAME_PROPERTY = "citrus.test.name";
    private static final String TEST_NAME_ENV = "CITRUS_TEST_NAME";
    private static final String TEST_NAME_DEFAULT = "citrus";

    private static final String STARTUP_TIMEOUT_PROPERTY = TESTCONTAINERS_PROPERTY_PREFIX + "startup.timeout";
    private static final String STARTUP_TIMEOUT_ENV = TESTCONTAINERS_ENV_PREFIX + "STARTUP_TIMEOUT";
    private static final String STARTUP_TIMEOUT_DEFAULT = "180";

    private static final String CONNECT_TIMEOUT_PROPERTY = TESTCONTAINERS_PROPERTY_PREFIX + "connect.timeout";
    private static final String CONNECT_TIMEOUT_ENV = TESTCONTAINERS_ENV_PREFIX + "CONNECT_TIMEOUT";
    private static final String CONNECT_TIMEOUT_DEFAULT = "5000";

    private TestContainersSettings() {
        // prevent instantiation of utility class
    }

    /**
     * Kubernetes may be disabled by default.
     * @return
     */
    public static boolean isEnabled() {
        return Boolean.parseBoolean(System.getProperty(ENABLED_PROPERTY,
                System.getenv(ENABLED_ENV) != null ? System.getenv(ENABLED_ENV) : ENABLED_DEFAULT));
    }

    /**
     * When set to true Kubernetes resources (e.g. services) created during the test are
     * automatically removed after the test.
     * @return
     */
    public static boolean isAutoRemoveResources() {
        return Boolean.parseBoolean(System.getProperty(AUTO_REMOVE_RESOURCES_PROPERTY,
                System.getenv(AUTO_REMOVE_RESOURCES_ENV) != null ? System.getenv(AUTO_REMOVE_RESOURCES_ENV) : AUTO_REMOVE_RESOURCES_DEFAULT));
    }

    /**
     * Docker registry.
     * @return
     */
    public static String getRegistry() {
        return System.getProperty(REGISTRY_PROPERTY, Optional.ofNullable(System.getenv(REGISTRY_ENV)).orElse(REGISTRY_DEFAULT));
    }

    /**
     * True when using Docker registry mirror.
     * @return
     */
    public static boolean isRegistryMirrorEnabled() {
        return Boolean.parseBoolean(System.getProperty(REGISTRY_MIRROR_ENABLED_PROPERTY,
                Optional.ofNullable(System.getenv(REGISTRY_MIRROR_ENABLED_ENV)).orElse(REGISTRY_MIRROR_ENABLED_DEFAULT)));
    }

    /**
     * Docker registry mirror.
     * @return
     */
    public static String getRegistryMirror() {
        return System.getProperty(REGISTRY_MIRROR_PROPERTY, Optional.ofNullable(System.getenv(REGISTRY_MIRROR_ENV)).orElse(REGISTRY_MIRROR_DEFAULT));
    }

    /**
     * True when using KubeDock services.
     * @return
     */
    public static boolean isKubedockEnabled() {
        return Boolean.parseBoolean(System.getProperty(KUBEDOCK_ENABLED_PROPERTY,
                Optional.ofNullable(System.getenv(KUBEDOCK_ENABLED_ENV)).orElse(KUBEDOCK_ENABLED_DEFAULT)));
    }

    /**
     * Current test id that is also set as label on the Pod running the test.
     * @return
     */
    public static String getTestId() {
        return System.getProperty(TEST_ID_PROPERTY, Optional.ofNullable(System.getenv(TEST_ID_ENV)).orElse(TEST_ID_DEFAULT));
    }

    /**
     * Current test id that is also set as label on the Pod running the test.
     * @return
     */
    public static String getTestName() {
        return System.getProperty(TEST_NAME_PROPERTY, Optional.ofNullable(System.getenv(TEST_NAME_ENV)).orElse(TEST_NAME_DEFAULT));
    }

    /**
     * Time in seconds to wait for the container to startup and accept connections.
     * @return
     */
    public static int getStartupTimeout() {
        return Integer.parseInt(System.getProperty(STARTUP_TIMEOUT_PROPERTY,
                System.getenv(STARTUP_TIMEOUT_ENV) != null ? System.getenv(STARTUP_TIMEOUT_ENV) : STARTUP_TIMEOUT_DEFAULT));
    }

    /**
     * Timeout when connecting to Docker.
     * @return
     */
    public static long getConnectTimeout() {
        return Long.parseLong(System.getProperty(CONNECT_TIMEOUT_PROPERTY,
                System.getenv(CONNECT_TIMEOUT_ENV) != null ? System.getenv(CONNECT_TIMEOUT_ENV) : CONNECT_TIMEOUT_DEFAULT));
    }

    /**
     * Gets the Docker registry to use for pulling Testcontainers images.
     * Uses registry mirror if enabled.
     * The default Docker registry is left empty.
     * @return
     */
    public static String getDockerRegistry() {
        String registry = "";
        if (TestContainersSettings.isRegistryMirrorEnabled()) {
            registry = TestContainersSettings.getRegistryMirror() + "/";
        } else if (!TestContainersSettings.DOCKER_REGISTRY.equals(TestContainersSettings.getRegistry())) {
            registry = TestContainersSettings.getRegistry() + "/";
        }

        return registry;
    }
}
