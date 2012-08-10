package com.consol.citrus.dsl;

import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.Resource;

import com.consol.citrus.actions.ExecutePLSQLAction;

public class ExecutePLSQLActionDefinition extends AbstractActionDefinition<ExecutePLSQLAction> {

	public ExecutePLSQLActionDefinition(ExecutePLSQLAction action) {
	    super(action);
    }

	public ExecutePLSQLActionDefinition statements(List<String> statements) {
		action.setStatements(statements);
		return this;
	}
	
	public ExecutePLSQLActionDefinition statements(String... statements) {
		return statements(Arrays.asList(statements));
	}
	
	public ExecutePLSQLActionDefinition sqlResource(Resource sqlResource) {
		action.setSqlResource(sqlResource);
		return this;
	}
	
	public ExecutePLSQLActionDefinition script(String script) {
		action.setScript(script);
		return this;
	}
	
	public ExecutePLSQLActionDefinition ignoreErrors(boolean ignoreErrors) {
		action.setIgnoreErrors(ignoreErrors);
		return this;
	}
}
