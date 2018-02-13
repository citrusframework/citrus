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
import com.consol.citrus.jdbc.config.annotation.JdbcServerConfig;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.message.MessageType;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("SqlNoDataSourceInspection")
@Test
public class JdbcExecutionsIT extends TestNGCitrusTestDesigner{

    @CitrusEndpoint
    @JdbcServerConfig(
            databaseName = "testdb",
            autoStart = true,
            port = 4567)
    private JdbcServer jdbcServer;

    private JdbcDriver jdbcDriver = new JdbcDriver();

    @AfterMethod
    public void teardown(){
        jdbcServer.stop();
    }

    @CitrusTest
    public void textExecuteQuery() throws Exception{

        final Connection connection =
                jdbcDriver.connect("jdbc:citrus:localhost:4567?database=testdb", new Properties());
        final Statement statement = connection.createStatement();
        final String sql = "SELECT whatever FROM somewhere";

        action(new AbstractTestAction() {
            @Override
            public void doExecute(final TestContext context) {
                Executors.newSingleThreadExecutor().submit(() -> {
                        try {
                            final ResultSet resultSet  = statement.executeQuery(sql);

                            assertTrue(resultSet.next());
                            assertEquals(resultSet.getString("foo"), "bar");
                        } catch (final SQLException e) {
                            e.printStackTrace();
                        }

                    }
                );
            }
        });

        receive(jdbcServer)
                .messageType(MessageType.JSON)
                .message(JdbcMessage.execute(sql));

        send(jdbcServer)
                .message(JdbcMessage.result().dataSet("[ { \"foo\": \"bar\" } ]"));
    }

    @CitrusTest
    public void textExecuteStatement() throws Exception{

        final Connection connection =
                jdbcDriver.connect("jdbc:citrus:localhost:4567?database=testdb", new Properties());
        final Statement statement = connection.createStatement();
        final String sql = "some statement";

        statement.execute(sql);

        receive(jdbcServer)
                .message(JdbcMessage.execute(sql));
    }

    @CitrusTest
    public void textExecuteUpdate() throws Exception{

        final Connection connection =
                jdbcDriver.connect("jdbc:citrus:localhost:4567?database=testdb", new Properties());
        final Statement statement = connection.createStatement();
        final String sql = "UPDATE something WHERE condition";

        action(new AbstractTestAction() {
            @Override
            public void doExecute(final TestContext context) {
                Executors.newSingleThreadExecutor().submit(() -> {
                            try {
                                final int updatedRows = statement.executeUpdate(sql);

                                assertEquals(updatedRows, 42);
                            } catch (final SQLException e) {
                                e.printStackTrace();
                            }

                        }
                );
            }
        });

        receive(jdbcServer)
                .message(JdbcMessage.execute(sql));

        send(jdbcServer)
                .message(JdbcMessage.result().rowsUpdated(42));
    }
}
