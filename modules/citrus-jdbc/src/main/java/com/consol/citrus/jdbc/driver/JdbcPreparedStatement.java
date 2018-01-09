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

import org.apache.http.client.HttpClient;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcPreparedStatement extends JdbcStatement implements PreparedStatement {

    private final String preparedStatement;

    public JdbcPreparedStatement(HttpClient httpClient, String preparedStatement, String serverUrl) {
        super(httpClient, serverUrl);
        this.preparedStatement = preparedStatement;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return super.executeQuery(preparedStatement);
    }

    @Override
    public int executeUpdate() throws SQLException {
        return super.executeUpdate(preparedStatement);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
    }

    @Override
    public void clearParameters() throws SQLException {
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
    }

    @Override
    public boolean execute() throws SQLException {
        return executeUpdate() > 0;
    }

    @Override
    public void addBatch() throws SQLException {
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return new JdbcResultSetMetaData(resultSet);
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return null;
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
    }
}
