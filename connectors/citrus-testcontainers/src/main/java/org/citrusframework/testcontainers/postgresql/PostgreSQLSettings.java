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

package org.citrusframework.testcontainers.postgresql;

import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.KubernetesSupport;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.citrusframework.testcontainers.TestcontainersHelper.getEnvVarName;

public class PostgreSQLSettings {

    private static final String POSTGRESQL_PROPERTY_PREFIX = TestContainersSettings.TESTCONTAINERS_PROPERTY_PREFIX + "postgresql.";
    private static final String POSTGRESQL_ENV_PREFIX = TestContainersSettings.TESTCONTAINERS_ENV_PREFIX + "POSTGRESQL_";

    private static final String IMAGE_NAME_PROPERTY = POSTGRESQL_PROPERTY_PREFIX + "image.name";
    private static final String IMAGE_NAME_ENV = POSTGRESQL_ENV_PREFIX + "IMAGE_NAME";
    private static final String IMAGE_NAME_DEFAULT = "postgres";

    private static final String POSTGRESQL_VERSION_PROPERTY = POSTGRESQL_PROPERTY_PREFIX + "version";
    private static final String POSTGRESQL_VERSION_ENV = POSTGRESQL_ENV_PREFIX + "POSTGRESQL_VERSION";
    private static final String POSTGRESQL_VERSION_DEFAULT = PostgreSQLContainer.DEFAULT_TAG;

    private static final String SERVICE_NAME_PROPERTY = POSTGRESQL_PROPERTY_PREFIX + "service.name";
    private static final String SERVICE_NAME_ENV = POSTGRESQL_ENV_PREFIX + "SERVICE_NAME";
    public static final String SERVICE_NAME_DEFAULT = "citrus-postgresql";

    private static final String CONTAINER_NAME_PROPERTY = POSTGRESQL_PROPERTY_PREFIX + "container.name";
    private static final String CONTAINER_NAME_ENV = POSTGRESQL_ENV_PREFIX + "CONTAINER_NAME";
    public static final String CONTAINER_NAME_DEFAULT = "postgreSQLContainer";

    private static final String DATABASE_NAME_PROPERTY = POSTGRESQL_PROPERTY_PREFIX + "db.name";
    private static final String DATABASE_NAME_ENV = POSTGRESQL_ENV_PREFIX + "DB_NAME";
    private static final String DATABASE_NAME_DEFAULT = "test";

    private static final String USERNAME_PROPERTY = POSTGRESQL_PROPERTY_PREFIX + "username";
    private static final String USERNAME_ENV = POSTGRESQL_ENV_PREFIX + "USERNAME";
    private static final String USERNAME_DEFAULT = "test";

    private static final String PASSWORD_PROPERTY = POSTGRESQL_PROPERTY_PREFIX + "password";
    private static final String PASSWORD_ENV = POSTGRESQL_ENV_PREFIX + "PASSWORD";
    private static final String PASSWORD_DEFAULT = "test";

    private static final String STARTUP_TIMEOUT_PROPERTY = POSTGRESQL_PROPERTY_PREFIX + "startup.timeout";
    private static final String STARTUP_TIMEOUT_ENV = POSTGRESQL_ENV_PREFIX + "STARTUP_TIMEOUT";
    private static final String STARTUP_TIMEOUT_DEFAULT = "180";

    private PostgreSQLSettings() {
        // prevent instantiation of utility class
    }

    /**
     * PostgreSQL image name setting.
     * @return
     */
    public static String getImageName() {
        return TestContainersSettings.getDockerRegistry() + System.getProperty(IMAGE_NAME_PROPERTY,
                System.getenv(IMAGE_NAME_ENV) != null ? System.getenv(IMAGE_NAME_ENV) : IMAGE_NAME_DEFAULT);
    }

    /**
     * PostgreSQL service name.
     * @return default service name.
     */
    public static String getServiceName() {
        return System.getProperty(SERVICE_NAME_PROPERTY,
                System.getenv(SERVICE_NAME_ENV) != null ? System.getenv(SERVICE_NAME_ENV) : SERVICE_NAME_DEFAULT);
    }

