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

import com.consol.citrus.endpoint.AbstractEndpointBuilder;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.message.MessageCorrelator;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcDbServerBuilder extends AbstractEndpointBuilder<JdbcDbServer> {

    /** Endpoint target */
    private JdbcDbServer endpoint = new JdbcDbServer();

    @Override
    protected JdbcDbServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the autoStart property.
     * @param autoStart
     * @return
     */
    public JdbcDbServerBuilder autoStart(boolean autoStart) {
        endpoint.setAutoStart(autoStart);
        return this;
    }

    /**
     * Sets the serverUrl property.
     * @param serverUrl
     * @return
     */
    public JdbcDbServerBuilder serverUrl(String serverUrl) {
        endpoint.getEndpointConfiguration().setServerUrl(serverUrl);
        return this;
    }

    /**
     * Sets the host property.
     * @param host
     * @return
     */
    public JdbcDbServerBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    /**
     * Sets the port property.
     * @param port
     * @return
     */
    public JdbcDbServerBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    /**
     * Sets the dbName property.
     * @param dbName
     * @return
     */
    public JdbcDbServerBuilder dbName(String dbName) {
        endpoint.getEndpointConfiguration().setDbName(dbName);
        return this;
    }

    /**
     * Sets the autoConnect property.
     * @param autoConnect
     * @return
     */
    public JdbcDbServerBuilder autoConnect(boolean autoConnect) {
        endpoint.getEndpointConfiguration().setAutoConnect(autoConnect);
        return this;
    }

    /**
     * Sets the autoCreateStatement property.
     * @param autoCreateStatement
     * @return
     */
    public JdbcDbServerBuilder autoCreateStatement(boolean autoCreateStatement) {
        endpoint.getEndpointConfiguration().setAutoCreateStatement(autoCreateStatement);
        return this;
    }

    /**
     * Sets the createRegistry property.
     * @param createRegistry
     * @return
     */
    public JdbcDbServerBuilder createRegistry(boolean createRegistry) {
        endpoint.getEndpointConfiguration().setCreateRegistry(createRegistry);
        return this;
    }

    /**
     * Sets the message correlator.
     * @param correlator
     * @return
     */
    public JdbcDbServerBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    /**
     * Sets the endpoint adapter.
     * @param endpointAdapter
     * @return
     */
    public JdbcDbServerBuilder endpointAdapter(EndpointAdapter endpointAdapter) {
        endpoint.setEndpointAdapter(endpointAdapter);
        return this;
    }

    /**
     * Sets the debug logging enabled flag.
     * @param enabled
     * @return
     */
    public JdbcDbServerBuilder debugLogging(boolean enabled) {
        endpoint.setDebugLogging(enabled);
        return this;
    }

    /**
     * Sets the maxConnections.
     * @param maxConnections
     * @return
     */
    public JdbcDbServerBuilder maxConnections(int maxConnections) {
        endpoint.getEndpointConfiguration().setMaxConnections(maxConnections);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public JdbcDbServerBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public JdbcDbServerBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

}
