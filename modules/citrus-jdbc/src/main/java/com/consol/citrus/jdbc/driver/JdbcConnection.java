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

package com.consol.citrus.jdbc.driver;

import com.consol.citrus.jdbc.server.*;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcConnection implements Connection {

    /** Remote connection */
    private final RemoteConnection remoteConnection;

    /**
     * Default constructor using remote connection reference.
     * @param remoteConnection
     */
    public JdbcConnection(RemoteConnection remoteConnection) {
        this.remoteConnection = remoteConnection;
    }

    @Override
    public Statement createStatement() throws SQLException {
        try {
            RemoteStatement statement = remoteConnection.createStatement();
            return new JdbcStatement(statement);
        } catch(RemoteException ex) {
            throw(new SQLException("RemoteException: " + ex.getMessage()));
        } catch(Exception ex) {
            throw(new SQLException("LocalException: " + ex.getMessage()));
        }
    }

    @Override
    public void close() throws SQLException {
        try {
            remoteConnection.closeConnection();
        } catch(RemoteException ex) {
            throw ((new SQLException("RemoteException: " + ex.getMessage())));
        }
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return true;
    }

    @Override
    public void commit() throws SQLException {
    }

    @Override
    public void rollback() throws SQLException {
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return new JdbcDatabaseMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
    }

    @Override
    public String getCatalog() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void clearWarnings() throws SQLException {
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        try {
            RemoteStatement remoteStatement = remoteConnection.createPreparedStatement(sql);
            return new JdbcPreparedStatement(remoteStatement, sql);
        } catch(RemoteException ex) {
            throw(new SQLException("RemoteException: " + ex.getMessage()));
        } catch(Exception ex) {
            throw(new SQLException("LocalException: " + ex.getMessage()));
        }
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public PreparedStatement prepareStatement(String sql,int resultSetType,int resultSetConcurrency) throws SQLException {
        return prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql,int resultSetType,int resultSetConcurrency) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public Statement createStatement(int resultSetType,int resultSetConcurrency) throws SQLException {
        return createStatement();
    }

    @Override
    public void setTypeMap(Map map) throws SQLException {
    }

    @Override
    public Map getTypeMap() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
    }

    @Override
    public int getHoldability() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return prepareStatement(sql);
    }

    @Override
    public Clob createClob() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public Blob createBlob() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public NClob createNClob() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void setSchema(String schema) throws SQLException {
    }

    @Override
    public String getSchema() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void abort(Executor executor) throws SQLException {
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new SQLException("Not Supported");
    }
}