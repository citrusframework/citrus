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

package com.consol.citrus.jdbc.integration;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.db.driver.JdbcDriver;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jdbc.config.annotation.JdbcServerConfig;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.TestNGCitrusSupport;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static com.consol.citrus.DefaultTestActionBuilder.action;
import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.container.Async.Builder.async;

@SuppressWarnings("SqlNoDataSourceInspection")
@Test
public class JdbcExecutionsIT extends TestNGCitrusSupport {

    @CitrusEndpoint
    @JdbcServerConfig(
            databaseName = "testdb",
            timeout = 1000L,
            autoStart = true,
            port = 4570)
    private JdbcServer jdbcServer;

    private final JdbcDriver jdbcDriver = new JdbcDriver();
    private final String serverUrl = "jdbc:citrus:localhost:4570?database=testdb";

    @AfterMethod
    public void teardown(){
        jdbcServer.stop();
    }

    @CitrusTest
    public void textExecuteQuery() {
        String sql = "SELECT whatever FROM somewhere";

        given(async().actions(
            action(context -> {
                try {
                    Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                    Assert.assertNotNull(connection);

                    Statement statement = connection.createStatement();
                    ResultSet resultSet  = statement.executeQuery(sql);

                    Assert.assertTrue(resultSet.next());
                    Assert.assertEquals(resultSet.getString("foo"), "bar");
                } catch (SQLException e) {
                    throw new CitrusRuntimeException(e);
                }
            })
        ));

        then(receive(jdbcServer)
                .message(JdbcMessage.execute(sql)));

        then(send(jdbcServer)
                .message(JdbcMessage.success().dataSet("[ { \"foo\": \"bar\" } ]"))
                .type(MessageType.JSON));
    }

    @CitrusTest
    public void textExecuteStatement() {
        String sql = "{CALL someFunction(?)}";

        given(async().actions(action(context -> {
            try {
                Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                Assert.assertNotNull(connection);

                CallableStatement statement = connection.prepareCall(sql);
                statement.setInt(1, 5);
                boolean isResultSet  = statement.execute();
                ResultSet resultSet = statement.getResultSet();

                Assert.assertTrue(isResultSet);
                Assert.assertTrue(resultSet.next());
                Assert.assertEquals(resultSet.getString("foo"), "bar");
            } catch (SQLException e) {
                throw new CitrusRuntimeException(e);
            }
        })));

        then(receive(jdbcServer)
                .message(JdbcMessage.execute(sql + " - (5)")));

        then(send(jdbcServer)
                .message(JdbcMessage.success().dataSet("[ { \"foo\": \"bar\" } ]"))
                .type(MessageType.JSON));
    }

    @CitrusTest
    public void textExecuteUpdate() {
        String sql = "UPDATE something WHERE condition";

        given(async().actions(action(context -> {
                try {
                    Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                    Assert.assertNotNull(connection);

                    Statement statement = connection.createStatement();
                    int updatedRows = statement.executeUpdate(sql);
                    Assert.assertEquals(updatedRows, 42);
                } catch (SQLException e) {
                    throw new CitrusRuntimeException(e);
                }
            })
        ));

        then(receive(jdbcServer)
                .message(JdbcMessage.execute(sql)));

        then(send(jdbcServer)
                .message(JdbcMessage.success().rowsUpdated(42)));
    }
}
