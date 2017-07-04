/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.actions.ExecutePLSQLAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Creates an ExecutePLSQLAction, which executes PLSQL statements either declared inline as
 * PLSQL statements or given by an external file resource.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ExecutePLSQLBuilder extends AbstractTestActionBuilder<ExecutePLSQLAction> {

    /**
     * Constructor using action field.
     * @param action
     */
    public ExecutePLSQLBuilder(ExecutePLSQLAction action) {
        super(action);
    }

    /**
     * Default constructor.
     */
    public ExecutePLSQLBuilder() {
        super(new ExecutePLSQLAction());
    }

    /**
     * Sets the Spring JDBC template to use.
     * @param jdbcTemplate
     * @return
     */
    public ExecutePLSQLBuilder jdbcTemplate(JdbcTemplate jdbcTemplate) {
        action.setJdbcTemplate(jdbcTemplate);
        return this;
    }

    /**
     * Sets the transaction manager to use.
     * @param transactionManager
     * @return
     */
    public ExecutePLSQLBuilder transactionManager(PlatformTransactionManager transactionManager) {
        action.setTransactionManager(transactionManager);
        return this;
    }

    /**
     * Sets the transaction timeout to use.
     * @param transactionTimeout
     * @return
     */
    public ExecutePLSQLBuilder transactionTimeout(int transactionTimeout) {
        action.setTransactionTimeout(String.valueOf(transactionTimeout));
        return this;
    }

    /**
     * Sets the transaction timeout to use.
     * @param transactionTimeout
     * @return
     */
    public ExecutePLSQLBuilder transactionTimeout(String transactionTimeout) {
        action.setTransactionTimeout(transactionTimeout);
        return this;
    }

    /**
     * Sets the transaction isolation level to use.
     * @param isolationLevel
     * @return
     */
    public ExecutePLSQLBuilder transactionIsolationLevel(String isolationLevel) {
        action.setTransactionIsolationLevel(isolationLevel);
        return this;
    }

    /**
     * Sets the SQL data source.
     * @param dataSource
     * @return
     */
    public ExecutePLSQLBuilder dataSource(DataSource dataSource) {
        action.setDataSource(dataSource);
        return this;
    }

    /**
     * Adds a list of statements to execute.
     * @param statements
     */
    public ExecutePLSQLBuilder statements(List<String> statements) {
        action.getStatements().addAll(statements);
        return this;
    }

    /**
     * Adds a new statement tp the list of SQL executions.
     * @param sql
     * @return
     */
    public ExecutePLSQLBuilder statement(String sql) {
        action.getStatements().add(sql);
        return this;
    }

    /**
     * Setter for external file resource containing the SQL statements to execute.
     * @param filePath
     */
    public ExecutePLSQLBuilder sqlResource(String filePath) {
        action.setSqlResourcePath(filePath);
        return this;
    }

    /**
     * Setter for external file resource containing the SQL statements to execute.
     * @param sqlResource
     */
    public ExecutePLSQLBuilder sqlResource(Resource sqlResource) {
        return sqlResource(sqlResource, FileUtils.getDefaultCharset());
    }

    /**
     * Setter for external file resource containing the SQL statements to execute.
     * @param sqlResource
     * @param charset
     */
    public ExecutePLSQLBuilder sqlResource(Resource sqlResource, Charset charset) {
        try {
            action.setScript(FileUtils.readToString(sqlResource, charset));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read sql resource", e);
        }
        return this;
    }

    /**
     * Setter for inline script.
     * @param script
     */
    public ExecutePLSQLBuilder sqlScript(String script) {
        action.setScript(script);
        return this;
    }

    /**
     * Ignore errors during execution.
     * @param ignoreErrors boolean flag to set
     */
    public ExecutePLSQLBuilder ignoreErrors(boolean ignoreErrors) {
        action.setIgnoreErrors(ignoreErrors);
        return this;
    }
}
