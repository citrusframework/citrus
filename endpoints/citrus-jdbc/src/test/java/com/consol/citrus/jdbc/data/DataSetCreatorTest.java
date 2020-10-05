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

package com.consol.citrus.jdbc.data;

import com.consol.citrus.db.driver.dataset.DataSet;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.model.OperationResult;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class DataSetCreatorTest {

    private DataSetCreator dataSetCreator = new DataSetCreator();

    @Test
    public void testCreateDataSetEmpty() throws SQLException {
        //GIVEN
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn("");
        when(message.getPayload(String.class)).thenReturn("");

        //WHEN
        DataSet dataSet = dataSetCreator.createDataSet(message, null);

        //THEN
        assertEquals(dataSet.getColumns().size(), 0L);
        assertEquals(dataSet.getRows().size(), 0L);
    }

    @Test
    public void testCreateDataSetWithDataSetPayload(){
        //GIVEN
        DataSet expectedDataSet = mock(DataSet.class);

        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(expectedDataSet);
        when(message.getPayload(DataSet.class)).thenReturn(expectedDataSet);

        //WHEN
        DataSet dataSet = dataSetCreator.createDataSet(message, null);

        //THEN
        assertEquals(dataSet, expectedDataSet);
    }

    @Test
    public void testCreateDataSetWithUnknownType(){
        //GIVEN

        //WHEN
        DataSet dataSet = dataSetCreator.createDataSet(mock(Message.class), null);

        //THEN
        assertEquals(dataSet, new DataSet());
    }

    @Test
    public void testCreateDataSetFromJson() throws SQLException {
        //GIVEN
        String payload = "[{ \"foo\": \"bar\" }]";
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload);
        when(message.getPayload(String.class)).thenReturn(payload);

        //WHEN
        DataSet dataSet = dataSetCreator.createDataSet(message, MessageType.JSON);

        //THEN
        assertEquals(dataSet.getColumns().toString(), "[foo]");
        assertEquals(dataSet.getNextRow().getValues().toString(), "{foo=bar}");

        OperationResult operationResult = new OperationResult();
        operationResult.setDataSet(payload);
        JdbcMessage jdbcMessage = mock(JdbcMessage.class);
        when(jdbcMessage.getPayload(OperationResult.class)).thenReturn(operationResult);
        when(jdbcMessage.getPayload()).thenReturn(operationResult);

        //WHEN
        dataSet = dataSetCreator.createDataSet(jdbcMessage, MessageType.JSON);

        //THEN
        assertEquals(dataSet.getColumns().toString(), "[foo]");
        assertEquals(dataSet.getNextRow().getValues().toString(), "{foo=bar}");

        Message operationResultMessage = mock(Message.class);
        when(operationResultMessage.getPayload(OperationResult.class)).thenReturn(operationResult);
        when(operationResultMessage.getPayload()).thenReturn(operationResult);

        //WHEN
        dataSet = dataSetCreator.createDataSet(operationResultMessage, MessageType.JSON);

        //THEN
        assertEquals(dataSet.getColumns().toString(), "[foo]");
        assertEquals(dataSet.getNextRow().getValues().toString(), "{foo=bar}");
    }

    @Test
    public void testCreateDataSetFromXml() throws SQLException {
        //GIVEN
        String payload = "<dataset><row><foo>bar</foo></row></dataset>";
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload);
        when(message.getPayload(String.class)).thenReturn(payload);

        //WHEN
        DataSet dataSet = dataSetCreator.createDataSet(message, MessageType.XML);

        //THEN
        assertEquals(dataSet.getColumns().toString(), "[foo]");
        assertEquals(dataSet.getNextRow().getValues().toString(), "{foo=bar}");

        OperationResult operationResult = new OperationResult();
        operationResult.setDataSet(payload);
        JdbcMessage jdbcMessage = mock(JdbcMessage.class);
        when(jdbcMessage.getPayload(OperationResult.class)).thenReturn(operationResult);
        when(jdbcMessage.getPayload()).thenReturn(operationResult);

        //WHEN
        dataSet = dataSetCreator.createDataSet(jdbcMessage, MessageType.XML);

        //THEN
        assertEquals(dataSet.getColumns().toString(), "[foo]");
        assertEquals(dataSet.getNextRow().getValues().toString(), "{foo=bar}");

        Message operationResultMessage = mock(Message.class);
        when(operationResultMessage.getPayload(OperationResult.class)).thenReturn(operationResult);
        when(operationResultMessage.getPayload()).thenReturn(operationResult);

        //WHEN
        dataSet = dataSetCreator.createDataSet(operationResultMessage, MessageType.XML);

        //THEN
        assertEquals(dataSet.getColumns().toString(), "[foo]");
        assertEquals(dataSet.getNextRow().getValues().toString(), "{foo=bar}");
    }

    @Test
    public void testCreateDataSetFromNotImplementedType() {
        //GIVEN
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn("");
        when(message.getPayload(String.class)).thenReturn("");

        //WHEN
        DataSet dataSet = dataSetCreator.createDataSet(message, MessageType.BINARY_BASE64);

        //THEN
        Assert.assertEquals(dataSet, new DataSet());
    }
}
