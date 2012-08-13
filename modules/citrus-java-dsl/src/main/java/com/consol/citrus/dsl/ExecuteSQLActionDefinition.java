package com.consol.citrus.dsl;

import java.util.Arrays;

import java.util.List;

import org.springframework.core.io.Resource;

import com.consol.citrus.actions.ExecuteSQLAction;

/**
 * Test action executes SQL statements. Use this action when executing
 * database altering statements like UPDATE, INSERT, ALTER, DELETE. Statements are either
 * embedded inline in the test case description or given by an external file resource.
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
		action.setStatements(statements);
		return this;
	}
	
	public ExecuteSQLActionDefinition statements(String... statements) {
		return statements(Arrays.asList(statements));
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
