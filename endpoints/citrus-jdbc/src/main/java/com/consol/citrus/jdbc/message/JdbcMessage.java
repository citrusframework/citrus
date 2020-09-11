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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jdbc.generator.JdbcOperationGenerator;
import com.consol.citrus.jdbc.model.JdbcMarshaller;
import com.consol.citrus.jdbc.model.OpenConnection;
import com.consol.citrus.jdbc.model.Operation;
import com.consol.citrus.jdbc.model.OperationResult;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.xml.StringResult;
import com.consol.citrus.xml.StringSource;
import org.springframework.core.io.Resource;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class JdbcMessage extends DefaultMessage {

    private OperationResult operationResult;
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
    private JdbcMessage(Operation operation) {
        super(operation);
        this.operation = operation;
    }

    /**
     * Constructor initializes new JDBC operation result.
     * @param operationResult The operation result to encapsulate in the message
     */
    private JdbcMessage(OperationResult operationResult) {
        super(operationResult);
        this.operationResult = operationResult;
    }

    public static JdbcMessage openConnection(OpenConnection.Property ... properties) {
        OpenConnection openConnection = new OpenConnection();
        if (properties.length > 0) {
            openConnection.getProperties().addAll(Arrays.asList(properties));
        }
        return new JdbcMessage(operationGenerator.generateOpenConnection(openConnection));
    }

    public static JdbcMessage openConnection(List<OpenConnection.Property> properties) {
        OpenConnection openConnection = new OpenConnection();
        openConnection.getProperties().addAll(properties);
        return new JdbcMessage(operationGenerator.generateOpenConnection(openConnection));
    }

    public static JdbcMessage closeConnection() {
        return new JdbcMessage(operationGenerator.generateCloseConnection());
    }

    public static JdbcMessage createPreparedStatement(String sql) {
        return new JdbcMessage(operationGenerator.generatePreparedStatement(sql));
    }

    public static JdbcMessage createStatement() {
        return new JdbcMessage(operationGenerator.generateCreateStatement());
    }

    public static JdbcMessage closeStatement() {
        return new JdbcMessage(operationGenerator.generateCloseStatement());
    }

    public static JdbcMessage execute(String sql) {
        return new JdbcMessage(operationGenerator.generateExecuteStatement(sql));
    }

    public static JdbcMessage success() {
        return result(true);
    }

    public static JdbcMessage error() {
        return result(false);
    }

    private static JdbcMessage result(boolean success) {
        OperationResult operationResult = new OperationResult();
        operationResult.setSuccess(success);

        JdbcMessage message = new JdbcMessage(operationResult);
        message.setHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS, success);
        return message;
    }

    public static JdbcMessage result(OperationResult operationResult) {
        JdbcMessage message = new JdbcMessage(operationResult);
        message.setHeader(JdbcMessageHeaders.JDBC_SERVER_SUCCESS, operationResult.isSuccess());

        if (!operationResult.isSuccess()) {
            message.exception(operationResult.getException());
        }

        return message;
    }

    public JdbcMessage exception(String message) {
        if (operationResult == null) {
            throw new CitrusRuntimeException("Invalid access to operation result exception for JDBC message");
        }

        if (operationResult.isSuccess()) {
            throw new CitrusRuntimeException("Unable to set operation result exception on 'success' JDBC result");
        }

        setHeader(JdbcMessageHeaders.JDBC_SERVER_EXCEPTION, message);
        return this;
    }

    public JdbcMessage rowsUpdated(int number) {
        if (operationResult == null) {
            throw new CitrusRuntimeException("Invalid access to operation result exception for JDBC message");
        }

        operationResult.setAffectedRows(number);
        setHeader(JdbcMessageHeaders.JDBC_ROWS_UPDATED, number);
        return this;
    }

    public JdbcMessage dataSet(String dataSet) {
        if (operationResult == null) {
            throw new CitrusRuntimeException("Invalid access to operation result exception for JDBC message");
        }

        operationResult.setDataSet(dataSet);
        return this;
    }

    public JdbcMessage dataSet(Resource dataSet) {
        try {
            dataSet(FileUtils.readToString(dataSet));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read data set file", e);
        }
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

    public static Message createCallableStatement(String sql) {
        return new JdbcMessage(operationGenerator.generateCreateCallableStatement(sql));
    }

    @Override
    public <T> T getPayload(Class<T> type) {
        if (Operation.class.equals(type)) {
            return (T) getOperation();
        } else if (OperationResult.class.equals(type)) {
            return (T) getOperationResult();
        } else if (String.class.equals(type)) {
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
        } else if (operationResult != null) {
            marshaller.marshal(operationResult, payloadResult);
            return payloadResult.toString();
        }

        return super.getPayload();
    }

    /**
     * Gets the operation result if any or tries to unmarshal String payload representation to an operation result model.
     * @return
     */
    private OperationResult getOperationResult() {
        if (operationResult == null) {
            this.operationResult = (OperationResult) marshaller.unmarshal(new StringSource(getPayload(String.class)));
        }

        return operationResult;
    }

    /**
     * Gets the operation if any or tries to unmarshal String payload representation to an operation model.
     * @return
     */
    private Operation getOperation() {
        if (operation == null) {
            this.operation = (Operation) marshaller.unmarshal(new StringSource(getPayload(String.class)));
        }

        return operation;
    }
}
