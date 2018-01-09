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

package com.consol.citrus.jdbc.server;

import com.consol.citrus.jdbc.model.ResultSet;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public interface JdbcController {

    /**
     * Get connection from db server.
     * @param properties
     * @return
     * @throws JdbcServerException
     */
    void getConnection(Map<String, String> properties) throws JdbcServerException;

    /**
     * Create statement request.
     * @return
     * @throws JdbcServerException
     */
    void createStatement() throws JdbcServerException;

    /**
     * Close connection request.
     * @throws JdbcServerException
     */
    void closeConnection() throws JdbcServerException;

    /**
     * Create new prepared statement.
     * @param stmt
     * @return
     * @throws JdbcServerException
     */
    void createPreparedStatement(String stmt) throws JdbcServerException;

    /**
     * Execute query statement
     * @param stmt
     * @throws JdbcServerException
     * @return
     */
    ResultSet executeQuery(String stmt) throws JdbcServerException;

    /**
     * Execute statement.
     * @param stmt
     * @return
     * @throws JdbcServerException
     */
    void execute(String stmt) throws JdbcServerException;

    /**
     * Execute update statement.
     * @param stmt
     * @return
     * @throws JdbcServerException
     */
    int executeUpdate(String stmt) throws JdbcServerException;

    /**
     * Close request.
     * @throws JdbcServerException
     */
    void closeStatement() throws JdbcServerException;
}
