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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.citrusframework.actions.testcontainers.TestcontainersKafkaStartActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.citrusframework.testcontainers.actions.StartTestcontainersAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class StartKafkaAction<T extends GenericContainer<?>> extends StartTestcontainersAction<T> {

    private static final Logger logger = LoggerFactory.getLogger(StartKafkaAction.class);

    private final Set<String> topics;

    public StartKafkaAction(Builder<T> builder) {
        super(builder);
        this.topics = builder.topics;
    }

    @Override
    protected void exposeConnectionSettings(T container, TestContext context) {
        KafkaSettings.exposeConnectionSettings(container, serviceName, context);

        if (!topics.isEmpty()) {
            createTopics(container, context);
        }
    }

    private void createTopics(T container, TestContext context) {
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

        try (Admin adminClient = Admin.create(Collections.singletonMap(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers))) {
            Set<NewTopic> newTopics = new HashSet<>();
            for (String topic : topics) {
                newTopics.add(new NewTopic(context.replaceDynamicContentInString(topic), 1, (short) 1));
            }

            CreateTopicsResult result = adminClient.createTopics(newTopics);
            result.all().get();

            for (String topic : topics) {
                logger.info("Successfully created Kafka topic: {}", context.replaceDynamicContentInString(topic));
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new CitrusRuntimeException("Failed to create Kafka topics", e);
        }
    }

    /**
     * Action builder.
     */
    public static class Builder<T extends GenericContainer<?>> extends AbstractBuilder<T, StartKafkaAction<T>, Builder<T>>
            implements TestcontainersKafkaStartActionBuilder<T, StartKafkaAction<T>, Builder<T>> {

        private KafkaImplementation implementation = KafkaSettings.getImplementation();
        private String kafkaVersion;
        private int port = -1;
        private final Set<String> topics = new HashSet<>();

        public Builder() {
            withStartupTimeout(KafkaSettings.getStartupTimeout());
        }

        @Override
        public Builder<T> implementation(String implementation) {
           this.implementation = KafkaImplementation.valueOf(implementation.toUpperCase(Locale.US));
           return this;
        }

        public Builder<T> implementation(KafkaImplementation implementation) {
            this.implementation = implementation;
            return this;
        }

        @Override
        public Builder<T> port(int port) {
            this.port = port;
            return this;
        }

        @Override
        public Builder<T> version(String kafkaVersion) {
           this.kafkaVersion = kafkaVersion;
           return this;
        }

        @Override
        public Builder<T> topics(String... topics) {
            this.topics.addAll(Arrays.asList(topics));
            return this;
        }

        @Override
        protected void prepareBuild() {
            if (containerName == null) {
                containerName(KafkaSettings.getContainerName());
            }

            if (serviceName == null) {
                serviceName(KafkaSettings.getServiceName());
            }

            if (image == null) {
                image(KafkaSettings.getImageName(implementation));
            }

            if (kafkaVersion == null) {
                version(KafkaSettings.getKafkaVersion(implementation));
            }

            withLabel("app", "citrus");
            withLabel("com.joyrex2001.kubedock.name-prefix", serviceName);
            withLabel("app.kubernetes.io/name", "kafka");
            withLabel("app.kubernetes.io/part-of", TestContainersSettings.getTestName());
            withLabel("app.openshift.io/connects-to", TestContainersSettings.getTestId());

            Class<?> kafkaContainerType = switch (implementation) {
                case DEFAULT, CONFLUENT -> ConfluentKafkaContainer.class;
                case STRIMZI -> StrimziContainer.class;
                case APACHE, APACHE_NATIVE -> KafkaContainer.class;
            };
            GenericContainer<?> kafkaContainer;
            if (referenceResolver != null && referenceResolver.isResolvable(containerName, kafkaContainerType)) {
                kafkaContainer = (GenericContainer<?>) referenceResolver.resolve(containerName, kafkaContainerType);
            } else {
                DockerImageName imageName;
                if (TestContainersSettings.isRegistryMirrorEnabled()) {
                    // make sure the mirror image is declared as compatible with original image
                    imageName = DockerImageName.parse(image).withTag(kafkaVersion)
                            .asCompatibleSubstituteFor(DockerImageName.parse(image));
                } else {
                    imageName = DockerImageName.parse(image).withTag(kafkaVersion);
                }

                if (port > 0) {
                    kafkaContainer = switch(implementation) {
                        case DEFAULT, CONFLUENT -> new ConfluentKafkaContainer(imageName) {
                            @Override
                            public void start() {
                                addFixedExposedPort(port, KafkaSettings.KAFKA_PORT);
                                super.start();
                            }
                        };
                        case APACHE, APACHE_NATIVE -> new KafkaContainer(imageName) {
                            @Override
                            public void start() {
                                addFixedExposedPort(port, KafkaSettings.KAFKA_PORT);
                                super.start();
                            }
                        };
                        case STRIMZI -> new StrimziContainer(imageName)
                                .withServiceName(serviceName)
                                .withPort(port);
                    };
                } else {
                    kafkaContainer = switch(implementation) {
                        case DEFAULT, CONFLUENT -> new ConfluentKafkaContainer(imageName);
                        case APACHE, APACHE_NATIVE ->  new KafkaContainer(imageName);
                        case STRIMZI ->  new StrimziContainer(imageName)
                                .withServiceName(serviceName);
                    };
                }

                kafkaContainer.withNetwork(network)
                                .withNetworkAliases(serviceName)
                                .withStartupTimeout(startupTimeout);
            }

            container(kafkaContainer);
        }

        @Override
        public StartKafkaAction<T> doBuild() {
            return new StartKafkaAction<>(this);
        }
    }
}
