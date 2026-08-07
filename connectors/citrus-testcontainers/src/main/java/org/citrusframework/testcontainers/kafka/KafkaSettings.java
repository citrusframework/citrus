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

package org.citrusframework.testcontainers.kafka;

import org.citrusframework.config.CitrusConfigProperties;
import org.citrusframework.config.CitrusConfigProperty;
import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.KubernetesSupport;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.kafka.KafkaContainer;

import static org.citrusframework.testcontainers.TestcontainersHelper.getEnvVarName;

@CitrusConfigProperties(prefix = "citrus.testcontainers.kafka", description = "Kafka Testcontainers settings")
public class KafkaSettings {

    private static final String KAFKA_PROPERTY_PREFIX = TestContainersSettings.TESTCONTAINERS_PROPERTY_PREFIX + "kafka.";
    private static final String KAFKA_ENV_PREFIX = TestContainersSettings.TESTCONTAINERS_ENV_PREFIX + "KAFKA_";

    public static final String NODE_ID = "1";
    public static final int KAFKA_PORT = 9092;
    public static final int CONTROLLER_PORT = 9093;

    @CitrusConfigProperty(description = "Kafka service name.", defaultValue = "citrus-kafka")
    private static final String SERVICE_NAME_PROPERTY = KAFKA_PROPERTY_PREFIX + "service.name";
    private static final String SERVICE_NAME_ENV = KAFKA_ENV_PREFIX + "SERVICE_NAME";
    public static final String SERVICE_NAME_DEFAULT = "citrus-kafka";

    @CitrusConfigProperty(description = "Kafka implementation to use.", defaultValue = "CONFLUENT")
    private static final String IMPLEMENTATION_PROPERTY = KAFKA_PROPERTY_PREFIX + "implementation";
    private static final String IMPLEMENTATION_ENV = KAFKA_ENV_PREFIX + "IMPLEMENTATION";
    public static final String IMPLEMENTATION_DEFAULT = KafkaImplementation.CONFLUENT.name();

    @CitrusConfigProperty(description = "Kafka container name.", defaultValue = "kafkaContainer")
    private static final String CONTAINER_NAME_PROPERTY = KAFKA_PROPERTY_PREFIX + "container.name";
    private static final String CONTAINER_NAME_ENV = KAFKA_ENV_PREFIX + "CONTAINER_NAME";
    public static final String CONTAINER_NAME_DEFAULT = "kafkaContainer";

    @CitrusConfigProperty(description = "Time in seconds to wait for the Kafka container to startup.", type = "java.lang.Long", defaultValue = "180")
    private static final String STARTUP_TIMEOUT_PROPERTY = KAFKA_PROPERTY_PREFIX + "startup.timeout";
    private static final String STARTUP_TIMEOUT_ENV = KAFKA_ENV_PREFIX + "STARTUP_TIMEOUT";
    private static final String STARTUP_TIMEOUT_DEFAULT = "180";

    @CitrusConfigProperty(description = "Apache Kafka Docker image name.", defaultValue = "apache/kafka")
    private static final String APACHE_IMAGE_NAME_PROPERTY = KAFKA_PROPERTY_PREFIX + "apache.image.name";
    private static final String APACHE_IMAGE_NAME_ENV = KAFKA_ENV_PREFIX + "APACHE_IMAGE_NAME";
    protected static final String APACHE_IMAGE_NAME_DEFAULT = "apache/kafka";

    @CitrusConfigProperty(description = "Apache Kafka version.", defaultValue = "4.2.1")
    private static final String APACHE_VERSION_PROPERTY = KAFKA_PROPERTY_PREFIX + "apache.version";
    private static final String APACHE_VERSION_ENV = KAFKA_ENV_PREFIX + "APACHE_VERSION";
    private static final String APACHE_VERSION_DEFAULT = "4.2.1";

    @CitrusConfigProperty(description = "Apache Kafka native Docker image name.", defaultValue = "apache/kafka-native")
    private static final String APACHE_NATIVE_IMAGE_NAME_PROPERTY = KAFKA_PROPERTY_PREFIX + "apache.native.image.name";
    private static final String APACHE_NATIVE_IMAGE_NAME_ENV = KAFKA_ENV_PREFIX + "APACHE_NATIVE_IMAGE_NAME";
    protected static final String APACHE_NATIVE_IMAGE_NAME_DEFAULT = "apache/kafka-native";

