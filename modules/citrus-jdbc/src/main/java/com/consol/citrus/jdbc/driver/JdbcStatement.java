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

import com.consol.citrus.Citrus;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jdbc.model.ResultSet;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.*;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcStatement implements Statement {

    private final HttpClient httpClient;
    private final String serverUrl;

    protected ResultSet resultSet;

    /**
     * Default constructor using remote client reference.
     * @param httpClient
     */
    public JdbcStatement(HttpClient httpClient, String serverUrl) {
        this.httpClient = httpClient;
        this.serverUrl = serverUrl;
    }

    @Override
    public java.sql.ResultSet executeQuery(String sqlQuery) throws SQLException {
        HttpResponse response = null;
        try {
            response = httpClient.execute(RequestBuilder.post(serverUrl + "/query")
                    .setEntity(new StringEntity(sqlQuery, ContentType.create("text/plain", Citrus.CITRUS_FILE_ENCODING)))
                    .build());

            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                throw new SQLException("Failed to execute query: " + EntityUtils.toString(response.getEntity()));
            }

            resultSet = (ResultSet) JAXBContext.newInstance(ResultSet.class).createUnmarshaller().unmarshal(response.getEntity().getContent());
            return new JdbcResultSet(resultSet);
        } catch (IOException | JAXBException e) {
            throw new CitrusRuntimeException(e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        HttpResponse response = null;
        try {
            response = httpClient.execute(RequestBuilder.post(serverUrl + "/update")
                    .setEntity(new StringEntity(sql, ContentType.create("text/plain", Citrus.CITRUS_FILE_ENCODING)))
                    .build());

            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                throw new SQLException("Failed to execute update: " + EntityUtils.toString(response.getEntity()));
            }

            String responseBody = EntityUtils.toString(response.getEntity());
            return Integer.valueOf(responseBody);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        HttpResponse response = null;
        try {
            response = httpClient.execute(RequestBuilder.post(serverUrl + "/execute")
                    .setEntity(new StringEntity(sql, ContentType.create("text/plain", Citrus.CITRUS_FILE_ENCODING)))
                    .build());

            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                throw new SQLException("Failed to execute statement: " + EntityUtils.toString(response.getEntity()));
            }
            return true;
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void close() throws SQLException {
        HttpResponse response = null;
        try {
            response = httpClient.execute(RequestBuilder.delete(serverUrl + "/statement")
                    .build());

            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 299) {
                throw new SQLException("Failed to close statement");
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } finally {
            HttpClientUtils.closeQuietly(response);
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
