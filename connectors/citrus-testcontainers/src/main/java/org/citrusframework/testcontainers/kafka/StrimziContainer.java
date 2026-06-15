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

import java.util.UUID;

import org.citrusframework.util.SocketUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers implementation for Strimzi Kafka.
 */
public class StrimziContainer extends GenericContainer<StrimziContainer> {

    private static final String DOCKER_IMAGE_NAME = KafkaSettings.getImageName(KafkaImplementation.STRIMZI);
    private static final String DOCKER_IMAGE_TAG = KafkaSettings.getKafkaVersion(KafkaImplementation.STRIMZI);

    private String serviceName = KafkaSettings.getServiceName();
    private int port = -1;

    public StrimziContainer() {
        this(DOCKER_IMAGE_TAG);
    }

    public StrimziContainer(String version) {
        this(DOCKER_IMAGE_NAME, version);
    }

    public StrimziContainer(String imageName, String version) {
        this(DockerImageName.parse(imageName).withTag(version));
    }

    public StrimziContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
    }

    public StrimziContainer withPort(int port) {
        this.port = port;
        return this;
    }

    public StrimziContainer withServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    @Override
    protected void configure() {
        super.configure();

        String clusterId = UUID.randomUUID().toString().replace("-", "").substring(0, 22);

        withEnv("LOG_DIR", "/tmp/logs")
            .withExposedPorts(KafkaSettings.KAFKA_PORT)
            .withEnv("KAFKA_BROKER_ID", KafkaSettings.NODE_ID)
            .withEnv("KAFKA_NODE_ID", KafkaSettings.NODE_ID)
            .withEnv("KAFKA_PROCESS_ROLES", "broker,controller")
            .withEnv("KAFKA_LISTENERS", "BROKER://0.0.0.0:%d,CONTROLLER://0.0.0.0:%d".formatted(KafkaSettings.KAFKA_PORT, KafkaSettings.CONTROLLER_PORT))
            .withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "BROKER")
            .withEnv("KAFKA_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
            .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "BROKER:PLAINTEXT,CONTROLLER:PLAINTEXT")
            .withEnv("KAFKA_CONTROLLER_QUORUM_VOTERS", "%s@localhost:%d".formatted(KafkaSettings.NODE_ID, KafkaSettings.CONTROLLER_PORT))
            .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
            .withEnv("KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS", "0")
            .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withHostName(serviceName))
            .withCommand("sh", "-c",
                    "bin/kafka-storage.sh format -t " + clusterId + " -c config/server.properties --standalone && "
                            + "bin/kafka-server-start.sh config/server.properties "
                            + "--override listeners=${KAFKA_LISTENERS} "
                            + "--override advertised.listeners=${KAFKA_ADVERTISED_LISTENERS} "
                            + "--override listener.security.protocol.map=${KAFKA_LISTENER_SECURITY_PROTOCOL_MAP} "
                            + "--override inter.broker.listener.name=${KAFKA_INTER_BROKER_LISTENER_NAME} "
                            + "--override controller.listener.names=${KAFKA_CONTROLLER_LISTENER_NAMES} "
                            + "--override controller.quorum.voters=${KAFKA_CONTROLLER_QUORUM_VOTERS} "
                            + "--override broker.id=${KAFKA_BROKER_ID} "
                            + "--override node.id=${KAFKA_NODE_ID} "
                            + "--override process.roles=${KAFKA_PROCESS_ROLES} "
                            + "--override offsets.topic.replication.factor=${KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR} "
                            + "--override transaction.state.log.replication.factor=${KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR} "
                            + "--override transaction.state.log.min.isr=${KAFKA_TRANSACTION_STATE_LOG_MIN_ISR} "
                            + "--override group.initial.rebalance.delay.ms=${KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS}")
            .waitingFor(Wait.forListeningPort());
    }

    @Override
    public void start() {
        withEnv("KAFKA_ADVERTISED_LISTENERS", String.format("BROKER://%s:%d".formatted(getHost(), resolveHostPort())));
        super.start();
    }

    private int resolveHostPort() {
        String suffix = ":" + KafkaSettings.KAFKA_PORT;
        for (String binding : getPortBindings()) {
            if (binding.endsWith(suffix)) {
                return Integer.parseInt(binding.substring(0, binding.indexOf(':')));
            }
        }

        int hostPort;
        if (port > 0) {
            hostPort = port;
        } else {
            hostPort = SocketUtils.findAvailableTcpPort();
        }

        addFixedExposedPort(hostPort, KafkaSettings.KAFKA_PORT);
        return hostPort;
    }
}
