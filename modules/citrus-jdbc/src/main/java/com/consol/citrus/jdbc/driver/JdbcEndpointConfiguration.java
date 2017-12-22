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

package com.consol.citrus.jdbc.driver;

import com.consol.citrus.endpoint.AbstractPollableEndpointConfiguration;
import com.consol.citrus.jdbc.model.JdbcMarshaller;
import com.consol.citrus.message.DefaultMessageCorrelator;
import com.consol.citrus.message.MessageCorrelator;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcEndpointConfiguration extends AbstractPollableEndpointConfiguration implements ApplicationContextAware {

    /** Rmi server url */
    private String serverUrl;

    /** Rmi connection parameters */
    private String host;
    private int port = Registry.REGISTRY_PORT;
    private String dbName;

    /** RMI registry */
    private Registry registry;

    /** Should server automatically create service registry */
    private boolean createRegistry = false;

    /** Auto accept connection requests */
    private boolean autoConnect = true;
    /** Auto accept create statement requests */
    private boolean autoCreateStatement = true;

    /** Maximum number of parallel connections */
    private int maxConnections = 20;

    /** Marshaller converts from XML to Jdbc model objects */
    private JdbcMarshaller marshaller = new JdbcMarshaller();

    /** Reply message correlator */
    private MessageCorrelator correlator = new DefaultMessageCorrelator();

    /** Spring application context used for method arg object reference evaluation */
    private ApplicationContext applicationContext;

    /**
     * Gets the RMI registry based on host and port settings in this configuration.
     * @return
     * @throws RemoteException
     */
    public Registry getRegistry() throws RemoteException {
        if (registry == null) {
            if (StringUtils.hasText(host)) {
                registry = LocateRegistry.getRegistry(host, port);
            } else {
                registry = LocateRegistry.getRegistry(port);
            }
        }

        return registry;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;

        this.host = JdbcEndpointUtils.getHost(serverUrl.substring("rmi://".length()));
        this.port = JdbcEndpointUtils.getPort(serverUrl.substring("rmi://".length()), getPort());
        this.dbName = JdbcEndpointUtils.getBinding(serverUrl.substring("rmi://".length()));
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the dbName.
     *
     * @return
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * Sets the dbName.
     *
     * @param dbName
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public MessageCorrelator getCorrelator() {
        return correlator;
    }

    public void setCorrelator(MessageCorrelator correlator) {
        this.correlator = correlator;
    }

    public boolean isCreateRegistry() {
        return createRegistry;
    }

    public void setCreateRegistry(boolean createRegistry) {
        this.createRegistry = createRegistry;
    }

    /**
     * Gets the autoConnect.
     *
     * @return
     */
    public boolean isAutoConnect() {
        return autoConnect;
    }

    /**
     * Sets the autoConnect.
     *
     * @param autoConnect
     */
    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    /**
     * Gets the autoCreateStatement.
     *
     * @return
     */
    public boolean isAutoCreateStatement() {
        return autoCreateStatement;
    }

    /**
     * Sets the autoCreateStatement.
     *
     * @param autoCreateStatement
     */
    public void setAutoCreateStatement(boolean autoCreateStatement) {
        this.autoCreateStatement = autoCreateStatement;
    }

    /**
     * Gets the maxConnections.
     *
     * @return
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * Sets the maxConnections.
     *
     * @param maxConnections
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Gets the marshaller.
     *
     * @return
     */
    public JdbcMarshaller getMarshaller() {
        return marshaller;
    }

    /**
     * Sets the marshaller.
     *
     * @param marshaller
     */
    public void setMarshaller(JdbcMarshaller marshaller) {
        this.marshaller = marshaller;
    }
}
