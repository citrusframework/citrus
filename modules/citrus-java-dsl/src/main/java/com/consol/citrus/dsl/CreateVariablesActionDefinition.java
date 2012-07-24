package com.consol.citrus.dsl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.consol.citrus.actions.CreateVariablesAction;

public class CreateVariablesActionDefinition extends AbstractActionDefinition<CreateVariablesAction>{

	private Map<String, String> variables = new LinkedHashMap<String, String>();
	private ApplicationContext applicationContext; 
	
	public CreateVariablesActionDefinition(CreateVariablesAction action, ApplicationContext ctx){
		super(action);
		this.applicationContext = ctx;
	}
	
	public CreateVariablesActionDefinition add(String key, String value)
	{
		variables.put(key, value);
		action.setVariables(variables);
		return this;
	}
}
