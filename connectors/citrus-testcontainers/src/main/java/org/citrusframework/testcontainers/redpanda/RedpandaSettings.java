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

package org.citrusframework.testcontainers.redpanda;

import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.KubernetesSupport;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.testcontainers.redpanda.RedpandaContainer;

import static org.citrusframework.testcontainers.TestcontainersHelper.getEnvVarName;

public class RedpandaSettings {

    private static final String REDPANDA_PROPERTY_PREFIX = TestContainersSettings.TESTCONTAINERS_PROPERTY_PREFIX + "redpanda.";
    private static final String REDPANDA_ENV_PREFIX = TestContainersSettings.TESTCONTAINERS_ENV_PREFIX + "REDPANDA_";

    public static final int REDPANDA_PORT = 9092;

    private static final String VERSION_PROPERTY = REDPANDA_PROPERTY_PREFIX + "version";
    private static final String VERSION_ENV = REDPANDA_ENV_PREFIX + "VERSION";
    private static final String VERSION_DEFAULT = "v24.2.8";

    private static final String SERVICE_NAME_PROPERTY = REDPANDA_PROPERTY_PREFIX + "service.name";
    private static final String SERVICE_NAME_ENV = REDPANDA_ENV_PREFIX + "SERVICE_NAME";
    public static final String SERVICE_NAME_DEFAULT = "citrus-redpanda";

    private static final String CONTAINER_NAME_PROPERTY = REDPANDA_PROPERTY_PREFIX + "container.name";
    private static final String CONTAINER_NAME_ENV = REDPANDA_ENV_PREFIX + "CONTAINER_NAME";
    public static final String CONTAINER_NAME_DEFAULT = "redpandaContainer";

    private static final String IMAGE_NAME_PROPERTY = REDPANDA_PROPERTY_PREFIX + "image.name";
    private static final String IMAGE_NAME_ENV = REDPANDA_ENV_PREFIX + "IMAGE_NAME";
    private static final String IMAGE_NAME_DEFAULT = "redpandadata/redpanda";

    private static final String STARTUP_TIMEOUT_PROPERTY = REDPANDA_PROPERTY_PREFIX + "startup.timeout";
    private static final String STARTUP_TIMEOUT_ENV = REDPANDA_ENV_PREFIX + "STARTUP_TIMEOUT";
    private static final String STARTUP_TIMEOUT_DEFAULT = "180";

    private RedpandaSettings() {
        // prevent instantiation of utility class
    }

    /**
     * Redpanda version setting.
     * @return
     */
    public static String getImageName() {
        return System.getProperty(IMAGE_NAME_PROPERTY,
                System.getenv(IMAGE_NAME_ENV) != null ? System.getenv(IMAGE_NAME_ENV) : IMAGE_NAME_DEFAULT);
    }

    /**
     * Redpanda version setting.
     * @return
     */
    public static String getRedpandaVersion() {
        return System.getProperty(VERSION_PROPERTY,
                System.getenv(VERSION_ENV) != null ? System.getenv(VERSION_ENV) : VERSION_DEFAULT);
    }

    /**
     * Redpanda service name setting.
     * @return
     */
    public static String getServiceName() {
        return System.getProperty(SERVICE_NAME_PROPERTY,
                System.getenv(SERVICE_NAME_ENV) != null ? System.getenv(SERVICE_NAME_ENV) : SERVICE_NAME_DEFAULT);
    }

    /**
     * Redpanda container name setting.
     * @return
     */
    public static String getContainerName() {
        return System.getProperty(CONTAINER_NAME_PROPERTY,
                System.getenv(CONTAINER_NAME_ENV) != null ? System.getenv(CONTAINER_NAME_ENV) : CONTAINER_NAME_DEFAULT);
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
     * Exposes the container connection settings as test variables on the given context.
     * @param container the container holding the connection settings.
     * @param serviceName the service name of the container.
     * @param context the test context to receive the test variables.
     */
    public static void exposeConnectionSettings(RedpandaContainer container, String serviceName, TestContext context) {
        if (container.getContainerId() != null) {
            String dockerContainerId = container.getContainerId().substring(0, 12);
            String dockerContainerName = container.getContainerName();

            if (dockerContainerName.startsWith("/")) {
                dockerContainerName = dockerContainerName.substring(1);
            }

            String containerType = "REDPANDA";
            context.setVariable(getEnvVarName(containerType, "HOST"), container.getHost());
            context.setVariable(getEnvVarName(containerType, "CONTAINER_IP"), container.getHost());
            context.setVariable(getEnvVarName(containerType, "CONTAINER_ID"), dockerContainerId);
            context.setVariable(getEnvVarName(containerType, "CONTAINER_NAME"), dockerContainerName);

            context.setVariable(getEnvVarName(containerType, "LOCAL_BOOTSTRAP_SERVERS"), container.getBootstrapServers());
            context.setVariable(getEnvVarName(containerType, "SERVICE_PORT"), container.getMappedPort(RedpandaSettings.REDPANDA_PORT));
            context.setVariable(getEnvVarName(containerType, "PORT"), container.getMappedPort(RedpandaSettings.REDPANDA_PORT));
            context.setVariable(getEnvVarName(containerType, "SERVICE_LOCAL_BOOTSTRAP_SERVERS"), container.getBootstrapServers());

            if (!KubernetesSupport.isConnected(context) || !TestContainersSettings.isKubedockEnabled()) {
                context.setVariable(getEnvVarName(containerType, "SERVICE_NAME"), serviceName);
                context.setVariable(getEnvVarName(containerType, "SERVICE_BOOTSTRAP_SERVERS"), container.getBootstrapServers());
                context.setVariable(getEnvVarName(containerType, "BOOTSTRAP_SERVERS"), container.getBootstrapServers());
            } else {
                context.setVariable(getEnvVarName(containerType, "SERVICE_NAME"), serviceName);
                context.setVariable(getEnvVarName(containerType, "SERVICE_BOOTSTRAP_SERVERS"), String.format("%s:%s", serviceName, container.getMappedPort(RedpandaSettings.REDPANDA_PORT)));
                context.setVariable(getEnvVarName(containerType, "BOOTSTRAP_SERVERS"), String.format("%s:%s", serviceName, container.getMappedPort(RedpandaSettings.REDPANDA_PORT)));
            }

            context.setVariable(getEnvVarName(containerType, "KUBE_DOCK_SERVICE_BOOTSTRAP_SERVERS"), String.format("%s:%s", serviceName, container.getMappedPort(RedpandaSettings.REDPANDA_PORT)));
            context.setVariable(getEnvVarName(containerType, "KUBE_DOCK_HOST"), serviceName);
        }
    }
}