    /**
     * PostgreSQL container name.
     * @return default container name.
     */
    public static String getContainerName() {
        return System.getProperty(CONTAINER_NAME_PROPERTY,
                System.getenv(CONTAINER_NAME_ENV) != null ? System.getenv(CONTAINER_NAME_ENV) : CONTAINER_NAME_DEFAULT);
    }

    /**
     * PostgreSQL database name.
     * @return default database name.
     */
    public static String getDatabaseName() {
        return System.getProperty(DATABASE_NAME_PROPERTY,
                System.getenv(DATABASE_NAME_ENV) != null ? System.getenv(DATABASE_NAME_ENV) : DATABASE_NAME_DEFAULT);
    }

    /**
     * PostgreSQL user name.
     * @return default user name.
     */
    public static String getUsername() {
        return System.getProperty(USERNAME_PROPERTY,
                System.getenv(USERNAME_ENV) != null ? System.getenv(USERNAME_ENV) : USERNAME_DEFAULT);
    }

    /**
     * PostgreSQL password.
     * @return default password.
     */
    public static String getPassword() {
        return System.getProperty(PASSWORD_PROPERTY,
                System.getenv(PASSWORD_ENV) != null ? System.getenv(PASSWORD_ENV) : PASSWORD_DEFAULT);
    }

    /**
     * PostgreSQL version setting.
     * @return
     */
    public static String getPostgreSQLVersion() {
        return System.getProperty(POSTGRESQL_VERSION_PROPERTY,
                System.getenv(POSTGRESQL_VERSION_ENV) != null ? System.getenv(POSTGRESQL_VERSION_ENV) : POSTGRESQL_VERSION_DEFAULT);
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
    public static void exposeConnectionSettings(PostgreSQLContainer<?> container, String serviceName, TestContext context) {
        if (container.getContainerId() != null) {
            String dockerContainerId = container.getContainerId().substring(0, 12);
            String dockerContainerName = container.getContainerName();

            if (dockerContainerName.startsWith("/")) {
                dockerContainerName = dockerContainerName.substring(1);
            }

            String containerType = "POSTGRESQL";
            context.setVariable(getEnvVarName(containerType, "HOST"), container.getHost());
            context.setVariable(getEnvVarName(containerType, "CONTAINER_IP"), container.getHost());
            context.setVariable(getEnvVarName(containerType, "CONTAINER_ID"), dockerContainerId);
            context.setVariable(getEnvVarName(containerType, "CONTAINER_NAME"), dockerContainerName);

            context.setVariable(getEnvVarName(containerType, "SERVICE_PORT"), container.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT));
            context.setVariable(getEnvVarName(containerType, "PORT"), container.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT));
            context.setVariable(getEnvVarName(containerType, "LOCAL_URL"), container.getJdbcUrl());
            context.setVariable(getEnvVarName(containerType, "SERVICE_LOCAL_URL"), container.getJdbcUrl());

            if (!KubernetesSupport.isConnected(context) || !TestContainersSettings.isKubedockEnabled()) {
                context.setVariable(getEnvVarName(containerType, "SERVICE_NAME"), serviceName);
                context.setVariable(getEnvVarName(containerType, "SERVICE_URL"), container.getJdbcUrl());
                context.setVariable(getEnvVarName(containerType, "URL"), container.getJdbcUrl());
            } else {
                context.setVariable(getEnvVarName(containerType, "SERVICE_NAME"), serviceName);
                context.setVariable(getEnvVarName(containerType, "SERVICE_URL"), container.getJdbcUrl().replace("localhost", serviceName));
                context.setVariable(getEnvVarName(containerType, "URL"), container.getJdbcUrl().replace("localhost", serviceName));
            }

            context.setVariable(getEnvVarName(containerType, "DRIVER"), container.getDriverClassName());
            context.setVariable(getEnvVarName(containerType, "DB_NAME"), container.getDatabaseName());
            context.setVariable(getEnvVarName(containerType, "USERNAME"), container.getUsername());
            context.setVariable(getEnvVarName(containerType, "PASSWORD"), container.getPassword());

            context.setVariable(getEnvVarName(containerType, "KUBE_DOCK_SERVICE_URL"), container.getJdbcUrl().replace("localhost", serviceName));
            context.setVariable(getEnvVarName(containerType, "KUBE_DOCK_HOST"), serviceName);
        }
    }
}