    @CitrusConfigProperty(description = "Apache Kafka native version.", defaultValue = "4.2.1")
    private static final String APACHE_NATIVE_VERSION_PROPERTY = KAFKA_PROPERTY_PREFIX + "apache.native.version";
    private static final String APACHE_NATIVE_VERSION_ENV = KAFKA_ENV_PREFIX + "APACHE_NATIVE_VERSION";
    private static final String APACHE_NATIVE_VERSION_DEFAULT = APACHE_VERSION_DEFAULT;

    @CitrusConfigProperty(description = "Strimzi Kafka Docker image name.", defaultValue = "quay.io/strimzi/kafka")
    private static final String STRIMZI_IMAGE_NAME_PROPERTY = KAFKA_PROPERTY_PREFIX + "strimzi.image.name";
    private static final String STRIMZI_IMAGE_NAME_ENV = KAFKA_ENV_PREFIX + "STRIMZI_IMAGE_NAME";
    protected static final String STRIMZI_IMAGE_NAME_DEFAULT = "quay.io/strimzi/kafka";

    @CitrusConfigProperty(description = "Strimzi Kafka version.", defaultValue = "0.51.0-kafka-4.2.0")
    private static final String STRIMZI_VERSION_PROPERTY = KAFKA_PROPERTY_PREFIX + "strimzi.version";
    private static final String STRIMZI_VERSION_ENV = KAFKA_ENV_PREFIX + "STRIMZI_VERSION";
    private static final String STRIMZI_VERSION_DEFAULT = "0.51.0-kafka-4.2.0";

    @CitrusConfigProperty(description = "Confluent Kafka Docker image name.", defaultValue = "confluentinc/cp-kafka")
    private static final String CONFLUENT_IMAGE_NAME_PROPERTY = KAFKA_PROPERTY_PREFIX + "confluent.image.name";
    private static final String CONFLUENT_IMAGE_NAME_ENV = KAFKA_ENV_PREFIX + "CONFLUENT_IMAGE_NAME";
    private static final String CONFLUENT_IMAGE_NAME_DEFAULT = "confluentinc/cp-kafka";

    @CitrusConfigProperty(description = "Confluent Kafka version.", defaultValue = "7.9.5")
    private static final String CONFLUENT_VERSION_PROPERTY = KAFKA_PROPERTY_PREFIX + "confluent.version";
    private static final String CONFLUENT_VERSION_ENV = KAFKA_ENV_PREFIX + "CONFLUENT_VERSION";
    private static final String CONFLUENT_VERSION_DEFAULT = "7.9.5";

    @CitrusConfigProperty(description = "Default Kafka Docker image name.", defaultValue = "confluentinc/cp-kafka")
    private static final String IMAGE_NAME_PROPERTY = KAFKA_PROPERTY_PREFIX + "image.name";
    private static final String IMAGE_NAME_ENV = KAFKA_ENV_PREFIX + "IMAGE_NAME";
    private static final String IMAGE_NAME_DEFAULT = CONFLUENT_IMAGE_NAME_DEFAULT;

    @CitrusConfigProperty(description = "Default Kafka version.", defaultValue = "7.9.5")
    private static final String VERSION_PROPERTY = KAFKA_PROPERTY_PREFIX + "version";
    private static final String VERSION_ENV = KAFKA_ENV_PREFIX + "VERSION";
    private static final String VERSION_DEFAULT = CONFLUENT_VERSION_DEFAULT;

    private KafkaSettings() {
        // prevent instantiation of utility class
    }

