/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.jdbc.server;

import com.consol.citrus.server.AbstractServer;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcServer extends AbstractServer {

    /** Endpoint configuration */
    private final JdbcEndpointConfiguration endpointConfiguration;

    /** Controller handling requests */
    private JdbcEndpointAdapterController controller;

    /** JDBC server delegate */
    private com.consol.citrus.db.server.JdbcServer jdbcServer;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public JdbcServer() {
        this(new JdbcEndpointConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public JdbcServer(JdbcEndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public JdbcEndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    @Override
    protected void startup() {
        controller = new JdbcEndpointAdapterController(getEndpointConfiguration(), getEndpointAdapter());
        this.jdbcServer = new com.consol.citrus.db.server.JdbcServer(controller, endpointConfiguration.getServerConfiguration());

        jdbcServer.startAndAwaitInitialization();
    }

    @Override
    protected void shutdown() {
        jdbcServer.stop();
    }
}
