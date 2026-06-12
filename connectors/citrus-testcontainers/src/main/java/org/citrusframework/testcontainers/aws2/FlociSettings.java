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

package org.citrusframework.testcontainers.aws2;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import io.floci.testcontainers.FlociContainer;
import org.citrusframework.actions.testcontainers.aws2.AwsService;
import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.KubernetesSupport;
import org.citrusframework.testcontainers.TestContainersSettings;
import software.amazon.awssdk.regions.Region;

import static org.citrusframework.testcontainers.TestcontainersHelper.getEnvVarName;

public class FlociSettings {

    private static final String FLOCI_PROPERTY_PREFIX = TestContainersSettings.TESTCONTAINERS_PROPERTY_PREFIX + "floci.";
    private static final String FLOCI_ENV_PREFIX = TestContainersSettings.TESTCONTAINERS_ENV_PREFIX + "FLOCI_";

    private static final String VERSION_PROPERTY = FLOCI_PROPERTY_PREFIX + "version";
    private static final String VERSION_ENV = FLOCI_ENV_PREFIX + "VERSION";
    public static final String VERSION_DEFAULT = "latest";

    private static final String IMAGE_NAME_PROPERTY = FLOCI_PROPERTY_PREFIX + "image.name";
    private static final String IMAGE_NAME_ENV = FLOCI_ENV_PREFIX + "IMAGE_NAME";
    private static final String IMAGE_NAME_DEFAULT = "floci/floci";

    private static final String SERVICE_NAME_PROPERTY = FLOCI_PROPERTY_PREFIX + "service.name";
    private static final String SERVICE_NAME_ENV = FLOCI_ENV_PREFIX + "SERVICE_NAME";
    public static final String SERVICE_NAME_DEFAULT = "citrus-floci";

    private static final String CONTAINER_NAME_PROPERTY = FLOCI_PROPERTY_PREFIX + "container.name";
    private static final String CONTAINER_NAME_ENV = FLOCI_ENV_PREFIX + "CONTAINER_NAME";
    public static final String CONTAINER_NAME_DEFAULT = "aws2Container";

    private static final String AUTO_CREATE_CLIENTS_PROPERTY = FLOCI_PROPERTY_PREFIX + "auto.create.clients";
    private static final String AUTO_CREATE_CLIENTS_ENV = FLOCI_ENV_PREFIX + "AUTO_CREATE_CLIENTS";
    public static final String AUTO_CREATE_CLIENTS_DEFAULT = "true";

    private static final String ACCOUNT_ID_PROPERTY = FLOCI_PROPERTY_PREFIX + "account.id";
    private static final String ACCOUNT_ID_ENV = FLOCI_ENV_PREFIX + "ACCOUNT_ID";
    public static final String ACCOUNT_ID_DEFAULT = "000000000000";

    private static final String AVAILABILITY_ZONE_PROPERTY = FLOCI_PROPERTY_PREFIX + "availability.zone";
    private static final String AVAILABILITY_ZONE_ENV = FLOCI_ENV_PREFIX + "AVAILABILITY_ZONE";
    public static final String AVAILABILITY_ZONE_DEFAULT = "us-east-1a";

    private static final String REGION_PROPERTY = FLOCI_PROPERTY_PREFIX + "region";
    private static final String REGION_ENV = FLOCI_ENV_PREFIX + "REGION";
    public static final String REGION_DEFAULT = Region.US_EAST_1.id();

    private static final String STARTUP_TIMEOUT_PROPERTY = FLOCI_PROPERTY_PREFIX + "startup.timeout";
    private static final String STARTUP_TIMEOUT_ENV = FLOCI_ENV_PREFIX + "STARTUP_TIMEOUT";

    private static final String AWS_PROPERTY_PREFIX = "aws.";

    public static final String AWS_ACCESS_KEY_PROPERTY = AWS_PROPERTY_PREFIX + "access.key";
    public static final String AWS_SECRET_KEY_PROPERTY = AWS_PROPERTY_PREFIX + "secret.key";
    public static final String AWS_REGION_PROPERTY = AWS_PROPERTY_PREFIX + "region";
    public static final String AWS_HOST_PROPERTY = AWS_PROPERTY_PREFIX + "host";
    public static final String AWS_PROTOCOL_PROPERTY = AWS_PROPERTY_PREFIX + "protocol";

    private FlociSettings() {
        // prevent instantiation of utility class
    }

    /**
     * Floci image name.
     * @return
     */
    public static String getImageName() {
        return TestContainersSettings.getDockerRegistry() + System.getProperty(IMAGE_NAME_PROPERTY,
                System.getenv(IMAGE_NAME_ENV) != null ? System.getenv(IMAGE_NAME_ENV) : IMAGE_NAME_DEFAULT);
    }

    /**
     * Floci version.
     * @return default Docker image version.
     */
    public static String getVersion() {
        return System.getProperty(VERSION_PROPERTY,
                System.getenv(VERSION_ENV) != null ? System.getenv(VERSION_ENV) : VERSION_DEFAULT);
    }

    /**
     * Floci service name.
     * @return the service name.
     */
    public static String getServiceName() {
        return System.getProperty(SERVICE_NAME_PROPERTY,
                System.getenv(SERVICE_NAME_ENV) != null ? System.getenv(SERVICE_NAME_ENV) : SERVICE_NAME_DEFAULT);
    }

    /**
     * Floci container name.
     * @return the container name.
     */
    public static String getContainerName() {
        return System.getProperty(CONTAINER_NAME_PROPERTY,
                System.getenv(CONTAINER_NAME_ENV) != null ? System.getenv(CONTAINER_NAME_ENV) : CONTAINER_NAME_DEFAULT);
    }

