/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.container.RepeatUntilTrue;

/**
 * Typical implementation of repeat iteration loop. Nested test actions are executed until
 * aborting condition evaluates to true.
 * 
 * @author Max Argyo, Giulia DelBravo
 * @since 1.3
 * @deprecated since 2.3 in favor of using {@link com.consol.citrus.dsl.builder.RepeatBuilder}
 */
public class RepeatUntilTrueDefinition extends AbstractActionContainerDefinition<RepeatUntilTrue> {

	/**
	 * Constructor using action field.
	 * @param action
	 */
	public RepeatUntilTrueDefinition(RepeatUntilTrue action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public RepeatUntilTrueDefinition() {
		super(new RepeatUntilTrue());
	}

	/**
     * Sets Name of index variable.
     * @param indexName
     */
	public RepeatUntilTrueDefinition index(String indexName) {
		action.setIndexName(indexName);
		return this;
	}

	/**
     * Setter for looping index.
     * @param index the index to set
     */
	public RepeatUntilTrueDefinition startsWith(int index) {
		action.setStart(index);
		return this;
	}

	/**
     * Aborting condition.
     * @param condition
     */
	public RepeatUntilTrueDefinition until(String condition) {
		action.setCondition(condition);
		return this;
	}
}
