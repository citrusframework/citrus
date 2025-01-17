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

import org.citrusframework.context.TestContext;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.citrusframework.testcontainers.actions.StartTestcontainersAction;
import org.citrusframework.testcontainers.postgresql.PostgreSQLSettings;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class StartKafkaAction extends StartTestcontainersAction<KafkaContainer> {

    public StartKafkaAction(Builder builder) {
        super(builder);
    }

    @Override
    protected void exposeConnectionSettings(KafkaContainer container, TestContext context) {
        KafkaSettings.exposeConnectionSettings(container, serviceName, context);
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractBuilder<KafkaContainer, StartKafkaAction, Builder> {

        private String kafkaVersion = KafkaSettings.getKafkaVersion();

        public Builder() {
            withStartupTimeout(PostgreSQLSettings.getStartupTimeout());
        }

        public Builder version(String kafkaVersion) {
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
                image(KafkaSettings.getImageName());
            }

            withLabel("app", "citrus");
            withLabel("com.joyrex2001.kubedock.name-prefix", serviceName);
            withLabel("app.kubernetes.io/name", "kafka");
            withLabel("app.kubernetes.io/part-of", TestContainersSettings.getTestName());
            withLabel("app.openshift.io/connects-to", TestContainersSettings.getTestId());

            KafkaContainer kafkaContainer;
            if (referenceResolver != null && referenceResolver.isResolvable(containerName, KafkaContainer.class)) {
                kafkaContainer = referenceResolver.resolve(containerName, KafkaContainer.class);
            } else {
                DockerImageName imageName;
                if (TestContainersSettings.isRegistryMirrorEnabled()) {
                    // make sure the mirror image is declared as compatible with original image
                    imageName = DockerImageName.parse(image).withTag(kafkaVersion)
                            .asCompatibleSubstituteFor(DockerImageName.parse("confluentinc/cp-kafka"));
                } else {
                    imageName = DockerImageName.parse(image).withTag(kafkaVersion);
                }

                kafkaContainer = new KafkaContainer(imageName)
                        .withNetwork(network)
                        .withNetworkAliases(serviceName)
                        .withStartupTimeout(startupTimeout);
            }

            container(kafkaContainer);
        }

        @Override
        public StartKafkaAction doBuild() {
            return new StartKafkaAction(this);
        }
    }
}
