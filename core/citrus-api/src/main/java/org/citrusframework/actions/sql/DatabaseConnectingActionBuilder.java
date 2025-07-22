/*
 * Copyright the original author or authors.
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

package org.citrusframework.actions.sql;

import java.util.List;
import javax.sql.DataSource;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ActionBuilder;
import org.citrusframework.actions.ReferenceResolverAwareBuilder;
import org.citrusframework.spi.Resource;

public interface DatabaseConnectingActionBuilder<T extends TestAction, B extends DatabaseConnectingActionBuilder<T, B>>
        extends ActionBuilder<T, B>, TestActionBuilder<T>, ReferenceResolverAwareBuilder<T, B> {

    /**
     * Sets the Spring JDBC template to use.
     */
    B jdbcTemplate(Object jdbcTemplate);

    /**
     * Sets the transaction manager to use.
     */
    B transactionManager(Object transactionManager);

    /**
     * Sets the transaction timeout to use.
     */
    B transactionTimeout(int transactionTimeout);

    /**
     * Sets the transaction timeout to use.
     */
    B transactionTimeout(String transactionTimeout);

    /**
     * Sets the transaction isolation level to use.
     */
    B transactionIsolationLevel(String isolationLevel);

    /**
     * Sets the SQL data source.
     */
    B dataSource(DataSource dataSource);

    /**
     * Sets the name of the SQL data source.
     */
    B dataSource(String dataSourceName);

    /**
     * List of statements to execute. Declared inline in the test case.
     */
    B statements(List<String> statements);

    /**
     * Adds a new statement to the list of SQL executions.
     */
    B statement(String sql);

    /**
     * Setter for external file resource containing the SQL statements to execute.
     */
    B sqlResource(Resource sqlResource);

    /**
     * Setter for external file resource containing the SQL statements to execute.
     */
    B sqlResource(String filePath);
}
