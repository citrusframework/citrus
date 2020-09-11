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

package com.consol.citrus.jdbc.model;

import com.consol.citrus.message.MessageType;
import com.consol.citrus.xml.StringResult;
import com.consol.citrus.xml.StringSource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class JdbcMarshallerTest {

    private JdbcMarshaller marshaller = new JdbcMarshaller();

    @Test
    public void testUnmarshalOperationJson() {
        marshaller.setType(MessageType.XML.name());
        Object operation = marshaller.unmarshal(new StringSource("{\"openConnection\":{}}"));

        Assert.assertTrue(operation instanceof Operation);
        Assert.assertNotNull(((Operation) operation).getOpenConnection());

        marshaller.setType(MessageType.JSON.name());
        operation = marshaller.unmarshal(new StringSource("{\"closeConnection\":{}}"));

        Assert.assertTrue(operation instanceof Operation);
        Assert.assertNotNull(((Operation) operation).getCloseConnection());
    }

    @Test
    public void testUnmarshalOperationXml() {
        marshaller.setType(MessageType.XML.name());
        Object operation = marshaller.unmarshal(new StringSource("<operation xmlns=\"http://www.citrusframework.org/schema/jdbc/message\"><open-connection/></operation>"));

        Assert.assertTrue(operation instanceof Operation);
        Assert.assertNotNull(((Operation) operation).getOpenConnection());

        marshaller.setType(MessageType.JSON.name());
        operation = marshaller.unmarshal(new StringSource("<operation xmlns=\"http://www.citrusframework.org/schema/jdbc/message\"><close-connection/></operation>"));

        Assert.assertTrue(operation instanceof Operation);
        Assert.assertNotNull(((Operation) operation).getCloseConnection());
    }

    @Test
    public void testUnmarshalOperationResultJson() {
        marshaller.setType(MessageType.XML.name());
        Object operationResult = marshaller.unmarshal(new StringSource("{\"success\": false, \"exception\": \"Something went wrong\"}"));

        Assert.assertTrue(operationResult instanceof OperationResult);
        Assert.assertFalse(((OperationResult) operationResult).isSuccess());
        Assert.assertEquals(((OperationResult) operationResult).getException(), "Something went wrong");

        marshaller.setType(MessageType.JSON.name());
        operationResult = marshaller.unmarshal(new StringSource("{\"success\": true}"));

        Assert.assertTrue(operationResult instanceof OperationResult);
        Assert.assertTrue(((OperationResult) operationResult).isSuccess());
    }

    @Test
    public void testUnmarshalOperationResultXml() {
        marshaller.setType(MessageType.XML.name());
        Object operationResult = marshaller.unmarshal(new StringSource("<operation-result xmlns=\"http://www.citrusframework.org/schema/jdbc/message\"><success>false</success><exception>Something went wrong</exception></operation-result>"));

        Assert.assertTrue(operationResult instanceof OperationResult);
        Assert.assertFalse(((OperationResult) operationResult).isSuccess());
        Assert.assertEquals(((OperationResult) operationResult).getException(), "Something went wrong");

        marshaller.setType(MessageType.JSON.name());
        operationResult = marshaller.unmarshal(new StringSource("<operation-result xmlns=\"http://www.citrusframework.org/schema/jdbc/message\"><success>true</success></operation-result>"));

        Assert.assertTrue(operationResult instanceof OperationResult);
        Assert.assertTrue(((OperationResult) operationResult).isSuccess());
    }

    @Test
    public void testMarshalOperation() {
        Operation operation = new Operation();
        OpenConnection openConnection = new OpenConnection();
        operation.setOpenConnection(openConnection);

        marshaller.setType(MessageType.XML.name());
        StringResult result = new StringResult();
        marshaller.marshal(operation, result);
        Assert.assertEquals(result.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><operation xmlns=\"http://www.citrusframework.org/schema/jdbc/message\"><open-connection/></operation>");

        marshaller.setType(MessageType.JSON.name());
        result = new StringResult();
        marshaller.marshal(operation, result);
        Assert.assertEquals(result.toString(), "{\"openConnection\":{\"properties\":[]}}");
    }

    @Test
    public void testMarshalOperationResult() {
        OperationResult operationResult = new OperationResult();
        operationResult.setSuccess(true);
        operationResult.setAffectedRows(5);

        marshaller.setType(MessageType.XML.name());
        StringResult result = new StringResult();
        marshaller.marshal(operationResult, result);
        Assert.assertEquals(result.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><operation-result affected-rows=\"5\" xmlns=\"http://www.citrusframework.org/schema/jdbc/message\"><success>true</success></operation-result>");

        marshaller.setType(MessageType.JSON.name());
        result = new StringResult();
        marshaller.marshal(operationResult, result);
        Assert.assertEquals(result.toString(), "{\"success\":true,\"affectedRows\":5}");
    }
}
