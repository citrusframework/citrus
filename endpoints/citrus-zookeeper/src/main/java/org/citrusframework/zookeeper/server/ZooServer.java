/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.zookeeper.server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.server.AbstractServer;
import org.apache.zookeeper.server.NIOServerCnxnFactory;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;

/**
 * Simple ZooKeeper server standalone instance.
 *
 * @author Christoph Deppisch
 * @since 2.6
 */
public class ZooServer extends AbstractServer {

    private int port = 21181;
    private ZooKeeperServer zooKeeperServer;
    private ServerCnxnFactory serverFactory;

    @Override
    protected void startup() {
        try {
            getServerFactory().startup(getZooKeeperServer());
        } catch (InterruptedException | IOException e) {
            throw new CitrusRuntimeException("Failed to start zookeeper server", e);
        }
    }

    @Override
    protected void shutdown() {
        getServerFactory().shutdown();
    }

    /**
     * Gets the value of the serverFactory property.
     *
     * @return the serverFactory
     */
    public ServerCnxnFactory getServerFactory() {
        if (serverFactory == null) {
            try {
                serverFactory = new NIOServerCnxnFactory();
                serverFactory.configure(new InetSocketAddress(port), 5000);
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to create default zookeeper server factory", e);
            }
        }

        return serverFactory;
    }

    /**
     * Sets the serverFactory property.
     *
     * @param serverFactory
     */
    public void setServerFactory(ServerCnxnFactory serverFactory) {
        this.serverFactory = serverFactory;
    }

    /**
     * Gets the value of the zooKeeperServer property.
     *
     * @return the zooKeeperServer
     */
    public ZooKeeperServer getZooKeeperServer() {
        if (zooKeeperServer == null) {
            System.setProperty(FileTxnSnapLog.ZOOKEEPER_SNAPSHOT_TRUST_EMPTY, Boolean.TRUE.toString());
            String dataDirectory = System.getProperty("java.io.tmpdir");
            File dir = new File(dataDirectory, "zookeeper").getAbsoluteFile();
            try {
                zooKeeperServer = new ZooKeeperServer(dir, dir, 2000);
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to create default zookeeper server", e);
            }
        }

        return zooKeeperServer;
    }

    /**
     * Sets the zooKeeperServer property.
     *
     * @param zooKeeperServer
     */
    public void setZooKeeperServer(ZooKeeperServer zooKeeperServer) {
        this.zooKeeperServer = zooKeeperServer;
    }

    /**
     * Gets the value of the port property.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port property.
     *
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }
}
