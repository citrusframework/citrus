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

import java.util.List;

import org.springframework.core.io.Resource;

import com.consol.citrus.actions.ExecuteSQLAction;

/**
 * Test action executes SQL statements. Use this action when executing
 * database altering statements like UPDATE, INSERT, ALTER, DELETE. Statements are either
 * embedded inline in the test case description or given by an external file resource.
 * 
 * @author Max Argyo, Giulia DelBravo
 * @since 1.3
 */
public class ExecuteSQLActionDefinition extends AbstractActionDefinition<ExecuteSQLAction> {

	public ExecuteSQLActionDefinition(ExecuteSQLAction action) {
	    super(action);
    }

	/**
     * List of statements to execute. Declared inline in the test case. 
     * @param statements
     */
	public ExecuteSQLActionDefinition statements(List<String> statements) {
		action.getStatements().addAll(statements);
		return this;
	}
	
	/**
	 * Adds a new statement to the list of SQL executions.
	 * @param sql
	 * @return
	 */
	public ExecuteSQLActionDefinition statement(String sql) {
	    action.getStatements().add(sql);
		return this;
	}
	
	/**
     * Setter for external file resource containing the SQL statements to execute.
     * @param sqlResource
     */
	public ExecuteSQLActionDefinition sqlResource(Resource sqlResource) {
		action.setSqlResource(sqlResource);
		return this;
	}
	
	/**
     * Ignore errors during execution.
     * @param ignoreErrors boolean flag to set
     */
	public ExecuteSQLActionDefinition ignoreErrors(boolean ignoreErrors) {
		action.setIgnoreErrors(ignoreErrors);
		return this;
	}
}
