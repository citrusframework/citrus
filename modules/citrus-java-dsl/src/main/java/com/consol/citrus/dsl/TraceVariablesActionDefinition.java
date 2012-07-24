package com.consol.citrus.dsl;


import java.util.ArrayList;
import java.util.List;


import com.consol.citrus.actions.TraceVariablesAction;


public class TraceVariablesActionDefinition extends AbstractActionDefinition<TraceVariablesAction> {

	private List<String> variableNames = new ArrayList<String>();

	public TraceVariablesActionDefinition(TraceVariablesAction action) {
	    super(action);

    }

	public TraceVariablesActionDefinition trace(String name){
		
		variableNames.add(name);
		action.setVariableNames(variableNames);
		return this;		
	}

}


