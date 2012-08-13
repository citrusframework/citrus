package com.consol.citrus.dsl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.consol.citrus.actions.CreateVariablesAction;
/**
 * Action definition which creates new test variables during a test. Existing test variables are overwritten
 * by new values.
 * */


public class CreateVariablesActionDefinition extends AbstractActionDefinition<CreateVariablesAction>{

	private Map<String, String> variables = new LinkedHashMap<String, String>(); 
	
	public CreateVariablesActionDefinition(CreateVariablesAction action){
		super(action);
	}
/**
 * Adds new Variables.
 * @param key name of the new variable
 * @param value value of the new variable
 * @return 
 */
	public CreateVariablesActionDefinition add(String key, String value)
	{
		variables.put(key, value);
		action.setVariables(variables);
		return this;
	}
}
