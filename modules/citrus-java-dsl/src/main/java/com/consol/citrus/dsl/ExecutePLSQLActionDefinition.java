package com.consol.citrus.dsl;

import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.Resource;

import com.consol.citrus.actions.ExecutePLSQLAction;
/**
 * Creates an ExecutePLSQLAction, which executes PLSQL statements either declared inline as PLSQL statements or given by an
 * external file resource.
 */
public class ExecutePLSQLActionDefinition extends AbstractActionDefinition<ExecutePLSQLAction> {

	public ExecutePLSQLActionDefinition(ExecutePLSQLAction action) {
	    super(action);
    }

	/**
     * List of statements to execute. Declared inline in the test case. 
     * @param statements
     */
	public ExecutePLSQLActionDefinition statements(List<String> statements) {
		action.setStatements(statements);
		return this;
	}
	
	public ExecutePLSQLActionDefinition statements(String... statements) {
		return statements(Arrays.asList(statements));
	}
	
	/**
     * Setter for external file resource containing the SQL statements to execute.
     * @param sqlResource
     */
	public ExecutePLSQLActionDefinition sqlResource(Resource sqlResource) {
		action.setSqlResource(sqlResource);
		return this;
	}
	
	/**
     * Setter for inline script.
     * @param script
     */
	public ExecutePLSQLActionDefinition script(String script) {
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
