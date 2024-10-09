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

package org.citrusframework.kafka.embedded;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.CoreUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.network.ListenerName;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.coordinator.group.GroupCoordinatorConfig;
import org.apache.kafka.metadata.BrokerState;
import org.apache.kafka.network.SocketServerConfigs;
import org.apache.kafka.server.config.ReplicationConfigs;
import org.apache.kafka.server.config.ServerConfigs;
import org.apache.kafka.server.config.ServerLogConfigs;
import org.apache.kafka.server.config.ZkConfigs;
import org.apache.kafka.storage.internals.log.CleanerConfig;
import org.apache.zookeeper.server.NIOServerCnxnFactory;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.citrusframework.common.InitializingPhase;
import org.citrusframework.common.ShutdownPhase;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

;

/**
 * Embedded Kafka server with reference to embedded Zookeeper cluster for testing purpose. Starts single Zookeeper instance with logs in Java temp directory. Starts single Kafka server
 * and automatically creates given topics with admin client.
 *
 * @since 2.8
 */
public class EmbeddedKafkaServer implements InitializingPhase, ShutdownPhase {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedKafkaServer.class);

    /** Zookeeper embedded server and factory */
    private ZooKeeperServer zookeeper;
    private ServerCnxnFactory serverFactory;

    /** Zookeeper server port */
    private int zookeeperPort = SocketUtils.findAvailableTcpPort();

    /** Kafka server instance */
    private KafkaServer kafkaServer;

    /** Kafka server port */
    private int kafkaServerPort = SocketUtils.findAvailableTcpPort(9092);

    /** Number of partitions to create for each topic */
    private int partitions = 1;

    /** Topics to create on embedded server */
    private String topics = "citrus";

    /** Path to logger directory for Zookeeper server */
    private String logDirPath;

    /** Auto delete logger dir on exit */
    private boolean autoDeleteLogs = true;

    /** Kafka broker server properties */
    private Map<String, String> brokerProperties = Collections.emptyMap();

    /**
     * Start embedded server instances for Kafka and Zookeeper.
     */
    public void start() {
        if (kafkaServer != null) {
            logger.debug("Found instance of Kafka server - avoid duplicate Kafka server startup");
            return;
        }

        File logDir = createLogDir();
        zookeeper = createZookeeperServer(logDir);
        serverFactory = createServerFactory();

        try {
            serverFactory.startup(zookeeper);
        } catch (InterruptedException | IOException e) {
            throw new CitrusRuntimeException("Failed to start embedded zookeeper server", e);
        }

        Properties brokerConfigProperties = createBrokerProperties("localhost:" + zookeeperPort, kafkaServerPort, logDir);
        brokerConfigProperties.setProperty(ReplicationConfigs.REPLICA_SOCKET_TIMEOUT_MS_CONFIG, "1000");
        brokerConfigProperties.setProperty(ReplicationConfigs.CONTROLLER_SOCKET_TIMEOUT_MS_CONFIG, "1000");
        brokerConfigProperties.setProperty(GroupCoordinatorConfig.OFFSETS_TOPIC_REPLICATION_FACTOR_CONFIG, "1");
        brokerConfigProperties.setProperty(ReplicationConfigs.REPLICA_HIGH_WATERMARK_CHECKPOINT_INTERVAL_MS_CONFIG, String.valueOf(Long.MAX_VALUE));

        if (brokerProperties != null) {
            brokerConfigProperties.putAll(brokerProperties);
        }

        kafkaServer = new KafkaServer(new KafkaConfig(brokerConfigProperties),
                Time.SYSTEM,
                scala.Option.apply(null),
                false);
        kafkaServer.startup();
        kafkaServer.boundPort(ListenerName.forSecurityProtocol(SecurityProtocol.PLAINTEXT));

        createKafkaTopics(Arrays.stream(topics.split(",")).collect(Collectors.toSet()));
    }

    /**
     * Shutdown embedded Kafka and Zookeeper server instances
     */
    public void stop() {
        if (kafkaServer != null) {
            try {
                if (kafkaServer.brokerState() != BrokerState.NOT_RUNNING) {
                    kafkaServer.shutdown();
                    kafkaServer.awaitShutdown();
                }
            } catch (Exception e) {
                logger.warn("Failed to shutdown Kafka embedded server", e);
            }

            try {
                CoreUtils.delete(kafkaServer.config().logDirs());
            } catch (Exception e) {
                logger.warn("Failed to remove logs on Kafka embedded server", e);
            }
        }

        if (serverFactory != null) {
            try {
                serverFactory.shutdown();
            } catch (Exception e) {
                logger.warn("Failed to shutdown Zookeeper instance", e);
            }
        }
    }

    @Override
    public void destroy() {
        stop();
    }

    @Override
    public void initialize() {
        start();
    }

    /**
     * Creates new embedded Zookeeper server.
     * @return
     */
    protected ZooKeeperServer createZookeeperServer(File logDir) {
        try {
            return new ZooKeeperServer(logDir, logDir, 2000);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to create embedded zookeeper server", e);
        }
    }

    /**
     * Creates Zookeeper logger directory. By default logs are created in Java temp directory.
     * By default directory is automatically deleted on exit.
     *
     * @return
     */
    protected File createLogDir() {
        File logDir = Optional.ofNullable(logDirPath)
                                    .map(Paths::get)
                                    .map(Path::toFile)
                                    .orElseGet(() -> new File(System.getProperty("java.io.tmpdir")));

        if (!logDir.exists()) {
            if (!logDir.mkdirs()) {
                logger.warn("Unable to create logger directory: " + logDir.getAbsolutePath());
                logDir = new File(System.getProperty("java.io.tmpdir"));
                logger.info("Using default logger directory: " + logDir.getAbsolutePath());
            }
        }

        File logs = new File(logDir, "zookeeper" + System.currentTimeMillis()).getAbsoluteFile();

        if (autoDeleteLogs) {
            logs.deleteOnExit();
        }

        return logs;
    }

    /**
     * Create server factory for embedded Zookeeper server instance.
     * @return
     */
    protected ServerCnxnFactory createServerFactory() {
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
    protected void createKafkaTopics(Set<String> topics) {
        Map<String, Object> adminConfigs = new HashMap<>();
        adminConfigs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + kafkaServerPort);
        try (AdminClient admin = AdminClient.create(adminConfigs)) {
            List<NewTopic> newTopics = topics.stream()
                    .map(t -> new NewTopic(t, partitions, (short) 1))
                    .collect(Collectors.toList());
            CreateTopicsResult createTopics = admin.createTopics(newTopics);
            try {
                createTopics.all().get();
            } catch (Exception e) {
                logger.warn("Failed to create Kafka topics", e);
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
    protected Properties createBrokerProperties(String zooKeeperConnect, int kafkaServerPort, File logDir) {
        Properties props = new Properties();

        props.put(ServerConfigs.BROKER_ID_CONFIG, "0");
        props.put(ZkConfigs.ZK_CONNECT_CONFIG, zooKeeperConnect);
        props.put(ZkConfigs.ZK_CONNECTION_TIMEOUT_MS_CONFIG, "10000");
        props.put(ReplicationConfigs.REPLICA_SOCKET_TIMEOUT_MS_CONFIG, "1500");
        props.put(ReplicationConfigs.CONTROLLER_SOCKET_TIMEOUT_MS_CONFIG, "1500");
        props.put(ServerConfigs.CONTROLLED_SHUTDOWN_ENABLE_CONFIG, "false");
        props.put(ServerConfigs.DELETE_TOPIC_ENABLE_CONFIG, "true");
        props.put(ServerLogConfigs.LOG_DELETE_DELAY_MS_CONFIG, "1000");
        props.put(ServerConfigs.CONTROLLED_SHUTDOWN_RETRY_BACKOFF_MS_CONFIG, "100");
        props.put(CleanerConfig.LOG_CLEANER_DEDUPE_BUFFER_SIZE_PROP, "2097152");
        props.put(TopicConfig.MESSAGE_TIMESTAMP_DIFFERENCE_MAX_MS_CONFIG, Long.MAX_VALUE);
        props.put(GroupCoordinatorConfig.OFFSETS_TOPIC_REPLICATION_FACTOR_CONFIG, "1");
        props.put(GroupCoordinatorConfig.OFFSETS_TOPIC_PARTITIONS_CONFIG, "5");
        props.put(GroupCoordinatorConfig.GROUP_INITIAL_REBALANCE_DELAY_MS_CONFIG, "0");
        props.put(ServerLogConfigs.LOG_DIR_CONFIG, logDir.getAbsolutePath());

        props.put(SocketServerConfigs.LISTENERS_CONFIG, SecurityProtocol.PLAINTEXT.name + "://localhost:" + kafkaServerPort);

        if (logger.isDebugEnabled()) {
            props.forEach((key, value) -> logger.debug(String.format("Using default Kafka broker property %s='%s'", key, value)));
        }

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
     * Gets the kafkaServerPort.
     *
     * @return
     */
    public int getKafkaServerPort() {
        return kafkaServerPort;
    }

    /**
     * Sets the kafkaServerPort.
     *
     * @param kafkaServerPort
     */
    public void setKafkaServerPort(int kafkaServerPort) {
        this.kafkaServerPort = kafkaServerPort;
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
    public String getTopics() {
        return topics;
    }

    /**
     * Sets the topics.
     *
     * @param topics
     */
    public void setTopics(String topics) {
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

    /**
     * Gets the logDirPath.
     *
     * @return
     */
    public String getLogDirPath() {
        return logDirPath;
    }

    /**
     * Sets the logDirPath.
     *
     * @param logDirPath
     */
    public void setLogDirPath(String logDirPath) {
        this.logDirPath = logDirPath;
    }

    /**
     * Gets the autoDeleteLogs.
     *
     * @return
     */
    public boolean isAutoDeleteLogs() {
        return autoDeleteLogs;
    }

    /**
     * Sets the autoDeleteLogs.
     *
     * @param autoDeleteLogs
     */
    public void setAutoDeleteLogs(boolean autoDeleteLogs) {
        this.autoDeleteLogs = autoDeleteLogs;
    }
}
