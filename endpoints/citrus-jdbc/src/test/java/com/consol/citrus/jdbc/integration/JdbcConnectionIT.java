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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.db.driver.JdbcDriver;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.jdbc.config.annotation.JdbcServerConfig;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.model.OpenConnection;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.testng.TestNGCitrusSupport;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static com.consol.citrus.DefaultTestActionBuilder.action;
import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.container.Assert.Builder.assertException;
import static com.consol.citrus.container.Async.Builder.async;

@SuppressWarnings("SqlNoDataSourceInspection")
@Test
public class JdbcConnectionIT extends TestNGCitrusSupport {

    @CitrusEndpoint
    @JdbcServerConfig(
            databaseName = "testdb",
            autoStart = true,
            timeout = 1000L,
            port = 4567,
            autoConnect = false)
    private JdbcServer jdbcServer;

    private final JdbcDriver jdbcDriver = new JdbcDriver();
    private final String serverUrl = "jdbc:citrus:localhost:4567?database=testdb";

    @AfterMethod
    public void teardown(){
        jdbcServer.stop();
    }

    @CitrusTest
    public void testOpenConnection() {
        //GIVEN

        //WHEN
        when(action(context -> {
                try {
                    Connection connection = jdbcDriver.connect(serverUrl, new Properties());
        //THEN
                    Assert.assertNotNull(connection);
                } catch (SQLException e) {
                    throw new CitrusRuntimeException("Failed to connect", e);
                }
            })
        );
    }

    @CitrusTest
    public void testConnectionWithWithVerification() {
        //GIVEN
        OpenConnection.Property database = new OpenConnection.Property();
        database.setName("database");
        database.setValue("testdb");

        //WHEN
        when(async().actions(
            action(context -> {
                try {
                    Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                    Assert.assertNotNull(connection);
                } catch (SQLException e) {
                    throw new CitrusRuntimeException("Failed to connect", e);
                }
            })
        ));

        //THEN
        then(receive(jdbcServer).message(JdbcMessage.openConnection(database)));
        and(send(jdbcServer).message(JdbcMessage.success()));
    }

    @CitrusTest
    public void testOpenConnectionWithWrongCredentials() {
        //GIVEN
        Properties properties = new Properties();
        properties.setProperty("username", "wrongUser");
        properties.setProperty("password", "wrongPassword");

        OpenConnection.Property database = new OpenConnection.Property();
        database.setName("database");
        database.setValue("testdb");

        OpenConnection.Property username = new OpenConnection.Property();
        username.setName("username");
        username.setValue("user");

        OpenConnection.Property password = new OpenConnection.Property();
        password.setName("password");
        password.setValue("password");

        //WHEN
        when(async().actions(
            action(context -> {
                try {
                    jdbcDriver.connect(serverUrl, properties);
                } catch (SQLException e) {
                    Assert.assertTrue(e.getMessage().contains("java.sql.SQLException: Failed to connect to server"));
                    return;
                }

                throw new CitrusRuntimeException("Missing exception for failed connection");
            })
        ));

        //THEN
        then(assertException()
                .exception(ValidationException.class)
                .when(receive(jdbcServer)
                                .message(JdbcMessage.openConnection(username, password, database))));

        and(send(jdbcServer).message(JdbcMessage.error()));
    }

    @CitrusTest
    public void testCloseConnection() {
        //GIVEN
        OpenConnection.Property database = new OpenConnection.Property();
        database.setName("database");
        database.setValue("testdb");

        given(async().actions(
            action(context -> {
                try {
                    Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                    Assert.assertNotNull(connection);
    //WHEN
                    connection.close();
                } catch (SQLException e) {
                    throw new CitrusRuntimeException("Failed to connect", e);
                }
            })
        ));

        when(receive(jdbcServer)
                .message(JdbcMessage.openConnection(database)));

        and(send(jdbcServer).message(JdbcMessage.success()));

        //THEN
        then(receive(jdbcServer)
                .message(JdbcMessage.closeConnection()));

        and(send(jdbcServer).message(JdbcMessage.success()));
    }
}
