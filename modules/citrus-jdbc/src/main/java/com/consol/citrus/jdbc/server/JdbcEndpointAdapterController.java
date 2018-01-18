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
import com.consol.citrus.endpoint.*;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.message.JdbcMessageHeaders;
import com.consol.citrus.jdbc.model.*;
import com.consol.citrus.message.*;
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

    /**
     * Default constructor using fields.
     * @param endpointConfiguration
     * @param delegate
     */
    public JdbcEndpointAdapterController(JdbcEndpointConfiguration endpointConfiguration, EndpointAdapter delegate) {
        this.endpointConfiguration = endpointConfiguration;
        this.delegate = delegate;
    }

    @Override
    public Message handleMessage(Message request) {
        if (request.getPayload() instanceof Operation) {
            StringResult result = new StringResult();
            endpointConfiguration.getMarshaller().marshal(request.getPayload(Operation.class), result);
            request.setPayload(result.toString());
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Received request on server: '%s':%n%s", endpointConfiguration.getServerConfiguration().getDatabaseName(), request.getPayload(String.class)));
        }

        return Optional.ofNullable(delegate.handleMessage(request))
                .orElse(JdbcMessage.result(true));
    }

    /**
     * Handle request message and check response is successful. When response has some exception header set the
     * exception is thrown as {@link JdbcServerException} in order to abort request processing with error.
     * @param request
     * @return
     * @throws JdbcServerException
     */
    private Message handleMessageAndCheckResponse(Message request) throws JdbcServerException {
        Message response = handleMessage(request);
        checkSuccess(response);
        return response;
    }

        @Override
    public void getConnection(Map<String, String> properties) throws JdbcServerException {
        if (!endpointConfiguration.isAutoConnect()) {
            handleMessageAndCheckResponse(JdbcMessage.openConnection(properties.entrySet()
                    .stream()
                    .map(entry -> new OpenConnection.Property(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList())));
        }

        if (connections.get() == endpointConfiguration.getServerConfiguration().getMaxConnections()) {
            throw new JdbcServerException(String.format("Maximum number of connections (%s) reached", endpointConfiguration.getServerConfiguration().getMaxConnections()));
        }

        connections.incrementAndGet();
    }

    @Override
    public void closeConnection() throws JdbcServerException {
        if (!endpointConfiguration.isAutoConnect()) {
            handleMessageAndCheckResponse(JdbcMessage.closeConnection());
        }

        if (connections.decrementAndGet() < 0) {
            connections.set(0);
        }
    }

    @Override
    public void createPreparedStatement(String stmt) throws JdbcServerException {
        if (!endpointConfiguration.isAutoCreateStatement()) {
            handleMessageAndCheckResponse(JdbcMessage.createPreparedStatement(stmt));
        }
    }

    @Override
    public void createStatement() throws JdbcServerException {
        if (!endpointConfiguration.isAutoCreateStatement()) {
            handleMessageAndCheckResponse(JdbcMessage.createStatement());
        }
    }

    @Override
    public DataSet executeQuery(String stmt) throws JdbcServerException {
        log.info("Received execute query request: " + stmt);
        return createDataSet(handleMessageAndCheckResponse(JdbcMessage.execute(stmt)));
    }

    @Override
    public void execute(String stmt) throws JdbcServerException {
        log.info("Received execute statement request: " + stmt);
        handleMessageAndCheckResponse(JdbcMessage.execute(stmt));
    }

    @Override
    public int executeUpdate(String stmt) throws JdbcServerException {
        log.info("Received execute update request: " + stmt);
        Message response = handleMessageAndCheckResponse(JdbcMessage.execute(stmt));
        return Optional.ofNullable(response.getHeader(JdbcMessageHeaders.JDBC_ROWS_UPDATED)).map(Object::toString).map(Integer::valueOf).orElse(0);
    }

    @Override
    public void closeStatement() throws JdbcServerException {
        if (!endpointConfiguration.isAutoCreateStatement()) {
            handleMessageAndCheckResponse(JdbcMessage.closeStatement());
        }
    }

    /**
     * Converts Citrus result set representation to db driver model result set.
     * @param response
     * @return
     */
    private DataSet createDataSet(Message response) {
        try {
            if (response.getPayload() instanceof DataSet) {
                return response.getPayload(DataSet.class);
            } else if (response.getPayload() != null) {
                if (endpointConfiguration.getMarshaller().getType().equalsIgnoreCase(MessageType.JSON.name())) {
                    return new JsonDataSetProducer(response.getPayload(String.class)).produce();
                } else if (endpointConfiguration.getMarshaller().getType().equalsIgnoreCase(MessageType.XML.name())) {
                    return new XmlDataSetProducer(response.getPayload(String.class)).produce();
                } else {
                    throw new CitrusRuntimeException("Unable to create dataset from data type " + endpointConfiguration.getMarshaller().getType());
                }
            } else {
                return new DataSet();
            }
        } catch (SQLException e) {
            throw new CitrusRuntimeException("Failed to read dataset from response message", e);
        }
    }

    /**
     * Check that response is not having an exception message.
     * @param response
     * @throws JdbcServerException
     */
    private void checkSuccess(Message response) throws JdbcServerException {
        if (!Optional.ofNullable(response.getHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS)).map(Object::toString).map(Boolean::valueOf).orElse(true)) {
            throw new JdbcServerException(Optional.ofNullable(response.getHeader(JdbcMessageHeaders.JDBC_SERVER_EXCEPTION)).map(Object::toString).orElse(""));
        }
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
