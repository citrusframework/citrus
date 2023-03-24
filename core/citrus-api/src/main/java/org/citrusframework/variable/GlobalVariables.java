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

package org.citrusframework.variable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global variables valid in each test case.
 */
public class GlobalVariables {

    /** Variables name value pair map */
    private final Map<String, Object> variables;

    public GlobalVariables() {
    	this(new Builder());
	}

    public GlobalVariables(Builder builder) {
    	this.variables = builder.variables;
	}

	/**
	 * Get the global variables.
	 * @return the variables
	 */
	public Map<String, Object> getVariables() {
		return variables;
	}

	/**
	 * Fluent builder.
	 */
	public static class Builder {
		private final Map<String, Object> variables = new LinkedHashMap<>();

		public Builder variable(String name, Object value) {
			this.variables.put(name, value);
			return this;
		}

		public Builder variables(Map<String, Object> variables) {
			this.variables.putAll(variables);
			return this;
		}

		public GlobalVariables build() {
			return new GlobalVariables(this);
		}
	}
}
