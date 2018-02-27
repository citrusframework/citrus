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
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.sql.*;
import java.util.Properties;

import static org.junit.Assert.*;

@Test
public class JdbcTransactionsIT extends TestNGCitrusTestDesigner{

    @CitrusEndpoint
    @JdbcServerConfig(
            databaseName = "testdb",
            autoStart = true,
            port = 4569,
            autoTransactionHandling = false)
    private JdbcServer jdbcServer;

    private JdbcDriver jdbcDriver = new JdbcDriver();
    private String serverUrl = "jdbc:citrus:localhost:4569?database=testdb";

    @AfterMethod
    public void teardown(){
        jdbcServer.stop();
    }

    @CitrusTest
    public void testStartTransaction() {
        //GIVEN
        async().actions(new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    try {
                        Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                        Assert.assertNotNull(connection);

                        //WHEN
                        connection.setAutoCommit(false);
                    } catch (SQLException e) {
                        throw new CitrusRuntimeException(e);
                    }
                }
            }
        );

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.startTransaction());
    }

    @CitrusTest
    public void testTransactionStateIsStored() {
        //GIVEN
        async().actions(new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    try {
                        Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                        Assert.assertNotNull(connection);

                        //WHEN
                        connection.setAutoCommit(false);
                        boolean shouldBeFalse = connection.getAutoCommit();
                        connection.setAutoCommit(true);
                        boolean shouldBeTrue = connection.getAutoCommit();

                        //THEN
                        assertFalse(shouldBeFalse);
                        assertTrue(shouldBeTrue);
                    } catch (SQLException e) {
                        throw new CitrusRuntimeException(e);
                    }
                }
            }
        );

        receive(jdbcServer)
                .message(JdbcMessage.startTransaction());
    }

    @CitrusTest
    public void testCommitTransaction() {
        //GIVEN
        async().actions(new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    try {
                        Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                        Assert.assertNotNull(connection);

                        //WHEN
                        connection.commit();
                    } catch (SQLException e) {
                        throw new CitrusRuntimeException(e);
                    }
                }
            }
        );

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.commitTransaction());
    }

    @CitrusTest
    public void testRollbackTransaction() {
        //GIVEN
        async().actions(new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    try {
                        Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                        Assert.assertNotNull(connection);

                        //WHEN
                        connection.rollback();
                    } catch (SQLException e) {
                        throw new CitrusRuntimeException(e);
                    }
                }
            }
        );

        //THEN
        receive(jdbcServer)
                .message(JdbcMessage.rollbackTransaction());
    }
}
