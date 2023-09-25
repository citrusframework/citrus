/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.zookeeper.client;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Zookeeper client used for executing zookeeper commands.
 *
 * @author Martin Maher
 * @since 2.5
 */
public class ZooClient {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ZooClient.class);

    /** ZooKeeper client */
    private ZooKeeper zookeeper;

    /** Zookeeper config */
    private ZooClientConfig zookeeperConfig;

    /**
     * Default constructor.
     */
    public ZooClient() {
        super();
    }

    /**
     * Constructor using zookeeper client instance.
     * @param zookeeper
     */
    public ZooClient(ZooKeeper zookeeper) {
        this();
        this.zookeeper = zookeeper;
    }

    /**
     * Creates a new Zookeeper client instance with configuration.
     * @return
     */
    private ZooKeeper createZooKeeperClient() throws IOException {
        ZooClientConfig config = getZookeeperClientConfig();
        return new ZooKeeper(config.getUrl(), config.getTimeout(), getConnectionWatcher());
    }

    /**
     * Constructs or gets the zookeeper client implementation.
     * @return
     */
    public ZooKeeper getZooKeeperClient() {
        if (zookeeper == null) {
            try {
                zookeeper = createZooKeeperClient();
                int retryAttempts = 5;
                while(!zookeeper.getState().isConnected() && retryAttempts > 0) {
                    logger.debug("connecting...");
                    retryAttempts--;
                    Thread.sleep(1000);
                }
            } catch (IOException | InterruptedException e) {
                throw new CitrusRuntimeException(e);
            }

        }

        return zookeeper;
    }

    /**
     * Gets the zookeeper client configuration.
     * @return
     */
    public ZooClientConfig getZookeeperClientConfig() {
        if (zookeeperConfig == null) {
            zookeeperConfig = ZooClientConfig.createDefaultConfigBuilder().build();
        }

        return zookeeperConfig;
    }

    /**
     * Sets the zookeeper client configuration.
     * @param zookeeperConfig
     */
    public void setZookeeperClientConfig(ZooClientConfig zookeeperConfig) {
        this.zookeeperConfig = zookeeperConfig;
    }

    private Watcher getConnectionWatcher() {
        return new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                logger.debug(String.format("Connection Event: %s", event.toString()));
            }
        };
    }
}
