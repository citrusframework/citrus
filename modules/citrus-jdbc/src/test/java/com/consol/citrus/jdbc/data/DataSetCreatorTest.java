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
    public void testCreateDataSetWithDataSetPayload(){

        //GIVEN
        final DataSet expectedDataSet = mock(DataSet.class);

        final Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(expectedDataSet);
        when(message.getPayload(DataSet.class)).thenReturn(expectedDataSet);

        //WHEN
        final DataSet dataSet = dataSetCreator.createDataSet(message, null);

        //THEN
        assertEquals(dataSet, expectedDataSet);
    }

    @Test
    public void testCreateDataSetWithUnknownType(){

        //GIVEN

        //WHEN
        final DataSet dataSet = dataSetCreator.createDataSet(mock(Message.class), null);

        //THEN
        assertEquals(dataSet, new DataSet());
    }

    @Test
    public void testCreateDataSetFromJson() throws SQLException {

        //GIVEN
        final String payload = "[{ \"foo\": \"bar\" }]";
        final Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload);
        when(message.getPayload(String.class)).thenReturn(payload);

        //WHEN
        final DataSet dataSet = dataSetCreator.createDataSet(message, MessageType.JSON);

        //THEN
        assertEquals(dataSet.getColumns().toString(), "[foo]");
        assertEquals(dataSet.getNextRow().getValues().toString(), "{foo=bar}");
    }

    @Test
    public void testCreateDataSetFromXml() throws SQLException {

        //GIVEN
        final String payload = "<dataset><row><foo>bar</foo></row></dataset>";
        final Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload);
        when(message.getPayload(String.class)).thenReturn(payload);

        //WHEN
        final DataSet dataSet = dataSetCreator.createDataSet(message, MessageType.XML);

        //THEN
        assertEquals(dataSet.getColumns().toString(), "[foo]");
        assertEquals(dataSet.getNextRow().getValues().toString(), "{foo=bar}");
    }

    @Test
    public void testCreateDataSetFromNotImplementedType() {

        //GIVEN
        final Message message = mock(Message.class);
        when(message.getPayload()).thenReturn("");
        when(message.getPayload(String.class)).thenReturn("");

        //WHEN
        final DataSet dataSet = dataSetCreator.createDataSet(message, MessageType.BINARY_BASE64);

        //THEN
        Assert.assertEquals(dataSet, new DataSet());
    }
}