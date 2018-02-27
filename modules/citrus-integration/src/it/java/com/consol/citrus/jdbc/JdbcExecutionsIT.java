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
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.sql.*;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("SqlNoDataSourceInspection")
@Test
public class JdbcExecutionsIT extends TestNGCitrusTestDesigner{

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
    public void textExecuteQuery() {
        String sql = "SELECT whatever FROM somewhere";

        async().actions(
            new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    try {
                        Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                        Assert.assertNotNull(connection);

                        Statement statement = connection.createStatement();
                        ResultSet resultSet  = statement.executeQuery(sql);

                        assertTrue(resultSet.next());
                        assertEquals(resultSet.getString("foo"), "bar");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        );

        receive(jdbcServer)
                .message(JdbcMessage.execute(sql));

        send(jdbcServer)
                .messageType(MessageType.JSON)
                .message(JdbcMessage.success().dataSet("[ { \"foo\": \"bar\" } ]"));
    }

    @CitrusTest
    public void textExecuteStatement() {
        String sql = "{CALL someFunction(?)}";

        async().actions(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                try {
                    Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                    Assert.assertNotNull(connection);

                    CallableStatement statement = connection.prepareCall(sql);
                    statement.setInt(1, 5);
                    boolean isResultSet  = statement.execute();
                    ResultSet resultSet = statement.getResultSet();

                    assertTrue(isResultSet);
                    assertTrue(resultSet.next());
                    assertEquals(resultSet.getString("foo"), "bar");
                } catch (SQLException e) {
                    throw new CitrusRuntimeException(e);
                }
            }
        });

        receive(jdbcServer)
                .message(JdbcMessage.execute(sql + " - (5)"));

        send(jdbcServer)
                .messageType(MessageType.JSON)
                .message(JdbcMessage.success().dataSet("[ { \"foo\": \"bar\" } ]"));
    }

    @CitrusTest
    public void textExecuteUpdate() {
        String sql = "UPDATE something WHERE condition";

        async().actions(new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    try {
                        Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                        Assert.assertNotNull(connection);

                        Statement statement = connection.createStatement();
                        int updatedRows = statement.executeUpdate(sql);
                        assertEquals(updatedRows, 42);
                    } catch (SQLException e) {
                        throw new CitrusRuntimeException(e);
                    }
                }
            }
        );

        receive(jdbcServer)
                .message(JdbcMessage.execute(sql));

        send(jdbcServer)
                .message(JdbcMessage.success().rowsUpdated(42));
    }
}
