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

package com.consol.citrus.kafka.embedded;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import kafka.server.*;
import kafka.utils.*;
import kafka.zk.EmbeddedZookeeper;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.utils.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Christoph Deppisch
 * @since 2.8
 */
public class EmbeddedKafkaServer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(EmbeddedKafkaServer.class);

    /** Zookeeper embedded server and client */
    private EmbeddedZookeeper zookeeper;
    private ZkClient zookeeperClient;

    /** Running Kafka server instance */
    private KafkaServer kafkaServer;

    /** Kafka server port */
    private int port = 9092;

    /** Number of partitions to create for each topic */
    private int partitions = 1;

    /** Topics to create on embedded server */
    private Set<String> topics = Collections.singleton("citrus");

    /** Kafka broker server properties */
    private Map<String, String> brokerProperties = Collections.emptyMap();

    public static void main(String[] args) {
        EmbeddedKafkaServer server = new EmbeddedKafkaServer();
        server.start();
    }

    /**
     * Start embedded server instances for Kafka and Zookeeper.
     */
    public void start() {
        this.zookeeper = new EmbeddedZookeeper();

        String zooKeeperConnect = "localhost:" + this.zookeeper.port();

        zookeeperClient = new ZkClient(zooKeeperConnect, 6000, 6000,
                ZKStringSerializer$.MODULE$);

        Properties brokerConfigProperties = createBrokerProperties(zooKeeperConnect, port);
        brokerConfigProperties.setProperty(KafkaConfig.PortProp(), Integer.toString(port));
        brokerConfigProperties.setProperty(KafkaConfig.DeleteTopicEnableProp(), "true");
        brokerConfigProperties.setProperty(KafkaConfig.ReplicaSocketTimeoutMsProp(), "1000");
        brokerConfigProperties.setProperty(KafkaConfig.ControllerSocketTimeoutMsProp(), "1000");
        brokerConfigProperties.setProperty(KafkaConfig.OffsetsTopicReplicationFactorProp(), "1");
        brokerConfigProperties.setProperty(KafkaConfig.ReplicaHighWatermarkCheckpointIntervalMsProp(), String.valueOf(Long.MAX_VALUE));

        if (this.brokerProperties != null) {
            this.brokerProperties.forEach(brokerConfigProperties::put);
        }

        kafkaServer = TestUtils.createServer(new KafkaConfig(brokerConfigProperties), Time.SYSTEM);
        TestUtils.boundPort(kafkaServer, SecurityProtocol.PLAINTEXT);

        createKafkaTopics(this.topics);
    }

    /**
     * Shutdown embedded Kafka and Zookeeper server instances
     */
    public void stop() {
        if (kafkaServer != null) {
            try {
                if (kafkaServer.brokerState().currentState() != (NotRunning.state())) {
                    kafkaServer.shutdown();
                    kafkaServer.awaitShutdown();
                }
            } catch (Exception e) {
                log.warn("Failed to shutdown Kafka embedded server", e);
            }

            try {
                CoreUtils.delete(kafkaServer.config().logDirs());
            } catch (Exception e) {
                log.warn("Failed to remove logs on Kafka embedded server", e);
            }
        }

        if (zookeeperClient != null) {
            try {
                this.zookeeperClient.close();
            } catch (ZkInterruptedException e) {
                log.warn("Failed to close Zookeeper client", e);
            }
        }

        if (zookeeper != null) {
            try {
                this.zookeeper.shutdown();
            } catch (Exception e) {
                log.warn("Failed to shutdown Zookeeper instance", e);
            }
        }
    }

    /**
     * Create topics on embedded Kafka server.
     * @param topics
     */
    private void createKafkaTopics(Set<String> topics) {
        Map<String, Object> adminConfigs = new HashMap<>();
        adminConfigs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + port);
        try (AdminClient admin = AdminClient.create(adminConfigs)) {
            List<NewTopic> newTopics = topics.stream()
                    .map(t -> new NewTopic(t, this.partitions, (short) 1))
                    .collect(Collectors.toList());
            CreateTopicsResult createTopics = admin.createTopics(newTopics);
            try {
                createTopics.all().get();
            } catch (Exception e) {
                throw new CitrusRuntimeException("Failed to create Kafka topics", e);
            }
        }
    }

    /**
     * Creates Kafka broker properties.
     * @param zooKeeperConnect
     * @param kafkaServerPort
     * @return
     */
    private Properties createBrokerProperties(String zooKeeperConnect, int kafkaServerPort) {
        return TestUtils.createBrokerConfig(0, zooKeeperConnect, false,true, kafkaServerPort,
                scala.Option.apply(null),
                scala.Option.apply(null),
                scala.Option.apply(null),
                true, false, 0, false, 0, false, 0, scala.Option.apply(null), 1, false);
    }

    /**
     * Gets the port.
     *
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port.
     *
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the partitions.
     *
     * @return
     */
    public int getPartitions() {
        return partitions;
    }

    /**
     * Sets the partitions.
     *
     * @param partitions
     */
    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    /**
     * Gets the topics.
     *
     * @return
     */
    public Set<String> getTopics() {
        return topics;
    }

    /**
     * Sets the topics.
     *
     * @param topics
     */
    public void setTopics(Set<String> topics) {
        this.topics = topics;
    }

    /**
     * Gets the brokerProperties.
     *
     * @return
     */
    public Map<String, String> getBrokerProperties() {
        return brokerProperties;
    }

    /**
     * Sets the brokerProperties.
     *
     * @param brokerProperties
     */
    public void setBrokerProperties(Map<String, String> brokerProperties) {
        this.brokerProperties = brokerProperties;
    }
}
