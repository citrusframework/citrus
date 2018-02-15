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
import com.consol.citrus.jdbc.generator.JdbcOperationGenerator;
import com.consol.citrus.jdbc.model.JdbcMarshaller;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.model.message.jdbc.OpenConnection;
import com.consol.citrus.model.message.jdbc.Operation;
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

    private static JdbcOperationGenerator operationGenerator = new JdbcOperationGenerator();

    /**
     * Prevent traditional instantiation.
     */
    private JdbcMessage() { super(); }

    /**
     * Constructor initializes new JDBC operation.
     * @param operation The Operation to encapsulate in the message
     */
    private JdbcMessage(final Operation operation) {
        super(operation);
        this.operation = operation;
    }

    public static JdbcMessage openConnection(final OpenConnection.Property ... properties) {
        final OpenConnection openConnection = new OpenConnection();
        if (properties.length > 0) {
            openConnection.getProperties().addAll(Arrays.asList(properties));
        }
        return new JdbcMessage(operationGenerator.generateOpenConnection(openConnection));
    }

    public static JdbcMessage openConnection(final List<OpenConnection.Property> properties) {
        final OpenConnection openConnection = new OpenConnection();
        openConnection.getProperties().addAll(properties);
        return new JdbcMessage(operationGenerator.generateOpenConnection(openConnection));
    }

    public static JdbcMessage closeConnection() {
        return new JdbcMessage(operationGenerator.generateCloseConnection());
    }

    public static JdbcMessage createPreparedStatement(final String sql) {
        return new JdbcMessage(operationGenerator.generatePreparedStatement(sql));
    }

    public static JdbcMessage createStatement() {
        return new JdbcMessage(operationGenerator.generateCreateStatement());
    }

    public static JdbcMessage closeStatement() {
        return new JdbcMessage(operationGenerator.generateCloseStatement());
    }

    public static JdbcMessage execute(final String sql) {
        return new JdbcMessage(operationGenerator.generateExecuteStatement(sql));
    }

    public static JdbcMessage result() {
        return result(true);
    }

    public static JdbcMessage result(final boolean success) {
        final JdbcMessage message = new JdbcMessage();
        message.setHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS, success);
        return message;
    }

    public JdbcMessage exception(final String message) {
        error();
        setHeader(JdbcMessageHeaders.JDBC_SERVER_EXCEPTION, message);
        return this;
    }

    public JdbcMessage rowsUpdated(final int number) {
        success();
        setHeader(JdbcMessageHeaders.JDBC_ROWS_UPDATED, number);
        return this;
    }

    public JdbcMessage dataSet(final DataSet dataSet) {
        success();
        setPayload(dataSet);
        return this;
    }

    public JdbcMessage dataSet(final String dataSet) {
        success();
        setPayload(dataSet);
        return this;
    }

    public JdbcMessage dataSet(final Resource dataSet) {
        success();
        try {
            setPayload(FileUtils.readToString(dataSet));
        } catch (final IOException e) {
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

    public static Message startTransaction() {
        return new JdbcMessage(operationGenerator.generateTransactionStarted());
    }

    public static Message commitTransaction(){
        return new JdbcMessage(operationGenerator.generateTransactionCommitted());
    }

    public static Message rollbackTransaction(){
        return new JdbcMessage(operationGenerator.generateTransactionRollback());
    }

    public static Message createCallableStatement(final String sql) {
        return new JdbcMessage(operationGenerator.generateCreateCallableStatement(sql));
    }

    @Override
    public <T> T getPayload(final Class<T> type) {
        if (String.class.equals(type)) {
            return (T) getPayload();
        } else {
            return super.getPayload(type);
        }
    }

    @Override
    public Object getPayload() {
        final StringResult payloadResult = new StringResult();
        if (operation != null) {
            marshaller.marshal(operation, payloadResult);
            return payloadResult.toString();
        }

        return super.getPayload();
    }
}
