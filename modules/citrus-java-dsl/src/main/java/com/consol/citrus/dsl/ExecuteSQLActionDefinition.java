package com.consol.citrus.dsl;

import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.Resource;

import com.consol.citrus.actions.ExecuteSQLAction;

public class ExecuteSQLActionDefinition extends AbstractActionDefinition<ExecuteSQLAction> {

	public ExecuteSQLActionDefinition(ExecuteSQLAction action) {
	    super(action);
    }

	public ExecuteSQLActionDefinition statements(List<String> statements) {
		action.setStatements(statements);
		return this;
	}
	
	public ExecuteSQLActionDefinition statements(String... statements) {
		return statements(Arrays.asList(statements));
	}
	
	public ExecuteSQLActionDefinition sqlResource(Resource sqlResource) {
		action.setSqlResource(sqlResource);
		return this;
	}
	
	public ExecuteSQLActionDefinition ignoreErrors(boolean ignoreErrors) {
		action.setIgnoreErrors(ignoreErrors);
		return this;
	}
}
