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
import com.consol.citrus.jdbc.config.annotation.JdbcServerConfig;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.testng.TestNGCitrusSupport;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static com.consol.citrus.DefaultTestActionBuilder.action;
import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.container.Async.Builder.async;

@Test
public class JdbcTransactionsIT extends TestNGCitrusSupport {

    @CitrusEndpoint
    @JdbcServerConfig(
            databaseName = "testdb",
            autoStart = true,
            port = 4569,
            timeout = 1000L,
            autoTransactionHandling = false)
    private JdbcServer jdbcServer;

    private final JdbcDriver jdbcDriver = new JdbcDriver();
    private final String serverUrl = "jdbc:citrus:localhost:4569?database=testdb";

    @AfterMethod
    public void teardown(){
        jdbcServer.stop();
    }

    @CitrusTest
    public void testStartTransaction() {
        //GIVEN
        given(async().actions(action(context -> {
                try {
                    Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                    Assert.assertNotNull(connection);

                    //WHEN
                    connection.setAutoCommit(false);
                } catch (SQLException e) {
                    throw new CitrusRuntimeException(e);
                }
            })
        ));

        //THEN
        then(receive(jdbcServer)
                .message(JdbcMessage.startTransaction()));

        and(send(jdbcServer).message(JdbcMessage.success()));
    }

    @CitrusTest
    public void testTransactionStateIsStored() {
        //GIVEN
        given(async().actions(action(context -> {
                try {
                    Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                    Assert.assertNotNull(connection);

                    //WHEN
                    connection.setAutoCommit(false);
                    boolean shouldBeFalse = connection.getAutoCommit();
                    connection.setAutoCommit(true);
                    boolean shouldBeTrue = connection.getAutoCommit();

                    //THEN
                    Assert.assertFalse(shouldBeFalse);
                    Assert.assertTrue(shouldBeTrue);
                } catch (SQLException e) {
                    throw new CitrusRuntimeException(e);
                }
            })
        ));

        then(receive(jdbcServer)
                .message(JdbcMessage.startTransaction()));

        and(send(jdbcServer).message(JdbcMessage.success()));
    }

    @CitrusTest
    public void testCommitTransaction() {
        //GIVEN
        given(async().actions(action(context -> {
                try {
                    Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                    Assert.assertNotNull(connection);

                    //WHEN
                    connection.commit();
                } catch (SQLException e) {
                    throw new CitrusRuntimeException(e);
                }
            })
        ));

        //THEN
        then(receive(jdbcServer)
                .message(JdbcMessage.commitTransaction()));

        and(send(jdbcServer).message(JdbcMessage.success()));
    }

    @CitrusTest
    public void testRollbackTransaction() {
        //GIVEN
        given(async().actions(action(context -> {
                try {
                    Connection connection = jdbcDriver.connect(serverUrl, new Properties());
                    Assert.assertNotNull(connection);

                    //WHEN
                    connection.rollback();
                } catch (SQLException e) {
                    throw new CitrusRuntimeException(e);
                }
            })
        ));

        //THEN
        then(receive(jdbcServer)
                .message(JdbcMessage.rollbackTransaction()));

        and(send(jdbcServer).message(JdbcMessage.success()));
    }
}
