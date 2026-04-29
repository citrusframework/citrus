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

import java.io.File;
import java.io.IOException;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import kafka.server.BrokerServer;
import kafka.server.ControllerServer;
import kafka.server.KafkaConfig;
import kafka.server.KafkaRaftServer;
import kafka.server.Server;
import kafka.server.SharedServer;
import kafka.server.StandardFaultHandlerFactory;
import kafka.utils.CoreUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.network.ListenerName;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.coordinator.group.GroupCoordinatorConfig;
import org.apache.kafka.metadata.BrokerState;
import org.apache.kafka.metadata.properties.MetaProperties;
import org.apache.kafka.metadata.properties.MetaPropertiesEnsemble;
import org.apache.kafka.metadata.properties.MetaPropertiesVersion;
import org.apache.kafka.metadata.properties.PropertiesUtils;
import org.apache.kafka.network.SocketServerConfigs;
import org.apache.kafka.raft.QuorumConfig;
import org.apache.kafka.server.ServerSocketFactory;
import org.apache.kafka.server.config.KRaftConfigs;
import org.apache.kafka.server.config.ReplicationConfigs;
import org.apache.kafka.server.config.ServerConfigs;
import org.apache.kafka.server.config.ServerLogConfigs;
import org.apache.kafka.storage.internals.log.CleanerConfig;
import org.citrusframework.common.InitializingPhase;
import org.citrusframework.common.ShutdownPhase;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Embedded Kafka server with reference to embedded controller for testing purpose. Starts single controller instance with logs in Java temp directory. Starts single Kafka server
 * and automatically creates given topics with admin client.
 *
 * @since 2.8
 */
