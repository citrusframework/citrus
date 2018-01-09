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

import com.consol.citrus.endpoint.*;
import com.consol.citrus.jdbc.driver.JdbcServerConfiguration;
import com.consol.citrus.jdbc.model.*;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.xml.transform.StringResult;

import javax.xml.transform.Source;
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

    private final JdbcServerConfiguration endpointConfiguration;
    private final EndpointAdapter delegate;

    private AtomicInteger connections = new AtomicInteger(0);

    /**
     * Default constructor using fields.
     * @param endpointConfiguration
     * @param delegate
     */
    public JdbcEndpointAdapterController(JdbcServerConfiguration endpointConfiguration, EndpointAdapter delegate) {
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
            log.debug(String.format("Received request on server: '%s':%n%s", endpointConfiguration.getDatabaseName(), request.getPayload(String.class)));
        }

        Message response = delegate.handleMessage(request);

        if (response != null && response.getPayload() != null) {
            if (response.getPayload() instanceof String) {
                response.setPayload(endpointConfiguration.getMarshaller().unmarshal(response.getPayload(Source.class)));
            }
        }

        return Optional.ofNullable(response)
                .orElse(new DefaultMessage(new OperationResult(true)));
    }

    @Override
    public void getConnection(Map<String, String> properties) throws JdbcServerException {
        if (!endpointConfiguration.isAutoConnect()) {
            OperationResult result = handleMessage(new DefaultMessage(new Operation(new OpenConnection(properties.entrySet()
                    .stream()
                    .map(entry -> new OpenConnection.Property(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList()))))).getPayload(OperationResult.class);

            if (!result.isSuccess()) {
                throw new JdbcServerException(result.getException());
            }
        }

        if (connections.get() == endpointConfiguration.getMaxConnections()) {
            throw new JdbcServerException(String.format("Maximum number of connections (%s) reached", endpointConfiguration.getMaxConnections()));
        }

        connections.incrementAndGet();
    }

    @Override
    public void closeConnection() throws JdbcServerException {
        if (!endpointConfiguration.isAutoConnect()) {
            OperationResult result = handleMessage(new DefaultMessage(new Operation(new CloseConnection()))).getPayload(OperationResult.class);

            if (!result.isSuccess()) {
                throw new JdbcServerException(result.getException());
            }
        }

        if (connections.decrementAndGet() < 0) {
            connections.set(0);
        }
    }

    @Override
    public void createPreparedStatement(String stmt) throws JdbcServerException {
        if (!endpointConfiguration.isAutoCreateStatement()) {
            OperationResult result = handleMessage(new DefaultMessage(new Operation(new CreatePreparedStatement(stmt)))).getPayload(OperationResult.class);

            if (!result.isSuccess()) {
                throw new JdbcServerException(result.getException());
            }
        }
    }

    @Override
    public void createStatement() throws JdbcServerException {
        if (!endpointConfiguration.isAutoCreateStatement()) {
            OperationResult result = handleMessage(new DefaultMessage(new Operation(new CreateStatement()))).getPayload(OperationResult.class);

            if (!result.isSuccess()) {
                throw new JdbcServerException(result.getException());
            }
        }
    }

    @Override
    public ResultSet executeQuery(String stmt) throws JdbcServerException {
        log.info("Received execute query request: " + stmt);

        OperationResult result = handleMessage(new DefaultMessage(new Operation(new Execute(new Execute.Statement(stmt))))).getPayload(OperationResult.class);

        if (!result.isSuccess()) {
            throw new JdbcServerException(result.getException());
        }

        return Optional.ofNullable(result.getResultSet()).orElse(new ResultSet());
    }

    @Override
    public void execute(String stmt) throws JdbcServerException {
        log.info("Received execute statement request: " + stmt);

        OperationResult result = handleMessage(new DefaultMessage(new Operation(new Execute(new Execute.Statement(stmt))))).getPayload(OperationResult.class);

        if (!result.isSuccess()) {
            throw new JdbcServerException(result.getException());
        }
    }

    @Override
    public int executeUpdate(String stmt) throws JdbcServerException {
        log.info("Received execute update request: " + stmt);

        OperationResult result = handleMessage(new DefaultMessage(new Operation(new Execute(new Execute.Statement(stmt))))).getPayload(OperationResult.class);

        if (!result.isSuccess()) {
            throw new JdbcServerException(result.getException());
        }

        return Optional.ofNullable(result.getResultSet()).orElse(new ResultSet()).getAffectedRows();
    }

    @Override
    public void closeStatement() throws JdbcServerException {

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
