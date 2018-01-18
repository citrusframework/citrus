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

package com.consol.citrus.jdbc.message;

import com.consol.citrus.db.driver.dataset.DataSet;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jdbc.model.*;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.util.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.xml.transform.StringResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class JdbcMessage extends DefaultMessage {

    private Operation operation;

    private JdbcMarshaller marshaller = new JdbcMarshaller();

    /**
     * Prevent traditional instantiation.
     */
    private JdbcMessage() { super(); }

    /**
     * Constructor initializes new JDBC operation.
     * @param operation
     */
    private JdbcMessage(Operation operation) {
        super(operation);
        this.operation = operation;
    }

    public static JdbcMessage openConnection(OpenConnection.Property ... properties) {
        OpenConnection openConnection = new OpenConnection();
        if (properties.length > 0) {
            openConnection.getProperties().addAll(Arrays.asList(properties));
        }
        return new JdbcMessage(new Operation(openConnection));
    }

    public static JdbcMessage openConnection(List<OpenConnection.Property> properties) {
        OpenConnection openConnection = new OpenConnection();
        openConnection.getProperties().addAll(properties);
        return new JdbcMessage(new Operation(openConnection));
    }

    public static JdbcMessage closeConnection() {
        return new JdbcMessage(new Operation(new CloseConnection()));
    }

    public static JdbcMessage createPreparedStatement(String sql) {
        return new JdbcMessage(new Operation(new CreatePreparedStatement(sql)));
    }

    public static JdbcMessage createStatement() {
        return new JdbcMessage(new Operation(new CreateStatement()));
    }

    public static JdbcMessage closeStatement() {
        return new JdbcMessage(new Operation(new CloseStatement()));
    }

    public static JdbcMessage execute(String sql) {
        return new JdbcMessage(new Operation(new Execute(new Execute.Statement(sql))));
    }

    public static JdbcMessage result() {
        return result(true);
    }

    public static JdbcMessage result(boolean success) {
        JdbcMessage message = new JdbcMessage();
        message.setHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS, success);
        return message;
    }

    public JdbcMessage exception(String message) {
        error();
        setHeader(JdbcMessageHeaders.JDBC_SERVER_EXCEPTION, message);
        return this;
    }

    public JdbcMessage rowsUpdated(int number) {
        success();
        setHeader(JdbcMessageHeaders.JDBC_ROWS_UPDATED, number);
        return this;
    }

    public JdbcMessage dataSet(DataSet dataSet) {
        success();
        setPayload(dataSet);
        return this;
    }

    public JdbcMessage dataSet(String dataSet) {
        success();
        setPayload(dataSet);
        return this;
    }

    public JdbcMessage dataSet(Resource dataSet) {
        success();
        try {
            setPayload(FileUtils.readToString(dataSet));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read data set file", e);
        }
        return this;
    }

    public JdbcMessage success() {
        setHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS, true);
        return this;
    }

    public JdbcMessage error() {
        setHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS, false);
        return this;
    }

    @Override
    public <T> T getPayload(Class<T> type) {
        if (String.class.equals(type)) {
            return (T) getPayload();
        } else {
            return super.getPayload(type);
        }
    }

    @Override
    public Object getPayload() {
        StringResult payloadResult = new StringResult();
        if (operation != null) {
            marshaller.marshal(operation, payloadResult);
            return payloadResult.toString();
        }

        return super.getPayload();
    }

}