public class EmbeddedKafkaServer implements InitializingPhase, ShutdownPhase {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedKafkaServer.class);

    /** Kafka server instance */
    private BrokerServer kafkaServer;
    private ControllerServer controllerServer;

    /** Controller port */
    private int controllerPort = SocketUtils.findAvailableTcpPort();

    /** Kafka server port */
    private int kafkaServerPort = SocketUtils.findAvailableTcpPort(9092);

    /** Number of partitions to create for each topic */
    private int partitions = 1;

    /** Topics to create on embedded server */
    private String topics = "citrus";

    /** Path to logger directory for Kafka server */
    private String logDirPath;

    /** Auto delete logger dir on exit */
    private boolean autoDeleteLogs = true;

    /** Kafka broker server properties */
    private Map<String, String> brokerProperties = Collections.emptyMap();

    /**
     * Start embedded server instances for Kafka and controller.
     */
    public void start() {
        if (kafkaServer != null) {
            logger.debug("Found instance of Kafka server - avoid duplicate Kafka server startup");
            return;
        }

        int nodeId = 1;
        File logDir = createLogDir();

        Properties brokerConfigProperties = createBrokerProperties(String.valueOf(nodeId), controllerPort, kafkaServerPort, logDir);
        brokerConfigProperties.setProperty(ReplicationConfigs.REPLICA_SOCKET_TIMEOUT_MS_CONFIG, "1000");
        brokerConfigProperties.setProperty(ReplicationConfigs.CONTROLLER_SOCKET_TIMEOUT_MS_CONFIG, "1000");
        brokerConfigProperties.setProperty(GroupCoordinatorConfig.OFFSETS_TOPIC_REPLICATION_FACTOR_CONFIG, "1");
        brokerConfigProperties.setProperty(ReplicationConfigs.REPLICA_HIGH_WATERMARK_CHECKPOINT_INTERVAL_MS_CONFIG, String.valueOf(Long.MAX_VALUE));

        if (brokerProperties != null) {
            brokerConfigProperties.putAll(brokerProperties);
        }

        KafkaConfig kafkaConfig = new KafkaConfig(brokerConfigProperties);

        String clusterId = UUID.randomUUID().toString();
        var metaProperties = new MetaProperties.Builder()
                .setVersion(MetaPropertiesVersion.V1)
                .setClusterId(clusterId)
                .setNodeId(nodeId)
                .build();

        try {
            var metaPropertiesFile = Paths.get(kafkaConfig.metadataLogDir()).toAbsolutePath()
                    .resolve(MetaPropertiesEnsemble.META_PROPERTIES_NAME).toFile();
            PropertiesUtils.writePropertiesFile(
                    metaProperties.toProperties(),
                    metaPropertiesFile.getAbsolutePath(),
                    false
            );
        } catch (IOException exception) {
            throw new CitrusRuntimeException("Failed to write meta properties for the embedded Kafka server", exception);
        }

        var metaData = KafkaRaftServer.initializeLogDirs(kafkaConfig, logger, "[KafkaRaftServer nodeId=%s] ".formatted(kafkaConfig.nodeId()));

        var metrics = Server.initializeMetrics(
            kafkaConfig,
            Time.SYSTEM,
            clusterId
        );

        SharedServer sharedServer = new SharedServer(kafkaConfig, metaData._1(), Time.SYSTEM, metrics,
                CompletableFuture.completedFuture(QuorumConfig.parseVoterConnections(kafkaConfig.quorumConfig().voters())),
                QuorumConfig.parseBootstrapServers(kafkaConfig.quorumConfig().bootstrapServers()),
                new StandardFaultHandlerFactory(), ServerSocketFactory.INSTANCE);

        controllerServer = new ControllerServer(sharedServer, KafkaRaftServer.configSchema(), metaData._2());
        kafkaServer = new BrokerServer(sharedServer);

        // Controller component must be started before the broker component so that the controller endpoints are passed to the KRaft manager
        controllerServer.startup();
        kafkaServer.startup();

        kafkaServer.boundPort(ListenerName.normalised("BROKER"));

        createKafkaTopics(Arrays.stream(topics.split(",")).collect(Collectors.toSet()));
    }

    /**
     * Shutdown embedded Kafka and Controller server instances
     */
    public void stop() {
        if (kafkaServer != null) {
            try {
                if (kafkaServer.brokerState() != BrokerState.NOT_RUNNING) {
                    try {
                        kafkaServer.shutdown();
                    } catch (Exception e) {
                        logger.warn("Failed to shutdown Kafka embedded server", e);
                    }
                }

                if (controllerServer != null) {
                    try {
                        controllerServer.shutdown();
                    } catch (Exception e) {
                        logger.warn("Failed to shutdown Controller instance", e);
                    }
                }

                kafkaServer.awaitShutdown();
                if (controllerServer != null) {
                    controllerServer.awaitShutdown();
                }
            } finally {
                try {
                    CoreUtils.delete(kafkaServer.config().logDirs());
                } catch (Exception e) {
                    logger.warn("Failed to remove logs on Kafka embedded server", e);
                }
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
     * Creates Kafka server logger directory. By default, logs are created in Java temp directory.
     * By default, directory is automatically deleted on exit.
     */
    protected File createLogDir() {
        File logDir = Optional.ofNullable(logDirPath)
                                    .map(Paths::get)
                                    .map(Path::toFile)
                                    .orElseGet(() -> new File(System.getProperty("java.io.tmpdir")));

        if (!logDir.exists()) {
            if (!logDir.mkdirs()) {
                logger.warn("Unable to create logger directory: {}", logDir.getAbsolutePath());
                logDir = new File(System.getProperty("java.io.tmpdir"));
                logger.info("Using default logger directory: {}", logDir.getAbsolutePath());
            }
        }

        File logs = new File(logDir, "embedded-kafka" + System.currentTimeMillis()).getAbsoluteFile();
        if (!logs.mkdirs()) {
            logger.warn("Unable to create logger directory: {}", logs.getAbsolutePath());
        }

        if (autoDeleteLogs) {
            logs.deleteOnExit();
        }

        return logs;
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
     */
    protected Properties createBrokerProperties(String nodeId, int controllerPort, int kafkaServerPort, File logDir) {
        Properties props = new Properties();

        props.put(KRaftConfigs.PROCESS_ROLES_CONFIG, "broker,controller");
        props.put(KRaftConfigs.NODE_ID_CONFIG, nodeId);
        props.put(ServerConfigs.BROKER_ID_CONFIG, nodeId);

        props.put(ReplicationConfigs.INTER_BROKER_LISTENER_NAME_CONFIG, "BROKER");
        props.put(KRaftConfigs.CONTROLLER_LISTENER_NAMES_CONFIG, "CONTROLLER");

        props.put(SocketServerConfigs.LISTENERS_CONFIG, "BROKER://localhost:%d,CONTROLLER://localhost:%d".formatted(kafkaServerPort, controllerPort));
        props.put(SocketServerConfigs.ADVERTISED_LISTENERS_CONFIG, "BROKER://localhost:%d".formatted(kafkaServerPort));
        props.put(SocketServerConfigs.LISTENER_SECURITY_PROTOCOL_MAP_CONFIG, "BROKER:PLAINTEXT,CONTROLLER:PLAINTEXT");

        props.put(QuorumConfig.QUORUM_VOTERS_CONFIG, "%s@localhost:%d".formatted(nodeId, controllerPort));

        // The total memory used for log deduplication across all cleaner threads, keep it small to not exhaust suite memory
        props.put(CleanerConfig.LOG_CLEANER_DEDUPE_BUFFER_SIZE_PROP, "2097152");

        props.put(ReplicationConfigs.REPLICA_SOCKET_TIMEOUT_MS_CONFIG, "1500");
        props.put(ReplicationConfigs.CONTROLLER_SOCKET_TIMEOUT_MS_CONFIG, "1500");
        props.put(ServerConfigs.CONTROLLED_SHUTDOWN_ENABLE_CONFIG, "false");
        props.put(ServerConfigs.DELETE_TOPIC_ENABLE_CONFIG, "true");
        props.put(ServerLogConfigs.LOG_DELETE_DELAY_MS_CONFIG, "1000");
        props.put(TopicConfig.MESSAGE_TIMESTAMP_BEFORE_MAX_MS_CONFIG, Long.MAX_VALUE);
        props.put(TopicConfig.MESSAGE_TIMESTAMP_AFTER_MAX_MS_CONFIG, Long.MAX_VALUE);
        props.put(GroupCoordinatorConfig.OFFSETS_TOPIC_REPLICATION_FACTOR_CONFIG, "1");
        props.put(GroupCoordinatorConfig.OFFSETS_TOPIC_PARTITIONS_CONFIG, "5");
        props.put(GroupCoordinatorConfig.GROUP_INITIAL_REBALANCE_DELAY_MS_CONFIG, "0");

        props.put(ServerLogConfigs.LOG_DIR_CONFIG, logDir.getAbsolutePath());
        props.put(ServerLogConfigs.LOG_DIRS_CONFIG, logDir.getAbsolutePath());
        props.put(ServerLogConfigs.AUTO_CREATE_TOPICS_ENABLE_CONFIG, "true");
        props.put(ServerLogConfigs.LOG_FLUSH_INTERVAL_MESSAGES_CONFIG, "1");

        if (logger.isDebugEnabled()) {
            props.forEach((key, value) -> logger.debug("Using default Kafka broker property {}='{}'", key, value));
        }

        return props;
    }

    /**
     * Gets the controllerPort.
     */
    public int getControllerPort() {
        return controllerPort;
    }

    /**
     * Sets the controllerPort.
     */
    public void setControllerPort(int controllerPort) {
        this.controllerPort = controllerPort;
    }

    /**
     * Gets the kafkaServerPort.
     */
    public int getKafkaServerPort() {
        return kafkaServerPort;
    }

    /**
     * Sets the kafkaServerPort.
     */
    public void setKafkaServerPort(int kafkaServerPort) {
        this.kafkaServerPort = kafkaServerPort;
    }

    /**
     * Gets the partitions.
     */
    public int getPartitions() {
        return partitions;
    }

    /**
     * Sets the partitions.
     */
    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    /**
     * Gets the topics.
     */
    public String getTopics() {
        return topics;
    }

    /**
     * Sets the topics.
     */
    public void setTopics(String topics) {
        this.topics = topics;
    }

    /**
     * Gets the brokerProperties.
     */
    public Map<String, String> getBrokerProperties() {
        return brokerProperties;
    }

    /**
     * Sets the brokerProperties.
     */
    public void setBrokerProperties(Map<String, String> brokerProperties) {
        this.brokerProperties = brokerProperties;
    }

    /**
     * Gets the logDirPath.
     */
    public String getLogDirPath() {
        return logDirPath;
    }

    /**
     * Sets the logDirPath.
     */
    public void setLogDirPath(String logDirPath) {
        this.logDirPath = logDirPath;
    }

    /**
     * Gets the autoDeleteLogs.
     */
    public boolean isAutoDeleteLogs() {
        return autoDeleteLogs;
    }

    /**
     * Sets the autoDeleteLogs.
     */
    public void setAutoDeleteLogs(boolean autoDeleteLogs) {
        this.autoDeleteLogs = autoDeleteLogs;
    }
}
