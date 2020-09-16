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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.consol.citrus.db.driver.dataset.DataSet;
import com.consol.citrus.db.server.JdbcServerException;
import com.consol.citrus.db.server.controller.JdbcController;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.jdbc.data.DataSetCreator;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.message.JdbcMessageHeaders;
import com.consol.citrus.jdbc.model.Execute;
import com.consol.citrus.jdbc.model.OpenConnection;
import com.consol.citrus.jdbc.model.Operation;
import com.consol.citrus.jdbc.model.OperationResult;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.xml.StringResult;
import com.consol.citrus.xml.StringSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcEndpointAdapterController implements JdbcController, EndpointAdapter {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JdbcEndpointAdapterController.class);

    private final JdbcEndpointConfiguration endpointConfiguration;
    private final EndpointAdapter delegate;

    private final DataSetCreator dataSetCreator;

    private AtomicInteger connections = new AtomicInteger(0);
    private boolean transactionState;

    protected static final String AUTO_HANDLE_QUERY_PROPERTY = "citrus.jdbc.auto.handle.query";
    protected static final String AUTO_HANDLE_QUERY_ENV = "CITRUS_JDBC_AUTO_HANDLE_QUERY";

    private Pattern autoHandleQueryPattern;

    /**
     * Default constructor using fields.
     * @param endpointConfiguration The endpoint config for the server
     * @param delegate The endpoint adapter to delegate to
     */
    JdbcEndpointAdapterController(
            JdbcEndpointConfiguration endpointConfiguration,
            EndpointAdapter delegate) {
        this(endpointConfiguration, delegate, new DataSetCreator());
    }

    /**
     * Currently just a constructor for testing purposes
     * @param endpointConfiguration he endpoint config for the server
     * @param delegate The endpoint adapter to delegate to
     * @param dataSetCreator The DataSetCreator to use for DataSetGeneration
     */
    JdbcEndpointAdapterController(
            JdbcEndpointConfiguration endpointConfiguration,
            EndpointAdapter delegate,
            DataSetCreator dataSetCreator) {
        this.endpointConfiguration = endpointConfiguration;
        this.delegate = delegate;
        this.dataSetCreator = dataSetCreator;

        String autoHandleQueries = System.getProperty(AUTO_HANDLE_QUERY_PROPERTY, System.getenv(AUTO_HANDLE_QUERY_ENV) != null ?
                System.getenv(AUTO_HANDLE_QUERY_ENV) : StringUtils.arrayToDelimitedString(endpointConfiguration.getAutoHandleQueries(), ";"));

        List<String> autoQueryPatterns = Arrays.stream(autoHandleQueries.split(";"))
                .map(String::trim)
                .filter(validationQuery -> !StringUtils.isEmpty(validationQuery))
                .map(validationQueryPattern -> "(?i)\\A" + validationQueryPattern + "\\Z")
                .collect(Collectors.toList());
        autoHandleQueryPattern = Pattern.compile(String.join("|", autoQueryPatterns));
    }

    @Override
    public Message handleMessage(Message request) {
        if (request.getPayload() instanceof Operation) {
            StringResult result = new StringResult();
            endpointConfiguration.getMarshaller().marshal(request.getPayload(Operation.class), result);
            request.setPayload(result.toString());
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Received request on server: '%s':%n%s",
                    endpointConfiguration.getServerConfiguration().getDatabaseName(),
                    request.getPayload(String.class)));
        }

        if (request.getPayload(Operation.class) != null) {
            String sqlQuery = Optional.ofNullable(request.getPayload(Operation.class).getExecute())
                                        .map(Execute::getStatement)
                                        .map(Execute.Statement::getSql)
                                        .orElse("");

            if (autoHandleQueryPattern.matcher(sqlQuery).find()) {
                log.debug(String.format("Auto handle query '%s' with positive response", sqlQuery));
                JdbcMessage defaultResponse = JdbcMessage.success().rowsUpdated(0);
                defaultResponse.setType(MessageType.XML);
                return defaultResponse;
            }
        }

        return Optional.ofNullable(delegate.handleMessage(request))
                       .orElse(JdbcMessage.success());
    }

    /**
     * Opens the connection with the given properties
     * @param properties The properties to open the connection with
     * @throws JdbcServerException In case that the maximum connections have been reached
     */
    @Override
    public void openConnection(Map<String, String> properties) throws JdbcServerException {
        if (!endpointConfiguration.isAutoConnect()) {
            List<OpenConnection.Property> propertyList = convertToPropertyList(properties);
            handleMessageAndCheckResponse(JdbcMessage.openConnection(propertyList));
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
    public void createPreparedStatement(String stmt) throws JdbcServerException {
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
    public DataSet executeQuery(String query) throws JdbcServerException {
        log.info("Received execute query request: " + query);
        Message response = handleMessageAndCheckResponse(JdbcMessage.execute(query));
        return dataSetCreator.createDataSet(response, getMessageType(response));
    }

    /**
     * Executes the given statement
     * @param stmt The statement to be executed
     * @throws JdbcServerException In case that the execution was not successful
     */
    @Override
    public DataSet executeStatement(String stmt) throws JdbcServerException {
        log.info("Received execute statement request: " + stmt);
        Message response = handleMessageAndCheckResponse(JdbcMessage.execute(stmt));
        return dataSetCreator.createDataSet(response, getMessageType(response));
    }

    /**
     * Executes the given update
     * @param updateSql The update statement to be executed
     * @throws JdbcServerException In case that the execution was not successful
     */
    @Override
    public int executeUpdate(String updateSql) throws JdbcServerException {
        log.info("Received execute update request: " + updateSql);
        Message response = handleMessageAndCheckResponse(JdbcMessage.execute(updateSql));
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
    public void setTransactionState(boolean transactionState) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Received transaction state change: '%s':%n%s",
                    endpointConfiguration.getServerConfiguration().getDatabaseName(),
                    String.valueOf(transactionState)));
        }

        this.transactionState = transactionState;
        if(!endpointConfiguration.isAutoTransactionHandling() && transactionState){
            handleMessageAndCheckResponse(JdbcMessage.startTransaction());
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

        if(!endpointConfiguration.isAutoTransactionHandling()){
            handleMessageAndCheckResponse(JdbcMessage.commitTransaction());
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

        if(!endpointConfiguration.isAutoTransactionHandling()){
            handleMessageAndCheckResponse(JdbcMessage.rollbackTransaction());
        }
    }

    /**
     * Creates a callable statement
     */
    @Override
    public void createCallableStatement(String sql) {
        if (!endpointConfiguration.isAutoCreateStatement()) {
            handleMessageAndCheckResponse(JdbcMessage.createCallableStatement(sql));
        }
    }

    /**
     * Determines the MessageType of the given response
     * @param response The response to get the message type from
     * @return The MessageType of the response
     */
    private MessageType getMessageType(Message response) {
        String messageTypeString = response.getType();
        if (MessageType.knows(messageTypeString)){
            return MessageType.valueOf(messageTypeString.toUpperCase());
        }
        return null;
    }

    /**
     * Converts a property map propertyKey -> propertyValue to a list of OpenConnection.Properties
     * @param properties The map to convert
     * @return A list of Properties
     */
    private List<OpenConnection.Property> convertToPropertyList(Map<String, String> properties) {
        return properties.entrySet()
                .stream()
                .map(this::convertToProperty)
                .sorted(Comparator.comparingInt(OpenConnection.Property::hashCode))
                .collect(Collectors.toList());
    }

    /**
     * Converts a Map entry into a OpenConnection.Property
     * @param entry The entry to convert
     * @return the OpenConnection.Property representation
     */
    private OpenConnection.Property convertToProperty(Map.Entry<String, String> entry) {
        OpenConnection.Property property = new OpenConnection.Property();
        property.setName(entry.getKey());
        property.setValue(entry.getValue());
        return property;
    }

    /**
     * Handle request message and check response is successful.
     * @param request The request message to handle
     * @return The response Message
     * @throws JdbcServerException Thrown when the response has some exception header.
     */
    private Message handleMessageAndCheckResponse(Message request) throws JdbcServerException {
        Message response = handleMessage(request);
        checkSuccess(response);
        return response;
    }

    /**
     * Check that response is not having an exception message.
     * @param response The response message to check
     * @throws JdbcServerException In case the message contains a error.
     */
    private void checkSuccess(Message response) throws JdbcServerException {
        OperationResult operationResult = null;
        if (response instanceof JdbcMessage || response.getPayload() instanceof OperationResult) {
            operationResult = response.getPayload(OperationResult.class);
        } else if (response.getPayload() != null && StringUtils.hasText(response.getPayload(String.class))) {
            operationResult = (OperationResult) endpointConfiguration.getMarshaller().unmarshal(new StringSource(response.getPayload(String.class)));
        }

        if (!success(response, operationResult)) {
            throw new JdbcServerException(getExceptionMessage(response, operationResult));
        }
    }

    private String getExceptionMessage(Message response, OperationResult operationResult) {
        return Optional.ofNullable(response.getHeader(JdbcMessageHeaders.JDBC_SERVER_EXCEPTION))
                        .map(Object::toString)
                        .orElse(Optional.ofNullable(operationResult).map(OperationResult::getException).orElse(""));
    }

    private boolean success(Message response, OperationResult result) {
        return Optional.ofNullable(response.getHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS))
                .map(Object::toString)
                .map(Boolean::valueOf)
                .orElse(Optional.ofNullable(result).map(OperationResult::isSuccess).orElse(true));
    }

    @Override
    public Endpoint getEndpoint() {
        return delegate.getEndpoint();
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration() {
        return delegate.getEndpointConfiguration();
    }

    AtomicInteger getConnections() {
        return connections;
    }
}