    /**
     * Kafka image name.
     */
    public static String getImageName(KafkaImplementation implementation) {
        return switch (implementation) {
            case CONFLUENT -> TestContainersSettings.getDockerImageName(System.getProperty(CONFLUENT_IMAGE_NAME_PROPERTY,
                    System.getenv(CONFLUENT_IMAGE_NAME_ENV) != null ? System.getenv(CONFLUENT_IMAGE_NAME_ENV) : CONFLUENT_IMAGE_NAME_DEFAULT));
            case APACHE -> TestContainersSettings.getDockerImageName(System.getProperty(APACHE_IMAGE_NAME_PROPERTY,
                    System.getenv(APACHE_IMAGE_NAME_ENV) != null ? System.getenv(APACHE_IMAGE_NAME_ENV) : APACHE_IMAGE_NAME_DEFAULT));
            case APACHE_NATIVE -> TestContainersSettings.getDockerImageName(System.getProperty(APACHE_NATIVE_IMAGE_NAME_PROPERTY,
                    System.getenv(APACHE_NATIVE_IMAGE_NAME_ENV) != null ? System.getenv(APACHE_NATIVE_IMAGE_NAME_ENV) : APACHE_NATIVE_IMAGE_NAME_DEFAULT));
            case STRIMZI -> TestContainersSettings.getDockerImageName(System.getProperty(STRIMZI_IMAGE_NAME_PROPERTY,
                    System.getenv(STRIMZI_IMAGE_NAME_ENV) != null ? System.getenv(STRIMZI_IMAGE_NAME_ENV) : STRIMZI_IMAGE_NAME_DEFAULT));
            case DEFAULT -> TestContainersSettings.getDockerImageName(System.getProperty(IMAGE_NAME_PROPERTY,
                    System.getenv(IMAGE_NAME_ENV) != null ? System.getenv(IMAGE_NAME_ENV) : IMAGE_NAME_DEFAULT));
        };
    }

    /**
     * Default Kafka image name without any registry prefix.
     */
    public static String getDefaultImageName(KafkaImplementation implementation) {
        return switch (implementation) {
            case CONFLUENT -> CONFLUENT_IMAGE_NAME_DEFAULT;
            case APACHE -> APACHE_IMAGE_NAME_DEFAULT;
            case APACHE_NATIVE -> APACHE_NATIVE_IMAGE_NAME_DEFAULT;
            case STRIMZI -> STRIMZI_IMAGE_NAME_DEFAULT;
            case DEFAULT -> IMAGE_NAME_DEFAULT;
        };
    }

    /**
     * Kafka version setting.
     */
    public static String getKafkaVersion(KafkaImplementation implementation) {
        return switch (implementation) {
            case CONFLUENT -> System.getProperty(CONFLUENT_VERSION_PROPERTY,
                    System.getenv(CONFLUENT_VERSION_ENV) != null ? System.getenv(CONFLUENT_VERSION_ENV) : CONFLUENT_VERSION_DEFAULT);
            case APACHE -> System.getProperty(APACHE_VERSION_PROPERTY,
                    System.getenv(APACHE_VERSION_ENV) != null ? System.getenv(APACHE_VERSION_ENV) : APACHE_VERSION_DEFAULT);
            case APACHE_NATIVE -> System.getProperty(APACHE_NATIVE_VERSION_PROPERTY,
                    System.getenv(APACHE_NATIVE_VERSION_ENV) != null ? System.getenv(APACHE_NATIVE_VERSION_ENV) : APACHE_NATIVE_VERSION_DEFAULT);
            case STRIMZI -> System.getProperty(STRIMZI_VERSION_PROPERTY,
                    System.getenv(STRIMZI_VERSION_ENV) != null ? System.getenv(STRIMZI_VERSION_ENV) : STRIMZI_VERSION_DEFAULT);
            case DEFAULT -> System.getProperty(VERSION_PROPERTY,
                    System.getenv(VERSION_ENV) != null ? System.getenv(VERSION_ENV) : VERSION_DEFAULT);
        };
    }

    /**
     * Kafka service name.
     */
    public static String getServiceName() {
        return System.getProperty(SERVICE_NAME_PROPERTY,
                System.getenv(SERVICE_NAME_ENV) != null ? System.getenv(SERVICE_NAME_ENV) : SERVICE_NAME_DEFAULT);
    }

    /**
     * Kafka container name.
     */
    public static String getContainerName() {
        return System.getProperty(CONTAINER_NAME_PROPERTY,
                System.getenv(CONTAINER_NAME_ENV) != null ? System.getenv(CONTAINER_NAME_ENV) : CONTAINER_NAME_DEFAULT);
    }

    /**
     * Time in seconds to wait for the container to startup and accept connections.
     */
    public static int getStartupTimeout() {
        return Integer.parseInt(System.getProperty(STARTUP_TIMEOUT_PROPERTY,
                System.getenv(STARTUP_TIMEOUT_ENV) != null ? System.getenv(STARTUP_TIMEOUT_ENV) : STARTUP_TIMEOUT_DEFAULT));
    }

