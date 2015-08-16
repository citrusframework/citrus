/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.actions.ExecutePLSQLAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

/**
 * Creates an ExecutePLSQLAction, which executes PLSQL statements either declared inline as 
 * PLSQL statements or given by an external file resource.
 * 
 * @author Max Argyo, Giulia DelBravo
 * @since 1.3
 * @deprecated since 2.3 in favor of using {@link com.consol.citrus.dsl.builder.ExecutePLSQLBuilder}
 */
public class ExecutePLSQLActionDefinition extends AbstractActionDefinition<ExecutePLSQLAction> {

	/**
	 * Constructor using action field.
	 * @param action
	 */
	public ExecutePLSQLActionDefinition(ExecutePLSQLAction action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public ExecutePLSQLActionDefinition() {
		super(new ExecutePLSQLAction());
	}

	/**
	 * Sets the Spring JDBC template to use.
	 * @param jdbcTemplate
	 * @return
	 */
	public ExecutePLSQLActionDefinition jdbcTemplate(JdbcTemplate jdbcTemplate) {
		action.setJdbcTemplate(jdbcTemplate);
		return this;
	}

	/**
	 * Sets the SQL data source.
	 * @param dataSource
	 * @return
	 */
	public ExecutePLSQLActionDefinition dataSource(DataSource dataSource) {
		action.setDataSource(dataSource);
		return this;
	}

	/**
     * Adds a list of statements to execute.
     * @param statements
     */
	public ExecutePLSQLActionDefinition statements(List<String> statements) {
	    action.getStatements().addAll(statements);
		return this;
	}

	/**
	 * Adds a new statement tp the list of SQL executions.
	 * @param sql
	 * @return
	 */
	public ExecutePLSQLActionDefinition statement(String sql) {
	    action.getStatements().add(sql);
		return this;
	}

	/**
     * Setter for external file resource containing the SQL statements to execute.
     * @param filePath
     */
	public ExecutePLSQLActionDefinition sqlResource(String filePath) {
		action.setSqlResourcePath(filePath);
		return this;
	}

	/**
     * Setter for external file resource containing the SQL statements to execute.
     * @param sqlResource
     */
    public ExecutePLSQLActionDefinition sqlResource(Resource sqlResource) {
        try {
            action.setScript(FileUtils.readToString(sqlResource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read sql resource", e);
        }
        return this;
    }

	/**
     * Setter for inline script.
     * @param script
     */
	public ExecutePLSQLActionDefinition sqlScript(String script) {
		action.setScript(script);
		return this;
	}

	/**
     * Ignore errors during execution.
     * @param ignoreErrors boolean flag to set
     */
	public ExecutePLSQLActionDefinition ignoreErrors(boolean ignoreErrors) {
		action.setIgnoreErrors(ignoreErrors);
		return this;
	}
}
