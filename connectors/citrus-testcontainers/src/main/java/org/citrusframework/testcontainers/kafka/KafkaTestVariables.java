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

import org.citrusframework.api.yaml.TestVariable;
import org.citrusframework.testcontainers.TestContainersSettings;

public enum KafkaTestVariables implements TestVariable {
    HOST("HOST", "Container host address."),
    CONTAINER_IP("CONTAINER_IP", "Container IP address."),
    CONTAINER_ID("CONTAINER_ID", "Docker container ID (first 12 characters)."),
    CONTAINER_NAME("CONTAINER_NAME", "Docker container name."),
    PORT("PORT", "Mapped container port."),
    SERVICE_PORT("SERVICE_PORT", "Mapped Kafka service port."),
    LOCAL_BOOTSTRAP_SERVERS("LOCAL_BOOTSTRAP_SERVERS", "Kafka bootstrap servers using localhost."),
    SERVICE_LOCAL_BOOTSTRAP_SERVERS("SERVICE_LOCAL_BOOTSTRAP_SERVERS", "Kafka bootstrap servers using localhost (service variant)."),
    SERVICE_NAME("SERVICE_NAME", "Service name of the container."),
    SERVICE_BOOTSTRAP_SERVERS("SERVICE_BOOTSTRAP_SERVERS", "Kafka bootstrap servers using the service name."),
    BOOTSTRAP_SERVERS("BOOTSTRAP_SERVERS", "Kafka bootstrap servers connection string."),
    KUBE_DOCK_SERVICE_BOOTSTRAP_SERVERS("KUBE_DOCK_SERVICE_BOOTSTRAP_SERVERS", "Kafka bootstrap servers for KubeDock service access."),
    KUBE_DOCK_HOST("KUBE_DOCK_HOST", "KubeDock service host name.");

    private static final String VARIABLE_PREFIX = TestContainersSettings.TESTCONTAINERS_VARIABLE_PREFIX + "KAFKA_";

    private final String suffix;
    private final String description;

    KafkaTestVariables(String suffix, String description) {
        this.suffix = suffix;
        this.description = description;
    }

    @Override
    public String variableName() {
        return VARIABLE_PREFIX + suffix;
    }

    @Override
    public String description() {
        return description;
    }
}
