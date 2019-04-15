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

package com.consol.citrus.jdbc;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.db.driver.JdbcDriver;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jdbc.config.annotation.JdbcServerConfig;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.message.MessageType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
@Test
public class JdbcExecutionsIT extends TestNGCitrusTestDesigner{

    private static final int ROWS_UPDATED = 42;
    private static final String TEST_COLUMN_LABEL = "foo";
    private static final String TEST_COLUMN_VALUE = "bar";
    private static final String SAMPLE_UPDATE_SQL = "UPDATE something WHERE condition";
    private final String testDataset = String.format("[ { \"%s\": \"%s\" } ]", TEST_COLUMN_LABEL, TEST_COLUMN_VALUE);

    @SuppressWarnings("unused")
    @CitrusEndpoint
    @JdbcServerConfig(
            databaseName = "testdb",
            autoStart = true,
            port = 4570)
    private JdbcServer jdbcServer;

    private JdbcDriver jdbcDriver = new JdbcDriver();
    private String serverUrl = "jdbc:citrus:localhost:4570?database=testdb";

    @AfterMethod
    public void teardown(){
        jdbcServer.stop();
    }

    @CitrusTest
    public void testExecuteQuery() {
        final String sql = "SELECT whatever FROM somewhere";

        async().actions(
                new AbstractTestAction() {
                    @Override
                    public void doExecute(final TestContext context) {
                        try {
                            final Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                            Assert.assertNotNull(connection);
                            try(final Statement statement = connection.createStatement();
                                final ResultSet resultSet  = statement.executeQuery(sql)){
                                assertTrue(resultSet.next());
                                assertEquals(TEST_COLUMN_VALUE, resultSet.getString(TEST_COLUMN_LABEL));
                            }
                        } catch (final SQLException | AssertionError e) {
                            throw new CitrusRuntimeException(e);
                        }
                    }
                }
        );

        receive(jdbcServer)
                .message(JdbcMessage.execute(sql));

        send(jdbcServer)
                .messageType(MessageType.JSON)
                .message(JdbcMessage.success().dataSet(testDataset));
    }

    @CitrusTest
    public void testExecuteStatement() {
        final String sql = "{CALL someFunction(?)}";

        async().actions(new AbstractTestAction() {
            @Override
            public void doExecute(final TestContext context) {
                try {
                    final Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                    Assert.assertNotNull(connection);

                    try(final CallableStatement statement = connection.prepareCall(sql)){
                        statement.setInt(1, 5);
                        final boolean isResultSet  = statement.execute();
                        try(final ResultSet resultSet = statement.getResultSet()){
                            assertTrue(isResultSet);
                            assertTrue(resultSet.next());
                            assertEquals(TEST_COLUMN_VALUE, resultSet.getString(TEST_COLUMN_LABEL));
                        }
                    }
                } catch (final SQLException | AssertionError e ) {
                    throw new CitrusRuntimeException(e);
                }
            }
        });

        receive(jdbcServer)
                .message(JdbcMessage.execute(sql + " - (5)"));

        send(jdbcServer)
                .messageType(MessageType.JSON)
                .message(JdbcMessage.success().dataSet(testDataset));
    }

    @CitrusTest
    public void testExecuteUpdate() {

        async().actions(new AbstractTestAction() {
                            @Override
                            public void doExecute(final TestContext context) {
                                try {
                                    final Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                                    Assert.assertNotNull(connection);
                                    try(final Statement statement = connection.createStatement()){
                                        final int updatedRows = statement.executeUpdate(SAMPLE_UPDATE_SQL);
                                        assertEquals(ROWS_UPDATED, updatedRows);
                                    }
                                } catch (final SQLException | AssertionError e) {
                                    throw new CitrusRuntimeException(e);
                                }
                            }
                        }
        );

        receive(jdbcServer)
                .message(JdbcMessage.execute(SAMPLE_UPDATE_SQL));

        send(jdbcServer)
                .messageType(MessageType.JSON)
                .message(JdbcMessage.success().rowsUpdated(ROWS_UPDATED));
    }

    @CitrusTest
    public void testUpdateCountHandlingOnExecute() {

        async().actions(new AbstractTestAction() {
                            @Override
                            public void doExecute(final TestContext context) {
                                try {
                                    final Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                                    Assert.assertNotNull(connection);
                                    try(final Statement statement = connection.createStatement()){
                                        final boolean isResultSet = statement.execute(SAMPLE_UPDATE_SQL);
                                        assertFalse(isResultSet);
                                        assertEquals(ROWS_UPDATED, statement.getUpdateCount());
                                    }
                                } catch (final SQLException | AssertionError e) {
                                    throw new CitrusRuntimeException(e);
                                }
                            }
                        }
        );

        receive(jdbcServer)
                .message(JdbcMessage.execute(SAMPLE_UPDATE_SQL));

        send(jdbcServer)
                .messageType(MessageType.JSON)
                .message(JdbcMessage.success().rowsUpdated(ROWS_UPDATED));
    }

