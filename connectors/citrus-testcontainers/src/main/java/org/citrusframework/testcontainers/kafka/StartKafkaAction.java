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

import org.citrusframework.actions.testcontainers.TestcontainersKafkaStartActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.citrusframework.testcontainers.actions.StartTestcontainersAction;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class StartKafkaAction<T extends GenericContainer<?>> extends StartTestcontainersAction<T> {

    public StartKafkaAction(Builder<T> builder) {
        super(builder);
    }

    @Override
    protected void exposeConnectionSettings(T container, TestContext context) {
        KafkaSettings.exposeConnectionSettings(container, serviceName, context);
    }

    /**
     * Action builder.
     */
    public static class Builder<T extends GenericContainer<?>> extends AbstractBuilder<T, StartKafkaAction<T>, Builder<T>>
            implements TestcontainersKafkaStartActionBuilder<T, StartKafkaAction<T>, Builder<T>> {

        private KafkaImplementation implementation = KafkaSettings.getImplementation();
        private String kafkaVersion;
        private int port = -1;

        public Builder() {
            withStartupTimeout(KafkaSettings.getStartupTimeout());
        }

        @Override
        public Builder<T> implementation(String implementation) {
           this.implementation = KafkaImplementation.valueOf(implementation);
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
                case APACHE -> KafkaContainer.class;
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
                        case APACHE -> new KafkaContainer(imageName) {
                            @Override
                            public void start() {
                                addFixedExposedPort(port, KafkaSettings.KAFKA_PORT);
                                super.start();
                            }
                        };
                    };
                } else {
                    kafkaContainer = switch(implementation) {
                        case DEFAULT, CONFLUENT -> new ConfluentKafkaContainer(imageName);
                        case APACHE ->  new KafkaContainer(imageName);
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
