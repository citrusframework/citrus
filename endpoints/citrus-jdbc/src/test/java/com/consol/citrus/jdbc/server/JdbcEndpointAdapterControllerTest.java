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

import java.util.HashMap;
import java.util.Random;

import com.consol.citrus.db.driver.dataset.DataSet;
import com.consol.citrus.db.server.JdbcServerConfiguration;
import com.consol.citrus.db.server.JdbcServerException;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.jdbc.data.DataSetCreator;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.message.JdbcMessageHeaders;
import com.consol.citrus.jdbc.model.Execute;
import com.consol.citrus.jdbc.model.JdbcMarshaller;
import com.consol.citrus.jdbc.model.Operation;
import com.consol.citrus.jdbc.model.OperationResult;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.xml.StringResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class JdbcEndpointAdapterControllerTest {

    private final JdbcEndpointConfiguration jdbcEndpointConfiguration = mock(JdbcEndpointConfiguration.class);
    private final EndpointAdapter endpointAdapter = mock(EndpointAdapter.class);

    private JdbcEndpointAdapterController jdbcEndpointAdapterController;

    @BeforeMethod
    public void setup(){
        final JdbcServerConfiguration serverConfiguration = mock(JdbcServerConfiguration.class);
        when(serverConfiguration.getMaxConnections()).thenReturn(1);

        when(jdbcEndpointConfiguration.getServerConfiguration()).thenReturn(serverConfiguration);
        when(jdbcEndpointConfiguration.getAutoHandleQueries()).thenReturn(new JdbcEndpointConfiguration().getAutoHandleQueries());

        jdbcEndpointAdapterController = new JdbcEndpointAdapterController(jdbcEndpointConfiguration, endpointAdapter);
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
        final Message expectedResponse = JdbcMessage.success();

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
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoConnect()).thenReturn(true);
        jdbcEndpointAdapterController.getConnections().set(0);

        //WHEN
        final int before = jdbcEndpointAdapterController.getConnections().get();
        jdbcEndpointAdapterController.openConnection(new HashMap<>());
        final int after = jdbcEndpointAdapterController.getConnections().get();

        //THEN
        assertEquals(before + 1, after);
        verify(jdbcEndpointAdapterController, never()).handleMessage(any());
    }

    @Test
    public void testOpenConnectionWithoutAutoConnect(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        jdbcEndpointAdapterController.getConnections().set(0);

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
        jdbcEndpointAdapterController.getConnections().set(0);

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
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoConnect()).thenReturn(true);
        jdbcEndpointAdapterController.getConnections().set(1);

        //WHEN
        final int before = jdbcEndpointAdapterController.getConnections().get();
        jdbcEndpointAdapterController.closeConnection();
        final int after = jdbcEndpointAdapterController.getConnections().get();

        //THEN
        verify(jdbcEndpointAdapterController, never()).handleMessage(any());
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
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoConnect()).thenReturn(true);
        jdbcEndpointAdapterController.getConnections().set(0);


        //WHEN
        jdbcEndpointAdapterController.closeConnection();

        //THEN
        verify(jdbcEndpointAdapterController, never()).handleMessage(any());
        assertEquals(jdbcEndpointAdapterController.getConnections().get(), 0);
    }

    @Test
    public void testCreatePreparedStatementWithAutoCreateStatement(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoCreateStatement()).thenReturn(true);

        //WHEN
        jdbcEndpointAdapterController.createPreparedStatement("some statement");

        //THEN
        verify(jdbcEndpointAdapterController, never()).handleMessage(any());
    }

    @Test
    public void testCreatePreparedStatementWithoutAutoCreateStatement(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoCreateStatement()).thenReturn(false);

        //WHEN
        jdbcEndpointAdapterController.createPreparedStatement("some statement");

        //THEN
        verify(jdbcEndpointAdapterController).handleMessage(any());
    }

    @Test(expectedExceptions = JdbcServerException.class)
    public void testCreatePreparedStatementWithoutAutoCreateStatementAndFailure(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoCreateStatement()).thenReturn(false);

        final Message errorMessage = mock(Message.class);
        when(errorMessage.getHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS)).thenReturn("false");
        doReturn(errorMessage).when(jdbcEndpointAdapterController).handleMessage(any());

        //WHEN
        jdbcEndpointAdapterController.createPreparedStatement("some statement");

        //THEN
        //Exception is thrown
    }

    @Test
    public void testCreateStatementWithAutoCreateStatement(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoCreateStatement()).thenReturn(true);

        //WHEN
        jdbcEndpointAdapterController.createStatement();

        //THEN
        verify(jdbcEndpointAdapterController, never()).handleMessage(any());
    }

    @Test
    public void testCreateStatementWithoutAutoCreateStatement(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoCreateStatement()).thenReturn(false);

        //WHEN
        jdbcEndpointAdapterController.createStatement();

        //THEN
        verify(jdbcEndpointAdapterController).handleMessage(any());
    }

    @Test(expectedExceptions = JdbcServerException.class)
    public void testCreateStatementWithoutAutoCreateStatementAndFailure(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoCreateStatement()).thenReturn(false);

        final Message errorMessage = mock(Message.class);
        when(errorMessage.getHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS)).thenReturn("false");
        doReturn(errorMessage).when(jdbcEndpointAdapterController).handleMessage(any());

        //WHEN
        jdbcEndpointAdapterController.createStatement();

        //THEN
        //Exception is thrown
    }

    @Test
    public void testExecuteQuery(){

        //GIVEN
        final DataSet expectedDataSet = mock(DataSet.class);
        final DataSetCreator dataSetCreator = mock(DataSetCreator.class);
        when(dataSetCreator.createDataSet(any(), any())).thenReturn(expectedDataSet);

        final JdbcEndpointAdapterController jdbcEndpointAdapterController =
                spy(new JdbcEndpointAdapterController(jdbcEndpointConfiguration, endpointAdapter, dataSetCreator));

        final Message messageToMarshal = mock(Message.class);
        when(messageToMarshal.getType()).thenReturn(MessageType.JSON.toString());
        doReturn(messageToMarshal).when(jdbcEndpointAdapterController).handleMessage(any());

        final String query = "some query";

        //WHEN
        final DataSet dataSet = jdbcEndpointAdapterController.executeQuery(query);

        //THEN
        verify(jdbcEndpointAdapterController).handleMessage(any());
        verify(dataSetCreator).createDataSet(messageToMarshal, MessageType.JSON);
        assertEquals(dataSet, expectedDataSet);
    }

    @Test(expectedExceptions = JdbcServerException.class)
    public void testExecuteQueryForwardsException(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);

        final Message errorMessage = mock(Message.class);
        when(errorMessage.getHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS)).thenReturn("false");
        doReturn(errorMessage).when(jdbcEndpointAdapterController).handleMessage(any());

        final String query = "some query";

        //WHEN
        jdbcEndpointAdapterController.executeQuery(query);

        //THEN
        //Exception is thrown
    }

    @Test
    public void testExecute(){

        //GIVEN
        final DataSet expectedDataSet = mock(DataSet.class);
        final DataSetCreator dataSetCreator = mock(DataSetCreator.class);
        when(dataSetCreator.createDataSet(any(), any())).thenReturn(expectedDataSet);

        final JdbcEndpointAdapterController jdbcEndpointAdapterController =
                spy(new JdbcEndpointAdapterController(jdbcEndpointConfiguration, endpointAdapter, dataSetCreator));

        final Message messageToMarshal = mock(Message.class);
        when(messageToMarshal.getType()).thenReturn(MessageType.JSON.toString());
        doReturn(messageToMarshal).when(jdbcEndpointAdapterController).handleMessage(any());

        //WHEN
        jdbcEndpointAdapterController.executeStatement("statement");

        //THEN
        verify(jdbcEndpointAdapterController).handleMessage(any());
    }

    @Test(expectedExceptions = JdbcServerException.class)
    public void testExecuteWithFailure(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);

        final Message errorMessage = mock(Message.class);
        when(errorMessage.getHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS)).thenReturn("false");
        doReturn(errorMessage).when(jdbcEndpointAdapterController).handleMessage(any());

        //WHEN
        jdbcEndpointAdapterController.executeStatement("statement");

        //THEN
        //Exception is thrown
    }

    @Test
    public void testExecuteUpdate(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);

        final Message errorMessage = mock(Message.class);
        when(errorMessage.getHeader(JdbcMessageHeaders.JDBC_ROWS_UPDATED)).thenReturn("2");
        doReturn(errorMessage).when(jdbcEndpointAdapterController).handleMessage(any());

        //WHEN
        final int rowsUpdated = jdbcEndpointAdapterController.executeUpdate("statement");

        //THEN
        verify(jdbcEndpointAdapterController).handleMessage(any());
        assertEquals(rowsUpdated, 2);
    }


    @Test(expectedExceptions = JdbcServerException.class)
    public void testExecuteUpdateWithFailure(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);

        final Message errorMessage = mock(Message.class);
        when(errorMessage.getHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS)).thenReturn("false");
        doReturn(errorMessage).when(jdbcEndpointAdapterController).handleMessage(any());

        //WHEN
        jdbcEndpointAdapterController.executeUpdate("statement");

        //THEN
        //Exception is thrown
    }

    @Test
    public void testCloseStatementWithAutoCreateStatement(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoCreateStatement()).thenReturn(true);

        //WHEN
        jdbcEndpointAdapterController.closeStatement();

        //THEN
        verify(jdbcEndpointAdapterController, never()).handleMessage(any());
    }

    @Test
    public void testCloseStatementWithoutAutoCreateStatement(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoCreateStatement()).thenReturn(false);

        //WHEN
        jdbcEndpointAdapterController.closeStatement();

        //THEN
        verify(jdbcEndpointAdapterController).handleMessage(any());
    }

    @Test(expectedExceptions = JdbcServerException.class)
    public void testCloseStatementWithoutAutoCreateStatementAndFailure(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoCreateStatement()).thenReturn(false);

        final Message errorMessage = mock(Message.class);
        when(errorMessage.getHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS)).thenReturn("false");
        doReturn(errorMessage).when(jdbcEndpointAdapterController).handleMessage(any());

        //WHEN
        jdbcEndpointAdapterController.closeStatement();

        //THEN
        //Exception is thrown
    }

    @Test
    public void testSetTransactionState(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        final boolean expectedBoolean = new Random().nextBoolean();
        when(jdbcEndpointConfiguration.isAutoTransactionHandling()).thenReturn(true);

        //WHEN
        jdbcEndpointAdapterController.setTransactionState(expectedBoolean);

        //THEN
        verify(jdbcEndpointAdapterController, never()).handleMessage(any());
        assertEquals(jdbcEndpointAdapterController.getTransactionState(), expectedBoolean);

    }

    @Test
    public void testSetTransactionStateWithoutAutoTransactionHandling(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoTransactionHandling()).thenReturn(false);

        //WHEN
        jdbcEndpointAdapterController.setTransactionState(true);

        //THEN
        verify(jdbcEndpointAdapterController).handleMessage(any());
    }

    @Test
    public void testSetTransactionStateVerifyMessageOnlyIfTransactionHasBeenStarted(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoTransactionHandling()).thenReturn(false);

        //WHEN
        jdbcEndpointAdapterController.setTransactionState(false);

        //THEN
        verify(jdbcEndpointAdapterController, never()).handleMessage(any());
    }

    @Test
    public void testCommitStatementsWithAutoCreateStatement(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoTransactionHandling()).thenReturn(true);

        //WHEN
        jdbcEndpointAdapterController.commitStatements();

        //THEN
        verify(jdbcEndpointAdapterController, never()).handleMessage(any());
    }

    @Test
    public void testCommitStatementsWithoutAutoCreateStatement(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoTransactionHandling()).thenReturn(false);

        //WHEN
        jdbcEndpointAdapterController.commitStatements();

        //THEN
        verify(jdbcEndpointAdapterController).handleMessage(any());
    }

    @Test(expectedExceptions = JdbcServerException.class)
    public void testCommitStatementsWithoutAutoCreateStatementAndFailure(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoTransactionHandling()).thenReturn(false);

        final Message errorMessage = mock(Message.class);
        when(errorMessage.getHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS)).thenReturn("false");
        doReturn(errorMessage).when(jdbcEndpointAdapterController).handleMessage(any());

        //WHEN
        jdbcEndpointAdapterController.commitStatements();

        //THEN
        //Exception is thrown
    }

    @Test
    public void testRollbackStatementsWithAutoCreateStatement(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoTransactionHandling()).thenReturn(true);

        //WHEN
        jdbcEndpointAdapterController.rollbackStatements();

        //THEN
        verify(jdbcEndpointAdapterController, never()).handleMessage(any());
    }

    @Test
    public void testRollbackStatementsWithoutAutoCreateStatement(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoTransactionHandling()).thenReturn(false);

        //WHEN
        jdbcEndpointAdapterController.rollbackStatements();

        //THEN
        verify(jdbcEndpointAdapterController).handleMessage(any());
    }

    @Test(expectedExceptions = JdbcServerException.class)
    public void testRollbackStatementsWithoutAutoCreateStatementAndFailure(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoTransactionHandling()).thenReturn(false);

        final Message errorMessage = mock(Message.class);
        when(errorMessage.getHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS)).thenReturn("false");
        doReturn(errorMessage).when(jdbcEndpointAdapterController).handleMessage(any());

        //WHEN
        jdbcEndpointAdapterController.rollbackStatements();

        //THEN
        //Exception is thrown
    }

    @Test
    public void testCreateCallableStatementWithAutoCreateStatement(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoCreateStatement()).thenReturn(true);

        //WHEN
        jdbcEndpointAdapterController.createCallableStatement("some statement");

        //THEN
        verify(jdbcEndpointAdapterController, never()).handleMessage(any());
    }

    @Test
    public void testCreateCallableStatementWithoutAutoCreateStatement(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoCreateStatement()).thenReturn(false);

        //WHEN
        jdbcEndpointAdapterController.createCallableStatement("some statement");

        //THEN
        verify(jdbcEndpointAdapterController).handleMessage(any());
    }

    @Test(expectedExceptions = JdbcServerException.class)
    public void testCreateCallableStatementWithoutAutoCreateStatementAndFailure(){

        //GIVEN
        final JdbcEndpointAdapterController jdbcEndpointAdapterController = spy(this.jdbcEndpointAdapterController);
        when(jdbcEndpointConfiguration.isAutoCreateStatement()).thenReturn(false);

        final Message errorMessage = mock(Message.class);
        when(errorMessage.getHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS)).thenReturn("false");
        doReturn(errorMessage).when(jdbcEndpointAdapterController).handleMessage(any());

        //WHEN
        jdbcEndpointAdapterController.createCallableStatement("some statement");

        //THEN
        //Exception is thrown
    }

    @Test
    public void testHandleMessageWithAutoHandleQueriesEmptyOperation(){

        //GIVEN
        final Message request = mock(Message.class);
        when(request.getPayload(Operation.class)).thenReturn(null);
        final Message expectedResponse = mock(Message.class);;

        when(endpointAdapter.handleMessage(request)).thenReturn(expectedResponse);

        //WHEN
        final Message response = jdbcEndpointAdapterController.handleMessage(request);

        //THEN
        assertEquals(response.getPayload(), expectedResponse.getPayload());
        verify(endpointAdapter, times(1)).handleMessage(request);
    }

    @Test
    public void testHandleMessageWithAutoHandleQueriesQueries(){

        //GIVEN
        final Message request = mock(Message.class);
        Operation operation = new Operation();
        operation.setExecute(new Execute());
        operation.getExecute().setStatement(new Execute.Statement());
        operation.getExecute().getStatement().setSql("SELECT 1");
        when(request.getPayload(Operation.class)).thenReturn(operation);

        //WHEN
        final Message response = jdbcEndpointAdapterController.handleMessage(request);

        //THEN
        assertTrue(JdbcMessage.class.isAssignableFrom(response.getClass()));
        JdbcMessage jdbcMessageResponse = (JdbcMessage) response;
        OperationResult operationResult = jdbcMessageResponse.getPayload(OperationResult.class);
        assertTrue(operationResult.isSuccess());
        assertEquals(operationResult.getDataSet(), null);
        assertEquals(operationResult.getAffectedRows(), new Integer(0));
        verify(endpointAdapter, times(0)).handleMessage(request);
    }

    @DataProvider
    public Object[][] systemQueries() {
        return new Object[][] {
                {"Select 1", true},
                {"Select 1 from", false},
                {"SELECT USER", true},
                {"SELECT USER from DUAL", true},
                {"SELECT 1 from DUAL", true},
                {"SELECT USER FROM SYSIBM.SYSDUMMY1", true},
                {"SELECT 1 FROM SYSIBM.SYSDUMMY1", true},
                {"SELECT 1 FROM SYSIBM.SYSDUMMY1 where", false},
        };
    }

    @Test(dataProvider = "systemQueries")
    public void match(String query, boolean isMatching) {
        reset(endpointAdapter);

        //GIVEN
        final Message request = mock(Message.class);
        Operation operation = new Operation();
        operation.setExecute(new Execute());
        operation.getExecute().setStatement(new Execute.Statement());
        operation.getExecute().getStatement().setSql(query);
        when(request.getPayload(Operation.class)).thenReturn(operation);

        //WHEN
        final Message response = jdbcEndpointAdapterController.handleMessage(request);

        //THEN
        assertTrue(JdbcMessage.class.isAssignableFrom(response.getClass()));
        JdbcMessage jdbcMessageResponse = (JdbcMessage) response;
        OperationResult operationResult = jdbcMessageResponse.getPayload(OperationResult.class);
        assertTrue(operationResult.isSuccess());
        assertEquals(operationResult.getDataSet(), null);
        assertEquals(operationResult.getAffectedRows(), isMatching ? 0 : null);

        verify(endpointAdapter, times(isMatching ? 0 : 1)).handleMessage(any());
    }

    @DataProvider
    public Object[][] systemQueriesOverwrite() {
        return new Object[][] {
                {"Select 1", true},
                {"Select 1 from", false},
                {"SELECT USER", false},
                {"SELECT 1", true},
        };
    }

    @Test(dataProvider = "systemQueriesOverwrite")
    public void matchUsingSystemProperty(String query, boolean isMatching) {
        reset(endpointAdapter);

        System.setProperty(JdbcEndpointAdapterController.AUTO_HANDLE_QUERY_PROPERTY, "select 1;;");

        JdbcEndpointAdapterController jdbcEndpointAdapterController =
                new JdbcEndpointAdapterController(jdbcEndpointConfiguration, endpointAdapter);

        //GIVEN
        final Message request = mock(Message.class);
        Operation operation = new Operation();
        operation.setExecute(new Execute());
        operation.getExecute().setStatement(new Execute.Statement());
        operation.getExecute().getStatement().setSql(query);
        when(request.getPayload(Operation.class)).thenReturn(operation);

        //WHEN
        final Message response = jdbcEndpointAdapterController.handleMessage(request);

        //THEN
        assertTrue(JdbcMessage.class.isAssignableFrom(response.getClass()));
        JdbcMessage jdbcMessageResponse = (JdbcMessage) response;
        OperationResult operationResult = jdbcMessageResponse.getPayload(OperationResult.class);
        assertTrue(operationResult.isSuccess());
        assertEquals(operationResult.getDataSet(), null);
        assertEquals(operationResult.getAffectedRows(), isMatching ? 0 : null);

        verify(endpointAdapter, times(isMatching ? 0 : 1)).handleMessage(any());
    }

}
