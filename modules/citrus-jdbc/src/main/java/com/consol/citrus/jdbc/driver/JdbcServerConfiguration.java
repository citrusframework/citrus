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

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcServerConfiguration extends AbstractPollableEndpointConfiguration implements ApplicationContextAware {

    /** Rmi connection parameters */
    private String host;
    private int port = 4567;
    private String databaseName;

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
     * Gets the databaseName.
     *
     * @return
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * Sets the databaseName.
     *
     * @param databaseName
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public MessageCorrelator getCorrelator() {
        return correlator;
    }

    public void setCorrelator(MessageCorrelator correlator) {
        this.correlator = correlator;
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
