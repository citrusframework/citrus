/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.kafka.embedded;

import java.util.Map;

/**
 * Embedded Kafka server with reference to embedded Zookeeper cluster for testing purpose. Starts single Zookeeper instance with logs in Java temp directory. Starts single Kafka server
 * and automatically creates given topics with admin client.
 *
 * @author Christoph Deppisch
 * @since 2.8
 */
public class EmbeddedKafkaServerBuilder {

    /** Kafka server instance to build */
    private final EmbeddedKafkaServer kafkaServer;

    /**
     * Default constructor.
     */
    public EmbeddedKafkaServerBuilder() {
        this(new EmbeddedKafkaServer());
    }

    /**
     * Constructor using Kafka server.
     * @param kafkaServer
     */
    public EmbeddedKafkaServerBuilder(EmbeddedKafkaServer kafkaServer) {
        this.kafkaServer = kafkaServer;
    }

    /**
     * Sets the Kafka server port
     * @param port
     * @return
     */
    public EmbeddedKafkaServerBuilder kafkaServerPort(int port) {
        kafkaServer.setKafkaServerPort(port);
        return this;
    }

    /**
     * Sets the Zookeeper server port
     * @param port
     * @return
     */
    public EmbeddedKafkaServerBuilder zookeeperPort(int port) {
        kafkaServer.setZookeeperPort(port);
        return this;
    }

    /**
     * Sets the topics to auto create on server as comma delimited list.
     * @param topics
     * @return
     */
    public EmbeddedKafkaServerBuilder topics(String topics) {
        kafkaServer.setTopics(topics);
        return this;
    }

    /**
     * Sets the topics to auto create on embedded server.
     * @param topics
     * @return
     */
    public EmbeddedKafkaServerBuilder topics(String ... topics) {
        return topics(String.join(",", topics));
    }

    /**
     * Sets the number of partitions to create for each topic.
     * @param count
     * @return
     */
    public EmbeddedKafkaServerBuilder partitions(int count) {
        kafkaServer.setPartitions(count);
        return this;
    }

    /**
     * Sets the kafka server broker properties.
     * @param properties
     * @return
     */
    public EmbeddedKafkaServerBuilder brokerProperties(Map<String, String> properties) {
        kafkaServer.setBrokerProperties(properties);
        return this;
    }

    /**
     * Sets the Zookeeper logger directory path.
     * @param logDirPath
     * @return
     */
    public EmbeddedKafkaServerBuilder logDirPath(String logDirPath) {
        kafkaServer.setLogDirPath(logDirPath);
        return this;
    }

    /**
     * Sets the auto delete option for Zookeeper logs.
     * @param autoDelete
     * @return
     */
    public EmbeddedKafkaServerBuilder autoDeleteLogs(boolean autoDelete) {
        kafkaServer.setAutoDeleteLogs(autoDelete);
        return this;
    }

    /**
     * Builds the kafkaServer.
     * @return
     */
    public EmbeddedKafkaServer build() {
        return kafkaServer;
    }
}
