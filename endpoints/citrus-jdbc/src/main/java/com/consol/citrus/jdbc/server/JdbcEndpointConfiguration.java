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

package com.consol.citrus.jdbc.server;

import com.consol.citrus.db.server.JdbcServerConfiguration;
import com.consol.citrus.endpoint.AbstractPollableEndpointConfiguration;
import com.consol.citrus.jdbc.model.JdbcMarshaller;
import com.consol.citrus.message.DefaultMessageCorrelator;
import com.consol.citrus.message.MessageCorrelator;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcEndpointConfiguration extends AbstractPollableEndpointConfiguration {

    /** Jdbc server configuration */
    private JdbcServerConfiguration serverConfiguration = new JdbcServerConfiguration();

    /** Auto accept connection requests */
    private boolean autoConnect = true;

    /** Auto accept create statement requests */
    private boolean autoCreateStatement = true;

    /** Auto accept transaction operations  */
    private boolean autoTransactionHandling = true;

    /** Semicolon separated list of queries that get auto handled for different databases */
    private String[] autoHandleQueries = new String[] {
            "SELECT \\w*", //H2, MySQL, PostgreSQL, SQLite, Microsoft SQL Server
            "SELECT.*FROM DUAL", // Oracle
            "SELECT.*FROM SYSIBM.SYSDUMMY1" // DB2
    };

    /** Marshaller converts from XML to Jdbc model objects */
    private JdbcMarshaller marshaller = new JdbcMarshaller();

    /** Reply message correlator */
    private MessageCorrelator correlator = new DefaultMessageCorrelator();

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
     * Gets the autoHandleQueries.
     *
     * @return
     */
    public String[] getAutoHandleQueries() {
        return autoHandleQueries;
    }

    /**
     * Sets the autoHandleQueries.
     *
     * @param autoHandleQueries
     */
    public void setAutoHandleQueries(String[] autoHandleQueries) {
        this.autoHandleQueries = autoHandleQueries;
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

    /**
     * Gets the serverConfiguration.
     *
     * @return
     */
    public JdbcServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    /**
     * Sets the serverConfiguration.
     *
     * @param serverConfiguration
     */
    public void setServerConfiguration(JdbcServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    /**
     * Gets the autoTransactionHandling.
     *
     * @return Whether to accept transaction commands
     */
    public boolean isAutoTransactionHandling() {
        return autoTransactionHandling;
    }

    /**
     * Sets the autoTransactionHandling.
     *
     * @param autoTransactionHandling Determines whether to accept transaction commands
     */
    public void setAutoTransactionHandling(boolean autoTransactionHandling) {
        this.autoTransactionHandling = autoTransactionHandling;
    }
}
