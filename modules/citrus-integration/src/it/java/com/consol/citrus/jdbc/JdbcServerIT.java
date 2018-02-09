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
import com.consol.citrus.jdbc.model.OpenConnection;
import com.consol.citrus.jdbc.server.JdbcServer;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.Properties;

public class JdbcServerIT extends TestNGCitrusTestDesigner {

    @CitrusEndpoint
    @JdbcServerConfig(
            autoStart=true,
            host="localhost",
            port=4567,
            databaseName = "testdb",
            autoConnect = false)
    private JdbcServer jdbcServer;

    private JdbcDriver jdbcDriver = new JdbcDriver();

    @Test
    @CitrusTest
    public void testOpenConnection() throws Exception{

        //GIVEN

        //WHEN
        jdbcDriver.connect("jdbc:citrus:localhost:4567?database=testdb", new Properties());

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.openConnection());
    }

    @Test
    @CitrusTest
    public void testOpenConnectionWithCredential() throws Exception{

        //GIVEN
        final Properties properties = new Properties();
        properties.setProperty("username", "user");
        properties.setProperty("password", "password");

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
        jdbcDriver.connect("jdbc:citrus:localhost:4567?database=testdb", properties);

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.openConnection(username, password, database));
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
        jdbcDriver.connect("jdbc:citrus:localhost:4567?database=testdb", properties);

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.openConnection(username, password, database));
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    @CitrusTest
    public void testCloseConnectionWithWrongCredential() throws Exception{

        //GIVEN
        final Connection connection =
                jdbcDriver.connect("jdbc:citrus:localhost:4567?database=testdb", new Properties());

        //WHEN
        connection.close();

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.closeConnection());
    }

}
