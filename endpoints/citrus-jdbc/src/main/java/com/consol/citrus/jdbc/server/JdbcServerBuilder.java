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

import com.consol.citrus.db.server.JdbcServerConfiguration;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.server.AbstractServerBuilder;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcServerBuilder extends AbstractServerBuilder<JdbcServer, JdbcServerBuilder> {

    /** Endpoint target */
    private final JdbcServer endpoint = new JdbcServer();

    @Override
    protected JdbcServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the host property.
     * @param host
     * @return
     */
    public JdbcServerBuilder host(String host) {
        endpoint.getEndpointConfiguration().getServerConfiguration().setHost(host);
        return this;
    }

    /**
     * Sets the port property.
     * @param port
     * @return
     */
    public JdbcServerBuilder port(int port) {
        endpoint.getEndpointConfiguration().getServerConfiguration().setPort(port);
        return this;
    }

    /**
     * Sets the database name property.
     * @param name
     * @return
     */
    public JdbcServerBuilder databaseName(String name) {
        endpoint.getEndpointConfiguration().getServerConfiguration().setDatabaseName(name);
        return this;
    }

    /**
     * Sets the autoConnect property.
     * @param autoConnect
     * @return
     */
    public JdbcServerBuilder autoConnect(boolean autoConnect) {
        endpoint.getEndpointConfiguration().setAutoConnect(autoConnect);
        return this;
    }

    /**
     * Sets the autoCreateStatement property.
     * @param autoCreateStatement
     * @return
     */
    public JdbcServerBuilder autoCreateStatement(boolean autoCreateStatement) {
        endpoint.getEndpointConfiguration().setAutoCreateStatement(autoCreateStatement);
        return this;
    }

    /**
     * Sets the autoHandleQueries property.
     * @param autoHandleQueries
     * @return
     */
    public JdbcServerBuilder autoHandleQueries(String ... autoHandleQueries) {
        endpoint.getEndpointConfiguration().setAutoHandleQueries(autoHandleQueries);
        return this;
    }

    /**
     * Sets the message correlator.
     * @param correlator
     * @return
     */
    public JdbcServerBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    /**
     * Sets the maxConnections.
     * @param maxConnections
     * @return
     */
    public JdbcServerBuilder maxConnections(int maxConnections) {
        endpoint.getEndpointConfiguration().getServerConfiguration().setMaxConnections(maxConnections);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public JdbcServerBuilder pollingInterval(long pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the autoTransactionHandling property.
     * @param autoTransactionHandling Determines whether to auto accept transaction
     * @return The builder
     */
    public JdbcServerBuilder autoTransactionHandling(final boolean autoTransactionHandling) {
        endpoint.getEndpointConfiguration().setAutoTransactionHandling(autoTransactionHandling);
        return this;
    }

    /**
     * Sets the serverConfiguration property.
     * @param serverConfiguration to set
     * @return The builder
     */
    public JdbcServerBuilder serverConfiguration(final JdbcServerConfiguration serverConfiguration) {
        endpoint.getEndpointConfiguration().setServerConfiguration(serverConfiguration);
        return this;
    }

}
