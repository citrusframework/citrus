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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.sql.*;
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
        String sql = "SELECT whatever FROM table";

        //GIVEN
        async().actions(new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    try {
                        Connection connection = jdbcDriver.connect(serverUrl, new Properties());

                        //WHEN
                        PreparedStatement preparedStatement = connection.prepareStatement(sql);
                        assertNotNull(preparedStatement);
                    } catch (SQLException e) {
                        throw new CitrusRuntimeException(e);
                    }
                }
            }
        );

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.createPreparedStatement(sql));
    }

    @CitrusTest
    public void testCreateStatement() {
        //GIVEN
        async().actions(new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    try {
                        Connection connection = jdbcDriver.connect(serverUrl, new Properties());

                        //WHEN
                        Statement statement = connection.createStatement();
                        assertNotNull(statement);
                    } catch (SQLException e) {
                        throw new CitrusRuntimeException(e);
                    }
                }
            }
        );

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.createStatement());
    }

    @CitrusTest
    public void testCloseStatement() {
        //GIVEN
        async().actions(new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    try {
                        Connection connection = jdbcDriver.connect(serverUrl, new Properties());

                        Statement statement = connection.createStatement();
                        assertNotNull(statement);
                        //WHEN
                        statement.close();
                    } catch (SQLException e) {
                        throw new CitrusRuntimeException(e);
                    }
                }
            }
        );
        receive(jdbcServer)
                .message(JdbcMessage.createStatement());

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.closeStatement());
    }

    @CitrusTest
    public void testCreateCallableStatementCredential() throws Exception{
        String sql = "SELECT whatever FROM table";

        //GIVEN
        async().actions(new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    try {
                        Connection connection = jdbcDriver.connect(serverUrl, new Properties());

                        //WHEN
                        CallableStatement callableStatement = connection.prepareCall(sql);
                        assertNotNull(callableStatement);
                    } catch (SQLException e) {
                        throw new CitrusRuntimeException(e);
                    }
                }
            }
        );

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.createCallableStatement(sql));
    }
}
