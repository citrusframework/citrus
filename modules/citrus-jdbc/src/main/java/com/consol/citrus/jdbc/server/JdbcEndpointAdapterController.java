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

import com.consol.citrus.db.driver.dataset.DataSet;
import com.consol.citrus.db.driver.json.JsonDataSetProducer;
import com.consol.citrus.db.driver.xml.XmlDataSetProducer;
import com.consol.citrus.db.server.JdbcServerException;
import com.consol.citrus.db.server.controller.JdbcController;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jdbc.command.JdbcCommand;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.message.JdbcMessageHeaders;
import com.consol.citrus.jdbc.model.OpenConnection;
import com.consol.citrus.jdbc.model.Operation;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.xml.transform.StringResult;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcEndpointAdapterController implements JdbcController, EndpointAdapter {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JdbcEndpointAdapterController.class);

    private final JdbcEndpointConfiguration endpointConfiguration;
    private final EndpointAdapter delegate;

    private AtomicInteger connections = new AtomicInteger(0);
    private boolean transactionState;

    /**
     * Default constructor using fields.
     * @param endpointConfiguration The endpoint config for the server
     * @param delegate The endpoint adapter to delegate to
     */
    JdbcEndpointAdapterController(
            final JdbcEndpointConfiguration endpointConfiguration,
            final EndpointAdapter delegate) {
        this.endpointConfiguration = endpointConfiguration;
        this.delegate = delegate;
    }

    @Override
    public Message handleMessage(final Message request) {
        if (request.getPayload() instanceof Operation) {
            final StringResult result = new StringResult();
            endpointConfiguration.getMarshaller().marshal(request.getPayload(Operation.class), result);
            request.setPayload(result.toString());
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Received request on server: '%s':%n%s",
                    endpointConfiguration.getServerConfiguration().getDatabaseName(),
                    request.getPayload(String.class)));
        }

        return Optional.ofNullable(delegate.handleMessage(request))
                .orElse(JdbcMessage.result(true));
    }

    /**
     * Handle request message and check response is successful.
     * @param request The request message to handle
     * @return The response Message
     * @throws JdbcServerException Thrown when the response has some exception header.
     */
    private Message handleMessageAndCheckResponse(final Message request) throws JdbcServerException {
        final Message response = handleMessage(request);
        checkSuccess(response);
        return response;
    }

    /**
     * Opens the connection with the given properties
     * @param properties The properties to open the connection with
     * @throws JdbcServerException In case that the maximum connections have been reached
     */
    @Override
    public void openConnection(final Map<String, String> properties) throws JdbcServerException {
        if (!endpointConfiguration.isAutoConnect()) {
            handleMessageAndCheckResponse(JdbcMessage.openConnection(properties.entrySet()
                    .stream()
                    .map(entry -> new OpenConnection.Property(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList())));
        }

        if (connections.get() == endpointConfiguration.getServerConfiguration().getMaxConnections()) {
            throw new JdbcServerException(String.format("Maximum number of connections (%s) reached",
                    endpointConfiguration.getServerConfiguration().getMaxConnections()));
        }

        connections.incrementAndGet();
    }

    /**
     * Closes the connection
     * @throws JdbcServerException In case that the connection could not be closed
     */
    @Override
    public void closeConnection() throws JdbcServerException {
        if (!endpointConfiguration.isAutoConnect()) {
            handleMessageAndCheckResponse(JdbcMessage.closeConnection());
        }

        if (connections.decrementAndGet() < 0) {
            connections.set(0);
        }
    }

    /**
     * Creates a prepared statement
     * @param stmt The statement to create
     * @throws JdbcServerException In case that the statement was not successful
     */
    @Override
    public void createPreparedStatement(final String stmt) throws JdbcServerException {
        if (!endpointConfiguration.isAutoCreateStatement()) {
            handleMessageAndCheckResponse(JdbcMessage.createPreparedStatement(stmt));
        }
    }

    /**
     * Creates a statement
     * @throws JdbcServerException In case that the statement was not successfully created
     */
    @Override
    public void createStatement() throws JdbcServerException {
        if (!endpointConfiguration.isAutoCreateStatement()) {
            handleMessageAndCheckResponse(JdbcMessage.createStatement());
        }
    }

    /**
     * Executes a given query and returns the mapped result
     * @param query The query to execute
     * @return The DataSet containing the query result
     * @throws JdbcServerException In case that the query was not successful
     */
    @Override
    public DataSet executeQuery(final String query) throws JdbcServerException {
        log.info("Received execute query request: " + query);
        return createDataSet(handleMessageAndCheckResponse(JdbcMessage.execute(query)));
    }

    /**
     * Executes the given statement
     * @param stmt The statement to be executed
     * @throws JdbcServerException In case that the execution was not successful
     */
    @Override
    public void execute(final String stmt) throws JdbcServerException {
        log.info("Received execute statement request: " + stmt);
        handleMessageAndCheckResponse(JdbcMessage.execute(stmt));
    }

    /**
     * Executes the given update
     * @param updateSql The update statement to be executed
     * @throws JdbcServerException In case that the execution was not successful
     */
    @Override
    public int executeUpdate(final String updateSql) throws JdbcServerException {
        log.info("Received execute update request: " + updateSql);
        final Message response = handleMessageAndCheckResponse(JdbcMessage.execute(updateSql));
        return Optional.ofNullable(
                response.getHeader(JdbcMessageHeaders.JDBC_ROWS_UPDATED))
                .map(Object::toString).map(Integer::valueOf)
                .orElse(0);
    }

    /**
     * Closes the connection
     * @throws JdbcServerException In case that the connection could not be closed
     */
    @Override
    public void closeStatement() throws JdbcServerException {
        if (!endpointConfiguration.isAutoCreateStatement()) {
            handleMessageAndCheckResponse(JdbcMessage.closeStatement());
        }
    }

    /**
     * Sets the transaction state of the database connection
     * @param transactionState The boolean value whether the server is in transaction state.
     */
    @Override
    public void setTransactionState(final boolean transactionState) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Received transaction state change: '%s':%n%s",
                    endpointConfiguration.getServerConfiguration().getDatabaseName(),
                    String.valueOf(transactionState)));
        }

        this.transactionState = transactionState;
        if(!endpointConfiguration.isAutoTransactions() && transactionState){
            handleMessageAndCheckResponse(JdbcCommand.startTransaction());
        }
    }

    /**
     * Returns the transaction state
     * @return The transaction state of the connection
     */
    @Override
    public boolean getTransactionState() {
        return this.transactionState;
    }

    /**
     * Commits the transaction statements
     */
    @Override
    public void commitStatements() {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Received transaction commit: '%s':%n",
                    endpointConfiguration.getServerConfiguration().getDatabaseName()));
        }

        if(!endpointConfiguration.isAutoTransactions()){
            handleMessageAndCheckResponse(JdbcCommand.commitTransaction());
        }
    }

    /**
     * Performs a rollback on the current transaction
     */
    @Override
    public void rollbackStatements() {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Received transaction rollback: '%s':%n",
                    endpointConfiguration.getServerConfiguration().getDatabaseName()));
        }

        if(!endpointConfiguration.isAutoTransactions()){
            handleMessageAndCheckResponse(JdbcCommand.rollbackTransaction());
        }
    }

    /**
     * Converts Citrus result set representation to db driver model result set.
     * @param response The result set to convert
     * @return A DataSet the jdbc driver can understand
     */
    private DataSet createDataSet(final Message response) {
        try {
            if (response.getPayload() instanceof DataSet) {
                return response.getPayload(DataSet.class);
            } else if (response.getPayload() != null) {
                if (endpointConfiguration.getMarshaller().getType().equalsIgnoreCase(MessageType.JSON.name())) {
                    return new JsonDataSetProducer(response.getPayload(String.class)).produce();
                } else if (endpointConfiguration.getMarshaller().getType().equalsIgnoreCase(MessageType.XML.name())) {
                    return new XmlDataSetProducer(response.getPayload(String.class)).produce();
                } else {
                    throw new CitrusRuntimeException("Unable to create dataset from data type " +
                            endpointConfiguration.getMarshaller().getType());
                }
            } else {
                return new DataSet();
            }
        } catch (final SQLException e) {
            throw new CitrusRuntimeException("Failed to read dataset from response message", e);
        }
    }

    /**
     * Check that response is not having an exception message.
     * @param response The response message to check
     * @throws JdbcServerException In case the message contains a error.
     */
    private void checkSuccess(final Message response) throws JdbcServerException {
        if (!success(response)) {
            throw new JdbcServerException(
                    Optional.ofNullable(
                            response.getHeader(JdbcMessageHeaders.JDBC_SERVER_EXCEPTION))
                            .map(Object::toString).orElse(""));
        }
    }

    private boolean success(final Message response) {
        return Optional.ofNullable(
                response.getHeader(
                        JdbcMessageHeaders.JDBC_SERVER_SUCCESS))
                .map(Object::toString)
                .map(Boolean::valueOf)
                .orElse(true);
    }

    @Override
    public Endpoint getEndpoint() {
        return delegate.getEndpoint();
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration() {
        return delegate.getEndpointConfiguration();
    }
}
