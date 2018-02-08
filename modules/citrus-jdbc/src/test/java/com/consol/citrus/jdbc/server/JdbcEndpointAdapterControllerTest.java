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
import com.consol.citrus.db.server.JdbcServerException;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.message.JdbcMessageHeaders;
import com.consol.citrus.jdbc.model.JdbcMarshaller;
import com.consol.citrus.jdbc.model.Operation;
import com.consol.citrus.message.Message;
import org.springframework.xml.transform.StringResult;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class JdbcEndpointAdapterControllerTest {

    private final JdbcEndpointConfiguration jdbcEndpointConfiguration = mock(JdbcEndpointConfiguration.class);
    private final EndpointAdapter endpointAdapter = mock(EndpointAdapter.class);

    private final JdbcEndpointAdapterController jdbcEndpointAdapterController =
            new JdbcEndpointAdapterController(jdbcEndpointConfiguration, endpointAdapter);

    @BeforeSuite
    public void setup(){
        final JdbcServerConfiguration serverConfiguration = mock(JdbcServerConfiguration.class);
        when(serverConfiguration.getMaxConnections()).thenReturn(1);

        when(jdbcEndpointConfiguration.getServerConfiguration()).thenReturn(serverConfiguration);
    }

    @Test
    public void testHandleMessage(){

        //GIVEN
        final Message request = mock(Message.class);
        final Message expectedResponse = mock(Message.class);

        when(endpointAdapter.handleMessage(request)).thenReturn(expectedResponse);

        //WHEN
        final Message response = jdbcEndpointAdapterController.handleMessage(request);

        //THEN
        assertEquals(response, expectedResponse);
    }

    @Test
    public void testHandleMessageWhenEndpointAdapterReturnsNull(){

        //GIVEN
        final Message request = mock(Message.class);
        final Message expectedResponse = JdbcMessage.result(true);

        when(endpointAdapter.handleMessage(request)).thenReturn(null);

        //WHEN
        final Message response = jdbcEndpointAdapterController.handleMessage(request);

        //THEN
        assertEquals(response.getPayload(), expectedResponse.getPayload());
    }

    @Test
    public void testHandleMessageOperationPayloadConversion(){

        //GIVEN
        final Message request = mock(Message.class);
        final Operation payload = mock(Operation.class);
        when(request.getPayload()).thenReturn(payload);
        when(request.getPayload(Operation.class)).thenReturn(payload);

        final JdbcMarshaller jdbcMarshallerMock = mock(JdbcMarshaller.class);
        when(jdbcEndpointConfiguration.getMarshaller()).thenReturn(jdbcMarshallerMock);

        //WHEN
        jdbcEndpointAdapterController.handleMessage(request);

        //THEN
        verify(jdbcMarshallerMock).marshal(eq(payload), any(StringResult.class));
        verify(request).setPayload(anyString());
    }

    @Test
    public void testOpenConnection(){

        //GIVEN
        when(jdbcEndpointConfiguration.isAutoConnect()).thenReturn(true);

        //WHEN
        final int before = jdbcEndpointAdapterController.getConnections().get();
        jdbcEndpointAdapterController.openConnection(new HashMap<>());
        final int after = jdbcEndpointAdapterController.getConnections().get();

        //THEN
        assertEquals(before + 1, after);
    }

    @Test
    public void testOpenConnectionWithoutAutoConnect(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);

        when(jdbcEndpointConfiguration.isAutoConnect()).thenReturn(false);

        //WHEN
        final int before = jdbcEndpointAdapterController.getConnections().get();
        jdbcEndpointAdapterController.openConnection(new HashMap<>());
        final int after = jdbcEndpointAdapterController.getConnections().get();

        //THEN
        verify(jdbcEndpointAdapterController).handleMessage(any());
        assertEquals(before + 1, after);
    }

    @Test(expectedExceptions = JdbcServerException.class)
    public void testOpenConnectionWithoutAutoConnectAndInvalidProperties(){

        //GIVEN
        when(jdbcEndpointConfiguration.isAutoConnect()).thenReturn(false);

        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);

        final Message errorMessage = mock(Message.class);
        when(errorMessage.getHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS)).thenReturn("false");
        doReturn(errorMessage).when(jdbcEndpointAdapterController).handleMessage(any());

        //WHEN
        jdbcEndpointAdapterController.openConnection(new HashMap<>());

        //THEN
        //Exception is Thrown
    }

    @Test(expectedExceptions = JdbcServerException.class)
    public void testOpenConnectionMaximumConnectionsReached(){

        //GIVEN
        when(jdbcEndpointConfiguration.isAutoConnect()).thenReturn(true);
        jdbcEndpointAdapterController.getConnections().set(1);

        //WHEN
        jdbcEndpointAdapterController.openConnection(new HashMap<>());

        //THEN
        //Exception is Thrown
    }

    @Test
    public void testCloseConnection(){

        //GIVEN
        when(jdbcEndpointConfiguration.isAutoConnect()).thenReturn(true);
        jdbcEndpointAdapterController.getConnections().set(1);

        //WHEN
        final int before = jdbcEndpointAdapterController.getConnections().get();
        jdbcEndpointAdapterController.closeConnection();
        final int after = jdbcEndpointAdapterController.getConnections().get();

        //THEN
        assertEquals(before -1 , after);
    }

    @Test
    public void testCloseConnectionWithoutAutoConnect(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        jdbcEndpointAdapterController.getConnections().set(1);
        when(jdbcEndpointConfiguration.isAutoConnect()).thenReturn(false);

        //WHEN
        final int before = jdbcEndpointAdapterController.getConnections().get();
        jdbcEndpointAdapterController.closeConnection();
        final int after = jdbcEndpointAdapterController.getConnections().get();

        //THEN
        verify(jdbcEndpointAdapterController).handleMessage(any());
        assertEquals(before - 1, after);
    }

    @Test(expectedExceptions = JdbcServerException.class)
    public void testErroneousCloseConnection(){

        //GIVEN
        when(jdbcEndpointConfiguration.isAutoConnect()).thenReturn(false);

        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);

        final Message errorMessage = mock(Message.class);
        when(errorMessage.getHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS)).thenReturn("false");
        doReturn(errorMessage).when(jdbcEndpointAdapterController).handleMessage(any());

        //WHEN
        jdbcEndpointAdapterController.closeConnection();

        //THEN
        //Exception is Thrown
    }

    @Test
    public void testCloseConnectionWithoutOpenConnectionIsSuccessful(){

        //GIVEN
        when(jdbcEndpointConfiguration.isAutoConnect()).thenReturn(true);
        jdbcEndpointAdapterController.getConnections().set(0);


        //WHEN
        jdbcEndpointAdapterController.closeConnection();

        //THEN
        assertEquals(jdbcEndpointAdapterController.getConnections().get(), 0);
    }
}