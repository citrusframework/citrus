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
import com.consol.citrus.jdbc.config.annotation.JdbcServerConfig;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.server.JdbcServer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

@Test
public class JdbcStatementsIT extends TestNGCitrusTestDesigner{

    @CitrusEndpoint
    @JdbcServerConfig(
            databaseName = "testdb",
            autoStart = true,
            port = 4568,
            autoCreateStatement = false)
    private JdbcServer jdbcServer;

    private JdbcDriver jdbcDriver = new JdbcDriver();
    private String serverUrl = "jdbc:citrus:localhost:4568?database=testdb";

    @AfterMethod
    public void teardown(){
        jdbcServer.stop();
    }

    @CitrusTest
    public void testCreatePreparedStatementCredential() throws Exception{

        //GIVEN
        final Connection connection =
                jdbcDriver.connect(serverUrl, new Properties());
        final String sql = "SELECT whatever FROM table";

        //WHEN
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.createPreparedStatement(sql));
        assertNotNull(preparedStatement);
    }

    @CitrusTest
    public void testCreateStatement() throws Exception {

        //GIVEN
        final Connection connection =
                jdbcDriver.connect(serverUrl, new Properties());

        //WHEN
        final Statement statement = connection.createStatement();

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.createStatement());
        assertNotNull(statement);
    }

    @CitrusTest
    public void testCloseStatement() throws Exception {

        //GIVEN
        final Connection connection =
                jdbcDriver.connect(serverUrl, new Properties());

        final Statement statement = connection.createStatement();
        receive(jdbcServer)
                .message(JdbcMessage.createStatement());

        //WHEN
        statement.close();

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.closeStatement());
    }
}
