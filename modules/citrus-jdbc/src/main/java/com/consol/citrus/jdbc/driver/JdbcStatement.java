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

import com.consol.citrus.jdbc.model.ResultSet;
import com.consol.citrus.jdbc.server.RemoteStatement;

import java.rmi.RemoteException;
import java.sql.*;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcStatement implements Statement {

    //Remote Statement
    private final RemoteStatement remoteStmt;

    /**
     * Constructor for creating the JWStatement
     */
    public JdbcStatement(RemoteStatement stmt) {
        remoteStmt = stmt;
    }

    @Override
    public java.sql.ResultSet executeQuery(String sqlQuery) throws SQLException {
        try {
            ResultSet remoteRsInstance = remoteStmt.executeQuery(sqlQuery);
            return new JdbcResultSet(remoteRsInstance);
        } catch (RemoteException ex) {
            throw (new SQLException(ex));
        }
    }

    @Override
    public int executeUpdate(String sqlQuery) throws SQLException {
        try {
            return remoteStmt.executeUpdate(sqlQuery);
        } catch (RemoteException ex) {
            throw (new SQLException(ex));
        }
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return executeUpdate(sql) > 0;
    }

    @Override
    public void close() throws SQLException {
        try {
            remoteStmt.closeStatement();
        } catch (RemoteException ex) {
            throw (new SQLException(ex));
        }
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public int getMaxRows() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void cancel() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void clearWarnings() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public java.sql.ResultSet getResultSet() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public int getUpdateCount() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public int getFetchDirection() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public int getFetchSize() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public int getResultSetType() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void clearBatch() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public int[] executeBatch() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public Connection getConnection() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public java.sql.ResultSet getGeneratedKeys() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public boolean isClosed() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public boolean isPoolable() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public long getLargeMaxRows() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
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
