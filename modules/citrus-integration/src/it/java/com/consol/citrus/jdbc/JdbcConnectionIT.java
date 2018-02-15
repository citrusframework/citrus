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

import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.db.driver.JdbcDriver;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.jdbc.config.annotation.JdbcServerConfig;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.model.message.jdbc.OpenConnection;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

@SuppressWarnings("SqlNoDataSourceInspection")
@Test
public class JdbcConnectionIT extends TestNGCitrusTestDesigner {

    @CitrusEndpoint
    @JdbcServerConfig(
            databaseName = "testdb",
            autoStart = true,
            port = 4567,
            autoConnect = false)
    private JdbcServer jdbcServer;

    private JdbcDriver jdbcDriver = new JdbcDriver();
    private String serverUrl = "jdbc:citrus:localhost:4567?database=testdb";

    @AfterMethod
    public void teardown(){
        jdbcServer.stop();
    }

    @CitrusTest
    public void testOpenConnection() throws Exception{

        //GIVEN

        //WHEN
        final Connection connection =
                jdbcDriver.connect(serverUrl, new Properties());

        //THEN
        assertNotNull(connection);
    }

    @CitrusTest
    public void testConnectionWithWithVerification() throws Exception{

        //GIVEN
        final OpenConnection.Property database = new OpenConnection.Property();
        database.setName("database");
        database.setValue("testdb");


        //WHEN
        final Connection connection =
                jdbcDriver.connect(serverUrl, new Properties());

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.openConnection(database));
        assertNotNull(connection);
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    @CitrusTest
    public void testOpenConnectionWithWrongCredential() throws Exception{

        //GIVEN
        final Properties properties = new Properties();
        properties.setProperty("username", "wrongUser");
        properties.setProperty("password", "wrongPassword");

        final OpenConnection.Property database = new OpenConnection.Property();
        database.setName("database");
        database.setValue("testdb");

        final OpenConnection.Property username = new OpenConnection.Property();
        username.setName("username");
        username.setValue("user");

        final OpenConnection.Property password = new OpenConnection.Property();
        password.setName("password");
        password.setValue("password");


        //WHEN
        jdbcDriver.connect(serverUrl, properties);

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.openConnection(username, password, database));
    }

    @CitrusTest
    public void testCloseConnection() throws Exception{

        //GIVEN
        final OpenConnection.Property database = new OpenConnection.Property();
        database.setName("database");
        database.setValue("testdb");

        final Connection connection =
                jdbcDriver.connect(serverUrl, new Properties());

        receive(jdbcServer)
                .message(JdbcMessage.openConnection(database));

        //WHEN
        connection.close();

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.closeConnection());
    }
}