    @CitrusTest
    public void testBatchExecution() {
        final String sqlOne = SAMPLE_UPDATE_SQL;
        final String sqlTwo = "UPDATE somethingElse WHERE otherCondition";
        final int[] expectedUpdatedRows = new int[]{ROWS_UPDATED, ROWS_UPDATED * 2};


        async().actions(new AbstractTestAction() {
                            @Override
                            public void doExecute(final TestContext context) {
                                try {
                                    final Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                                    Assert.assertNotNull(connection);

                                    try(final Statement statement = connection.createStatement()){
                                        statement.addBatch(sqlOne);
                                        statement.addBatch(sqlTwo);
                                        final int[] updatedRows = statement.executeBatch();
                                        assertArrayEquals(updatedRows, expectedUpdatedRows);
                                    }
                                } catch (final SQLException | AssertionError e) {
                                    throw new CitrusRuntimeException(e);
                                }
                            }
                        }
        );

        receive(jdbcServer)
                .message(JdbcMessage.execute(sqlOne));

        send(jdbcServer)
                .messageType(MessageType.JSON)
                .message(JdbcMessage.success().rowsUpdated(ROWS_UPDATED));

        receive(jdbcServer)
                .message(JdbcMessage.execute(sqlTwo));

        send(jdbcServer)
                .messageType(MessageType.JSON)
                .message(JdbcMessage.success().rowsUpdated(ROWS_UPDATED * 2));
    }

    @CitrusTest
    public void testLargeBatchExecution() {
        final String sqlOne = SAMPLE_UPDATE_SQL;
        final long[] expectedUpdatedRows = new long[]{ROWS_UPDATED};


        async().actions(new AbstractTestAction() {
                            @Override
                            public void doExecute(final TestContext context) {
                                try {
                                    final Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                                    Assert.assertNotNull(connection);

                                    try(final Statement statement = connection.createStatement()){
                                        statement.addBatch(sqlOne);
                                        final long[] updatedRows = statement.executeLargeBatch();
                                        assertArrayEquals(updatedRows, expectedUpdatedRows);
                                    }
                                } catch (final SQLException | AssertionError e) {
                                    throw new CitrusRuntimeException(e);
                                }
                            }
                        }
        );

        receive(jdbcServer)
                .message(JdbcMessage.execute(sqlOne));

        send(jdbcServer)
                .messageType(MessageType.JSON)
                .message(JdbcMessage.success().rowsUpdated(ROWS_UPDATED));
    }

    @CitrusTest
    public void testClobIntegration() {

        //GIVEN
        final String sql = "{? = CALL someClobFunction(?)}";
        final String clobRequestValue = "clobloblobloblob";
        final String clobReturnValue = "bolbolbolbolbolc";


        //WHEN + THEN
        async().actions(new AbstractTestAction() {
                            @Override
                            public void doExecute(final TestContext context) {
                                try {
                                    final Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                                    Assert.assertNotNull(connection);
                                    try(final PreparedStatement statement = connection.prepareStatement(sql)){

                                        final Clob requestClob = connection.createClob();
                                        requestClob.setString(1,clobRequestValue);
                                        statement.setClob(1, requestClob);

                                        statement.execute();

                                        final ResultSet resultSet = statement.getResultSet();
                                        resultSet.next();

                                        final Clob responseClob = resultSet.getClob(1);

                                        assertEquals(clobReturnValue,
                                                responseClob.getSubString(1, (int) requestClob.length()));
                                    }
                                } catch (final SQLException | AssertionError e) {
                                    throw new CitrusRuntimeException(e);
                                }
                            }
                        }
        );

        receive(jdbcServer)
                .message(JdbcMessage.execute("{? = CALL someClobFunction(?)} - ("+clobRequestValue+")"));

        send(jdbcServer)
                .messageType(MessageType.XML)
                .message(JdbcMessage.success().dataSet("" +
                        "<dataset>" +
                        "<row>" +
                        "<RETURN_CLOB>"+clobReturnValue+"</RETURN_CLOB>"+
                        "</row>" +
                        "</dataset>"));
    }

    @CitrusTest
    public void testBlobIntegration() throws IOException {

        //GIVEN
        final String sql = "{? = CALL someClobFunction(?)}";

        final ClassPathResource blobRequestValue = new ClassPathResource("jdbc/RequestBlob.pdf");
        final String requestBlobContent = Base64.encodeBase64String(IOUtils.toByteArray(blobRequestValue.getInputStream()));

        final ClassPathResource blobReturnValue = new ClassPathResource("jdbc/ResponseBlob.pdf");
        final String responseBlobContent = Base64.encodeBase64String(IOUtils.toByteArray(blobReturnValue.getInputStream()));

        //WHEN + THEN
        async().actions(new AbstractTestAction() {
                            @Override
                            public void doExecute(final TestContext context) {
                                try {
                                    final Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                                    Assert.assertNotNull(connection);
                                    try(final PreparedStatement statement = connection.prepareStatement(sql)){

                                        statement.setBlob(1, blobRequestValue.getInputStream());
                                        statement.execute();

                                        final ResultSet resultSet = statement.getResultSet();
                                        resultSet.next();

                                        final Blob responseBlob = resultSet.getBlob(1);

                                        assertEquals(
                                                IOUtils.toString(blobReturnValue.getInputStream(), "UTF8"),
                                                IOUtils.toString(responseBlob.getBinaryStream(),"UTF8"));
                                    }
                                } catch (final SQLException | AssertionError | IOException e) {
                                    throw new CitrusRuntimeException(e);
                                }
                            }
                        }
        );

        receive(jdbcServer)
                .message(JdbcMessage.execute("{? = CALL someClobFunction(?)} - ("+requestBlobContent+")"));

        send(jdbcServer)
                .messageType(MessageType.XML)
                .message(JdbcMessage.success().dataSet("" +
                        "<dataset>" +
                        "<row>" +
                        "<RETURN_BLOB>"+responseBlobContent+"</RETURN_BLOB>"+
                        "</row>" +
                        "</dataset>"));
    }
}
