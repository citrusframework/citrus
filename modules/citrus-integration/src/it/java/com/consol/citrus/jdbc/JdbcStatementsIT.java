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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
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
    private final String SELECT_STATEMENT = "SELECT whatever FROM table";

    @AfterMethod
    public void teardown(){
        jdbcServer.stop();
    }

    @CitrusTest
    public void testCreatePreparedStatementCredential(){

        //GIVEN
        async().actions(new AbstractTestAction() {
                @Override
                public void doExecute(final TestContext context) {
                    try {
                        final Connection connection = jdbcDriver.connect(serverUrl, new Properties());

                        //WHEN
                        final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_STATEMENT);
                        assertNotNull(preparedStatement);
                    } catch (final SQLException | AssertionError e ) {
                        throw new CitrusRuntimeException(e);
                    }
                }
            }
        );

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.createPreparedStatement(SELECT_STATEMENT));
    }

    @CitrusTest
    public void testCreateStatement() {
        //GIVEN
        async().actions(new AbstractTestAction() {
                @Override
                public void doExecute(final TestContext context) {
                    try {
                        final Connection connection = jdbcDriver.connect(serverUrl, new Properties());

                        //WHEN
                        final Statement statement = connection.createStatement();
                        assertNotNull(statement);
                    } catch (final SQLException | AssertionError e ) {
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
                public void doExecute(final TestContext context) {
                    try {
                        final Connection connection = jdbcDriver.connect(serverUrl, new Properties());

                        final Statement statement = connection.createStatement();
                        assertNotNull(statement);
                        //WHEN
                        statement.close();
                    } catch (final SQLException | AssertionError e ) {
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
    public void testCreateCallableStatementCredential(){

        //GIVEN
        async().actions(new AbstractTestAction() {
                @Override
                public void doExecute(final TestContext context) {
                    try {
                        final Connection connection = jdbcDriver.connect(serverUrl, new Properties());

                        //WHEN
                        final CallableStatement callableStatement = connection.prepareCall(SELECT_STATEMENT);
                        assertNotNull(callableStatement);
                    } catch (final SQLException | AssertionError e ) {
                        throw new CitrusRuntimeException(e);
                    }
                }
            }
        );

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.createCallableStatement(SELECT_STATEMENT));
    }
}
