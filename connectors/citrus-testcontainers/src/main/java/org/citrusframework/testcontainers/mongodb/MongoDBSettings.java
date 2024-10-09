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

package org.citrusframework.testcontainers.mongodb;

import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.KubernetesSupport;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.testcontainers.containers.MongoDBContainer;

import static org.citrusframework.testcontainers.TestcontainersHelper.getEnvVarName;

public class MongoDBSettings {

    private static final String MONGODB_PROPERTY_PREFIX = TestContainersSettings.TESTCONTAINERS_PROPERTY_PREFIX + "mongodb.";
    private static final String MONGODB_ENV_PREFIX = TestContainersSettings.TESTCONTAINERS_ENV_PREFIX + "MONGODB_";

    private static final String VERSION_PROPERTY = MONGODB_PROPERTY_PREFIX + "version";
    private static final String VERSION_ENV = MONGODB_ENV_PREFIX + "VERSION";
    private static final String VERSION_DEFAULT = "4.0.10";

    private static final String SERVICE_NAME_PROPERTY = MONGODB_PROPERTY_PREFIX + "service.name";
    private static final String SERVICE_NAME_ENV = MONGODB_ENV_PREFIX + "SERVICE_NAME";
    public static final String SERVICE_NAME_DEFAULT = "citrus-mongodb";

    private static final String CONTAINER_NAME_PROPERTY = MONGODB_PROPERTY_PREFIX + "container.name";
    private static final String CONTAINER_NAME_ENV = MONGODB_ENV_PREFIX + "CONTAINER_NAME";
    public static final String CONTAINER_NAME_DEFAULT = "mongoDBContainer";

    private static final String STARTUP_TIMEOUT_PROPERTY = MONGODB_PROPERTY_PREFIX + "startup.timeout";
    private static final String STARTUP_TIMEOUT_ENV = MONGODB_ENV_PREFIX + "STARTUP_TIMEOUT";
    private static final String STARTUP_TIMEOUT_DEFAULT = "180";

    private MongoDBSettings() {
        // prevent instantiation of utility class
    }

    /**
     * MongoDB version setting.
     * @return
     */
    public static String getMongoDBVersion() {
        return System.getProperty(VERSION_PROPERTY,
                System.getenv(VERSION_ENV) != null ? System.getenv(VERSION_ENV) : VERSION_DEFAULT);
    }

    /**
     * MongoDB service name.
     * @return
     */
    public static String getServiceName() {
        return System.getProperty(SERVICE_NAME_PROPERTY,
                System.getenv(SERVICE_NAME_ENV) != null ? System.getenv(SERVICE_NAME_ENV) : SERVICE_NAME_DEFAULT);
    }

    /**
     * MongoDB container name.
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
    public static void exposeConnectionSettings(MongoDBContainer container, String serviceName, TestContext context) {
        if (container.getContainerId() != null) {
            String dockerContainerId = container.getContainerId().substring(0, 12);
            String dockerContainerName = container.getContainerName();

            if (dockerContainerName.startsWith("/")) {
                dockerContainerName = dockerContainerName.substring(1);
            }

            String containerType = "MONGODB";
            context.setVariable(getEnvVarName(containerType, "HOST"), container.getHost());
            context.setVariable(getEnvVarName(containerType, "CONTAINER_IP"), container.getHost());
            context.setVariable(getEnvVarName(containerType, "CONTAINER_ID"), dockerContainerId);
            context.setVariable(getEnvVarName(containerType, "CONTAINER_NAME"), dockerContainerName);

            context.setVariable(getEnvVarName(containerType, "LOCAL_URL"), container.getReplicaSetUrl());
            context.setVariable(getEnvVarName(containerType, "SERVICE_PORT"), container.getMappedPort(27017));
            context.setVariable(getEnvVarName(containerType, "PORT"), container.getMappedPort(27017));
            context.setVariable(getEnvVarName(containerType, "SERVICE_LOCAL_URL"), container.getReplicaSetUrl());

            if (!KubernetesSupport.isConnected(context) || !TestContainersSettings.isKubedockEnabled()) {
                context.setVariable(getEnvVarName(containerType, "SERVICE_NAME"), serviceName);
                context.setVariable(getEnvVarName(containerType, "SERVICE_URL"), container.getReplicaSetUrl());
                context.setVariable(getEnvVarName(containerType, "URL"), container.getReplicaSetUrl());
            } else {
                context.setVariable(getEnvVarName(containerType, "SERVICE_NAME"), serviceName);
                context.setVariable(getEnvVarName(containerType, "SERVICE_URL"), String.format("mongodb://%s:%d/test", serviceName, container.getMappedPort(27017)));
                context.setVariable(getEnvVarName(containerType, "URL"), String.format("mongodb://%s:%d/test", serviceName, container.getMappedPort(27017)));
            }

            context.setVariable(getEnvVarName(containerType, "CONNECTION_STRING"), container.getConnectionString());

            context.setVariable(getEnvVarName(containerType, "KUBE_DOCK_SERVICE_URL"), String.format("mongodb://%s:%d/test", serviceName, container.getMappedPort(27017)));
            context.setVariable(getEnvVarName(containerType, "KUBE_DOCK_HOST"), serviceName);
        }
    }
}