    /**
     * Gets the Kafka implementation.
     */
    public static KafkaImplementation getImplementation() {
        return KafkaImplementation.valueOf(System.getProperty(IMPLEMENTATION_PROPERTY,
                System.getenv(IMPLEMENTATION_ENV) != null ? System.getenv(IMPLEMENTATION_ENV) : IMPLEMENTATION_DEFAULT));
    }

    /**
     * Exposes the container connection settings as test variables on the given context.
     * @param container the container holding the connection settings.
     * @param serviceName the service name of the container.
     * @param context the test context to receive the test variables.
     */
    public static void exposeConnectionSettings(GenericContainer<?> container, String serviceName, TestContext context) {
        if (container.getContainerId() != null) {
            String dockerContainerId = container.getContainerId().substring(0, 12);
            String dockerContainerName = container.getContainerName();

            if (dockerContainerName.startsWith("/")) {
                dockerContainerName = dockerContainerName.substring(1);
            }

            String containerType = "KAFKA";
            context.setVariable(getEnvVarName(containerType, "HOST"), container.getHost());
            context.setVariable(getEnvVarName(containerType, "CONTAINER_IP"), container.getHost());
            context.setVariable(getEnvVarName(containerType, "CONTAINER_ID"), dockerContainerId);
            context.setVariable(getEnvVarName(containerType, "CONTAINER_NAME"), dockerContainerName);

            context.setVariable(getEnvVarName(containerType, "SERVICE_PORT"), container.getMappedPort(KafkaSettings.KAFKA_PORT));
            context.setVariable(getEnvVarName(containerType, "PORT"), container.getMappedPort(KafkaSettings.KAFKA_PORT));

            String bootstrapServers;
            if (container instanceof KafkaContainer kafkaContainer) {
                bootstrapServers = kafkaContainer.getBootstrapServers();
            } else if (container instanceof ConfluentKafkaContainer confluentContainer) {
                bootstrapServers = confluentContainer.getBootstrapServers();
            } else if (container instanceof StrimziContainer strimziContainer) {
                bootstrapServers = strimziContainer.getBootstrapServers();
            } else {
                bootstrapServers = "localhost:%d".formatted(container.getMappedPort(KafkaSettings.KAFKA_PORT));
            }

            context.setVariable(getEnvVarName(containerType, "LOCAL_BOOTSTRAP_SERVERS"), bootstrapServers);
            context.setVariable(getEnvVarName(containerType, "SERVICE_LOCAL_BOOTSTRAP_SERVERS"), bootstrapServers);

            if (!KubernetesSupport.isConnected(context) || !TestContainersSettings.isKubedockEnabled()) {
                context.setVariable(getEnvVarName(containerType, "SERVICE_NAME"), serviceName);
                context.setVariable(getEnvVarName(containerType, "SERVICE_BOOTSTRAP_SERVERS"), bootstrapServers);
                context.setVariable(getEnvVarName(containerType, "BOOTSTRAP_SERVERS"), bootstrapServers);
            }

            if (!KubernetesSupport.isConnected(context) || !TestContainersSettings.isKubedockEnabled()) {
                context.setVariable(getEnvVarName(containerType, "SERVICE_NAME"), serviceName);
            } else {
                context.setVariable(getEnvVarName(containerType, "SERVICE_NAME"), serviceName);
                context.setVariable(getEnvVarName(containerType, "SERVICE_BOOTSTRAP_SERVERS"), String.format("%s:%s", serviceName, container.getMappedPort(KafkaSettings.KAFKA_PORT)));
                context.setVariable(getEnvVarName(containerType, "BOOTSTRAP_SERVERS"), String.format("%s:%s", serviceName, container.getMappedPort(KafkaSettings.KAFKA_PORT)));
            }

            context.setVariable(getEnvVarName(containerType, "KUBE_DOCK_SERVICE_BOOTSTRAP_SERVERS"), String.format("%s:%s", serviceName, container.getMappedPort(KafkaSettings.KAFKA_PORT)));
            context.setVariable(getEnvVarName(containerType, "KUBE_DOCK_HOST"), serviceName);
        }
    }
}
