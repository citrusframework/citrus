package com.consol.citrus.dsl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.consol.citrus.actions.ExecuteSQLQueryAction;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.script.sql.SqlResultSetScriptValidator;

public class ExecuteSQLQueryActionDefinition extends AbstractActionDefinition<ExecuteSQLQueryAction> {

	public ExecuteSQLQueryActionDefinition(ExecuteSQLQueryAction action) {
	    super(action);
    }

	public ExecuteSQLQueryActionDefinition statements(List<String> statements) {
		action.setStatements(statements);
		return this;
	}
	
	public ExecuteSQLQueryActionDefinition statements(String... statements) {
		return statements(Arrays.asList(statements));
	}
	
	public ExecuteSQLQueryActionDefinition sqlResource(Resource sqlResource) {
		action.setSqlResource(sqlResource);
		return this;
	}
	
	public ExecuteSQLQueryActionDefinition controlResultSet(Map<String, List<String>> controlResultSet) {
		action.setControlResultSet(controlResultSet);
		return this;
	}
	
	public ExecuteSQLQueryActionDefinition extractVariables(Map<String,String> variablesMap) {
		action.setExtractVariables(variablesMap);
		return this;
	}
	
	public ExecuteSQLQueryActionDefinition scriptValidationContext(ScriptValidationContext scriptValidationContext) {
		action.setScriptValidationContext(scriptValidationContext);
		return this;
	}
	
	public ExecuteSQLQueryActionDefinition validator(SqlResultSetScriptValidator validator) {
		action.setValidator(validator);
		return this;
	}
}
