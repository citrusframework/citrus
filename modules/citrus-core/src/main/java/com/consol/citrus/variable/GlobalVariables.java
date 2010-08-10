/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.variable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global variables valid in each test case.
 * 
 * @author Christoph Deppisch
 */
public class GlobalVariables {
	/** Variables name value pair map */
    private Map<String, String> variables = new LinkedHashMap<String, String>();
	
	/**
	 * Set the global variables.
	 * @param variables the variables to set
	 */
	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}

	/**
	 * Get the global variables.
	 * @return the variables
	 */
	public Map<String, String> getVariables() {
		return variables;
	}
}