    /**
     * Auto create clients for enabled services and add them as beans to the Citrus registry.
     * @return the enabled/disabled flag.
     */
    public static boolean isAutoCreateClients() {
        return Boolean.parseBoolean(System.getProperty(AUTO_CREATE_CLIENTS_PROPERTY,
                System.getenv(AUTO_CREATE_CLIENTS_ENV) != null ? System.getenv(AUTO_CREATE_CLIENTS_ENV) : AUTO_CREATE_CLIENTS_DEFAULT));
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

    public static String getAccountId() {
        return System.getProperty(ACCOUNT_ID_PROPERTY,
                System.getenv(ACCOUNT_ID_ENV) != null ? System.getenv(ACCOUNT_ID_ENV) : ACCOUNT_ID_DEFAULT);
    }

    public static String getAvailabilityZone() {
        return System.getProperty(AVAILABILITY_ZONE_PROPERTY,
                System.getenv(AVAILABILITY_ZONE_ENV) != null ? System.getenv(AVAILABILITY_ZONE_ENV) : AVAILABILITY_ZONE_DEFAULT);
    }

    public static String getRegion() {
        return System.getProperty(REGION_PROPERTY,
                System.getenv(REGION_ENV) != null ? System.getenv(REGION_ENV) : REGION_DEFAULT);
    }

    /**
     * Exposes the container connection settings as test variables on the given context.
     * @param container the container holding the connection settings.
     * @param services the set of enabled services.
     * @param serviceName the service name of the container.
     * @param context the test context to receive the test variables.
     */
    public static void exposeConnectionSettings(FlociContainer container, Set<AwsService> services, String serviceName, TestContext context) {
        if (container.getContainerId() != null) {
            URI serviceEndpoint = URI.create(container.getEndpoint());

            String dockerContainerId = container.getContainerId().substring(0, 12);
            String dockerContainerName = container.getContainerName();

            if (dockerContainerName.startsWith("/")) {
                dockerContainerName = dockerContainerName.substring(1);
            }

            String containerType = "FLOCI";
            context.setVariable(getEnvVarName(containerType, "HOST"), container.getHost());
            context.setVariable(getEnvVarName(containerType, "CONTAINER_IP"), container.getHost());
            context.setVariable(getEnvVarName(containerType, "CONTAINER_ID"), dockerContainerId);
            context.setVariable(getEnvVarName(containerType, "CONTAINER_NAME"), dockerContainerName);
            context.setVariable(getEnvVarName(containerType, "REGION"), container.getRegion());
            context.setVariable(getEnvVarName(containerType, "ACCESS_KEY"), container.getAccessKey());
            context.setVariable(getEnvVarName(containerType, "SECRET_KEY"), container.getSecretKey());
            context.setVariable(getEnvVarName(containerType, "SERVICE_PORT"), serviceEndpoint.getPort());
            context.setVariable(getEnvVarName(containerType, "SERVICE_LOCAL_URL"), String.format("http://localhost:%s", serviceEndpoint.getPort()));

            if (!KubernetesSupport.isConnected(context) || !TestContainersSettings.isKubedockEnabled()) {
                context.setVariable(getEnvVarName(containerType, "SERVICE_NAME"), serviceName);
                context.setVariable(getEnvVarName(containerType, "SERVICE_URL"), String.format("http://localhost:%s", serviceEndpoint.getPort()));
            } else {
                context.setVariable(getEnvVarName(containerType, "SERVICE_NAME"), serviceName);
                context.setVariable(getEnvVarName(containerType, "SERVICE_URL"), String.format("http://%s:%s", serviceName, serviceEndpoint.getPort()));
            }

            services.forEach(service -> {
                String aws2ServiceName = service.getServiceName().toUpperCase(Locale.US);

                if (!KubernetesSupport.isConnected(context) || !TestContainersSettings.isKubedockEnabled()) {
                    context.setVariable(getEnvVarName(containerType, String.format("%s_URL", aws2ServiceName)), String.format("http://localhost:%s", serviceEndpoint.getPort()));
                } else {
                    context.setVariable(getEnvVarName(containerType, String.format("%s_URL", aws2ServiceName)), String.format("http://%s:%s", serviceName, serviceEndpoint.getPort()));
                }

                context.setVariable(getEnvVarName(containerType, String.format("%s_LOCAL_URL", aws2ServiceName)), String.format("http://localhost:%s", serviceEndpoint.getPort()));
                context.setVariable(getEnvVarName(containerType, String.format("%s_PORT", aws2ServiceName)), serviceEndpoint.getPort());
            });

            context.setVariable(getEnvVarName(containerType, "KUBE_DOCK_SERVICE_URL"), String.format("http://%s:%s", serviceName, serviceEndpoint.getPort()));
            context.setVariable(getEnvVarName(containerType, "KUBE_DOCK_HOST"), serviceName);

            for (Map.Entry<Object, Object> connectionProperty : getConnectionProperties(container).entrySet()) {
                context.setVariable(connectionProperty.getKey().toString(), connectionProperty.getValue().toString());
            }
        }
    }

    /**
     * Provides the connection properties to this container.
     * Clients may use these to initialize.
     * @return connection properties.
     */
    private static Properties getConnectionProperties(FlociContainer container) {
        Properties properties = new Properties();

        properties.put(FlociSettings.AWS_ACCESS_KEY_PROPERTY, container.getAccessKey());
        properties.put(FlociSettings.AWS_SECRET_KEY_PROPERTY, container.getSecretKey());
        properties.put(FlociSettings.AWS_REGION_PROPERTY, container.getRegion());
        properties.put(FlociSettings.AWS_HOST_PROPERTY, container.getHost() + ":" + container.getMappedPort(FlociContainer.PORT));
        properties.put(FlociSettings.AWS_PROTOCOL_PROPERTY, "http");

        return properties;
    }
}
