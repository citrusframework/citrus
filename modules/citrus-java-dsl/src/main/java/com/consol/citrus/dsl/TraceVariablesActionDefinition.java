package com.consol.citrus.dsl;


import java.util.ArrayList;
import java.util.List;


import com.consol.citrus.actions.TraceVariablesAction;

/**
 * Action that prints variable values to the console/logger. Action requires a list of variable
 * names. Tries to find the variables in the test context and print its values.
 */
public class TraceVariablesActionDefinition extends AbstractActionDefinition<TraceVariablesAction> {

	private List<String> variableNames = new ArrayList<String>();

	public TraceVariablesActionDefinition(TraceVariablesAction action) {
	    super(action);

    }

	/**
     * Setter for info values list
     * @param variableNames
     */
	public TraceVariablesActionDefinition trace(String name) {
		variableNames.add(name);
		action.setVariableNames(variableNames);
		return this;		
	}

}


