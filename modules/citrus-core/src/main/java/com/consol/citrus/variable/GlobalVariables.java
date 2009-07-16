package com.consol.citrus.variable;

import java.util.LinkedHashMap;
import java.util.Map;

public class GlobalVariables {
	private Map<String, String> variables = new LinkedHashMap<String, String>();
	
	/**
	 * @param variables the variables to set
	 */
	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}

	/**
	 * @return the variables
	 */
	public Map<String, String> getVariables() {
		return variables;
	}
}
