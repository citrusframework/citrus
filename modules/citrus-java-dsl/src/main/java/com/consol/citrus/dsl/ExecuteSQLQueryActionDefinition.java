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

package com.consol.citrus.dsl;

import java.util.*;

import org.springframework.core.io.Resource;

import com.consol.citrus.actions.ExecuteSQLQueryAction;
import com.consol.citrus.validation.script.sql.SqlResultSetScriptValidator;

/**
 * Action executes SQL queries and offers result set validation.
 *
 * The class enables you to query data result sets from a
 * database. Validation will happen on column basis inside the result set.
 * 
 * @author Max Argyo, Giulia DelBravo
 * @since 1.3
 */
public class ExecuteSQLQueryActionDefinition extends AbstractActionDefinition<ExecuteSQLQueryAction> {

	public ExecuteSQLQueryActionDefinition(ExecuteSQLQueryAction action) {
	    super(action);
    }

	/**
     * List of statements to execute. Declared inline in the test case. 
     * @param statements
     */
	public ExecuteSQLQueryActionDefinition statements(List<String> statements) {
		action.getStatements().addAll(statements);
		return this;
	}
	
	/**
	 * Adds a new statement to the list of SQL executions.
	 * @param statements
	 * @return
	 */
	public ExecuteSQLQueryActionDefinition statement(String statements) {
	    action.getStatements().add(statements);
		return this;
	}
	
	/**
     * Setter for external file resource containing the SQL statements to execute.
     * @param sqlResource
     */
	public ExecuteSQLQueryActionDefinition sqlResource(Resource sqlResource) {
		action.setSqlResource(sqlResource);
		return this;
	}
	
	/**
     * Set expected control result set. Keys represent the column names, values
     * the expected values.
     *
     * @param controlResultSet
     */
	public ExecuteSQLQueryActionDefinition validate(String column, String ... values) {
		action.getControlResultSet().put(column, Arrays.asList(values));
		return this;
	}
	
	 /**
     * User can extract column values to test variables. Map holds column names (keys) and
     * respective target variable names (values).
     *
     * @param variablesMap the variables to be created out of database values
     */
	public ExecuteSQLQueryActionDefinition extract(String columnName, String variableName) {
		action.getExtractVariables().put(columnName, variableName);
		return this;
	}
	
	/**
     * Sets the validator.
     * @param validator the validator to set
     */
	public ExecuteSQLQueryActionDefinition validator(SqlResultSetScriptValidator validator) {
		action.setValidator(validator);
		return this;
	}
}
