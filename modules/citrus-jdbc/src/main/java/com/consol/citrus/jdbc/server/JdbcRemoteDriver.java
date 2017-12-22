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

import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.jdbc.driver.JdbcEndpointConfiguration;
import com.consol.citrus.jdbc.model.*;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.xml.transform.StringResult;

import javax.xml.transform.Source;
import java.rmi.RemoteException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcRemoteDriver implements RemoteDriver {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JdbcRemoteDriver.class);

    private final JdbcEndpointConfiguration endpointConfiguration;
    private final EndpointAdapter endpointAdapter;

    private AtomicInteger connections = new AtomicInteger(0);

    /**
     * Default constructor using fields.
     * @param endpointConfiguration
     * @param endpointAdapter
     */
    public JdbcRemoteDriver(JdbcEndpointConfiguration endpointConfiguration, EndpointAdapter endpointAdapter) {
        this.endpointConfiguration = endpointConfiguration;
        this.endpointAdapter = endpointAdapter;
    }

    private Message process(Message request) throws RemoteException {
        if (log.isDebugEnabled()) {
            log.debug("Received message on db server: '" + endpointConfiguration.getBinding() + "'");
        }

        if (request.getPayload() instanceof Operation) {
            StringResult result = new StringResult();
            endpointConfiguration.getMarshaller().marshal(request.getPayload(Operation.class), result);
            request.setPayload(result.toString());
        }

        Message response = endpointAdapter.handleMessage(request);

        if (response != null && response.getPayload() != null) {
            if (response.getPayload() instanceof String) {
                response.setPayload(endpointConfiguration.getMarshaller().unmarshal(response.getPayload(Source.class)));
            }
        }

        return Optional.ofNullable(response)
                .orElse(new DefaultMessage(new OperationResult(true)));
    }

    @Override
    public RemoteConnection getConnection() throws RemoteException {
        if (!endpointConfiguration.isAutoConnect()) {
            OperationResult result = process(new DefaultMessage(new Operation(new OpenConnection()))).getPayload(OperationResult.class);

            if (!result.isSuccess()) {
                throw new RemoteException(result.getException());
            }
        }

        if (connections.get() == endpointConfiguration.getMaxConnections()) {
            throw new RemoteException(String.format("Maximum number of connections (%s) reached", endpointConfiguration.getMaxConnections()));
        }

        connections.incrementAndGet();

        return this;
    }

    @Override
    public void closeConnection() throws RemoteException {
        if (!endpointConfiguration.isAutoConnect()) {
            OperationResult result = process(new DefaultMessage(new Operation(new CloseConnection()))).getPayload(OperationResult.class);

            if (!result.isSuccess()) {
                throw new RemoteException(result.getException());
            }
        }

        if (connections.decrementAndGet() < 0) {
            connections.set(0);
        }
    }

    @Override
    public RemoteStatement createStatement() throws RemoteException {
        if (!endpointConfiguration.isAutoCreateStatement()) {
            OperationResult result = process(new DefaultMessage(new Operation(new CreateStatement()))).getPayload(OperationResult.class);

            if (!result.isSuccess()) {
                throw new RemoteException(result.getException());
            }
        }

        return this;
    }

    @Override
    public ResultSet executeQuery(String stmt) throws RemoteException {
        OperationResult result = process(new DefaultMessage(new Operation(new Execute(new Execute.Statement(stmt))))).getPayload(OperationResult.class);

        if (!result.isSuccess()) {
            throw new RemoteException(result.getException());
        }

        return Optional.ofNullable(result.getResultSet()).orElse(new ResultSet());
    }

    @Override
    public int executeUpdate(String stmt) throws RemoteException {
        OperationResult result = process(new DefaultMessage(new Operation(new Execute(new Execute.Statement(stmt))))).getPayload(OperationResult.class);

        if (!result.isSuccess()) {
            throw new RemoteException(result.getException());
        }

        return Integer.valueOf(Optional.ofNullable(result.getResultSet()).orElse(new ResultSet()).getAffectedRows());
    }

    @Override
    public void closeStatement() throws RemoteException {

    }
}
