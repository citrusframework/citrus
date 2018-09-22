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
import kafka.metrics.KafkaMetricsReporter;
import kafka.server.*;
import kafka.utils.CoreUtils;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.network.ListenerName;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.utils.Time;
import org.apache.zookeeper.server.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
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
    private ZooKeeperServer zookeeper;
    private ServerCnxnFactory serverFactory;

    /** Running Kafka server instance */
    private KafkaServer kafkaServer;

    /** Kafka server port */
    private int zookeeperPort = 21181;

    /** Kafka server port */
    private int port = 9092;

    /** Number of partitions to create for each topic */
    private int partitions = 1;

    /** Topics to create on embedded server */
    private Set<String> topics = Collections.singleton("citrus");

    /** Kafka broker server properties */
    private Map<String, String> brokerProperties = Collections.emptyMap();

    /**
     * Start embedded server instances for Kafka and Zookeeper.
     */
    public void start() {
        File logDir = createLogDir();
        zookeeper = createZookeeperServer(logDir);
        serverFactory = createServerFactory();

        try {
            serverFactory.startup(zookeeper);
        } catch (InterruptedException | IOException e) {
            throw new CitrusRuntimeException("Failed to start embedded zookeeper server", e);
        }

        Properties brokerConfigProperties = createBrokerProperties("localhost:" + zookeeperPort, port, logDir);
        brokerConfigProperties.setProperty(KafkaConfig.ReplicaSocketTimeoutMsProp(), "1000");
        brokerConfigProperties.setProperty(KafkaConfig.ControllerSocketTimeoutMsProp(), "1000");
        brokerConfigProperties.setProperty(KafkaConfig.OffsetsTopicReplicationFactorProp(), "1");
        brokerConfigProperties.setProperty(KafkaConfig.ReplicaHighWatermarkCheckpointIntervalMsProp(), String.valueOf(Long.MAX_VALUE));

        if (brokerProperties != null) {
            brokerProperties.forEach(brokerConfigProperties::put);
        }

        kafkaServer = new KafkaServer(new KafkaConfig(brokerConfigProperties), Time.SYSTEM,
                scala.Option.apply(null), scala.collection.JavaConversions.asScalaBuffer(Collections.<KafkaMetricsReporter>emptyList()).toList());
        kafkaServer.startup();
        kafkaServer.boundPort(ListenerName.forSecurityProtocol(SecurityProtocol.PLAINTEXT));

        createKafkaTopics(topics);
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

        if (serverFactory != null) {
            try {
                serverFactory.shutdown();
            } catch (Exception e) {
                log.warn("Failed to shutdown Zookeeper instance", e);
            }
        }
    }

    /**
     * Creates new embedded Zookeeper server.
     * @return
     */
    private ZooKeeperServer createZookeeperServer(File logDir) {
        try {
            return new ZooKeeperServer(logDir, logDir, 2000);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to create embedded zookeeper server", e);
        }
    }

    private File createLogDir() {
        String dataDirectory = System.getProperty("java.io.tmpdir");
        return new File(dataDirectory, "zookeeper").getAbsoluteFile();
    }

    /**
     * Create server factory for embedded Zookeeper server instance.
     * @return
     */
    private ServerCnxnFactory createServerFactory() {
        try {
            ServerCnxnFactory serverFactory = new NIOServerCnxnFactory();
            serverFactory.configure(new InetSocketAddress(zookeeperPort), 5000);
            return serverFactory;
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to create default zookeeper server factory", e);
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
                    .map(t -> new NewTopic(t, partitions, (short) 1))
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
     * @param logDir
     * @return
     */
    private Properties createBrokerProperties(String zooKeeperConnect, int kafkaServerPort, File logDir) {
        Properties props = new Properties();

        props.put(KafkaConfig.BrokerIdProp(), "0");
        props.put(KafkaConfig.ZkConnectProp(), zooKeeperConnect);
        props.put(KafkaConfig.ZkConnectionTimeoutMsProp(), "10000");
        props.put(KafkaConfig.ReplicaSocketTimeoutMsProp(), "1500");
        props.put(KafkaConfig.ControllerSocketTimeoutMsProp(), "1500");
        props.put(KafkaConfig.ControlledShutdownEnableProp(), "false");
        props.put(KafkaConfig.DeleteTopicEnableProp(), "true");
        props.put(KafkaConfig.LogDeleteDelayMsProp(), "1000");
        props.put(KafkaConfig.ControlledShutdownRetryBackoffMsProp(), "100");
        props.put(KafkaConfig.LogCleanerDedupeBufferSizeProp(), "2097152");
        props.put(KafkaConfig.LogMessageTimestampDifferenceMaxMsProp(), Long.MAX_VALUE);
        props.put(KafkaConfig.OffsetsTopicReplicationFactorProp(), "1");
        props.put(KafkaConfig.OffsetsTopicPartitionsProp(), "5");
        props.put(KafkaConfig.GroupInitialRebalanceDelayMsProp(), "0");
        props.put(KafkaConfig.LogDirProp(), logDir.getAbsolutePath());

        props.put(KafkaConfig.ListenersProp(), SecurityProtocol.PLAINTEXT.name + "://localhost:" + kafkaServerPort);

        props.forEach((key, value) -> System.out.println(key + "=" + value));

        return props;
    }

    /**
     * Gets the zookeeperPort.
     *
     * @return
     */
    public int getZookeeperPort() {
        return zookeeperPort;
    }

    /**
     * Sets the zookeeperPort.
     *
     * @param zookeeperPort
     */
    public void setZookeeperPort(int zookeeperPort) {
        this.zookeeperPort = zookeeperPort;
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
