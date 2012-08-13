package com.consol.citrus.dsl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.consol.citrus.actions.ExecuteSQLQueryAction;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.script.sql.SqlResultSetScriptValidator;

/**
 * Action executes SQL queries and offers result set validation.
 *
 * The class enables you to query data result sets from a
 * database. Validation will happen on column basis inside the result set.
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
		action.setStatements(statements);
		return this;
	}
	
	public ExecuteSQLQueryActionDefinition statements(String... statements) {
		return statements(Arrays.asList(statements));
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
	public ExecuteSQLQueryActionDefinition controlResultSet(Map<String, List<String>> controlResultSet) {
		action.setControlResultSet(controlResultSet);
		return this;
	}
	
	 /**
     * User can extract column values to test variables. Map holds column names (keys) and
     * respective target variable names (values).
     *
     * @param variablesMap the variables to be created out of database values
     */
	public ExecuteSQLQueryActionDefinition extractVariables(Map<String,String> variablesMap) {
		action.setExtractVariables(variablesMap);
		return this;
	}
	
	/**
     * Sets the script validation context.
     * @param scriptValidationContext the scriptValidationContext to set
     */
	public ExecuteSQLQueryActionDefinition scriptValidationContext(ScriptValidationContext scriptValidationContext) {
		action.setScriptValidationContext(scriptValidationContext);
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
