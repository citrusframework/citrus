/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.container.IteratingConditionExpression;
import com.consol.citrus.container.RepeatUntilTrue;

/**
 * Typical implementation of repeat iteration loop. Nested test actions are executed until
 * aborting condition evaluates to true.
 * 
 * @author Christoph Deppisch
 * @since 2.3
 */
public class RepeatBuilder extends AbstractTestActionContainerBuilder<RepeatUntilTrue> {

	/**
	 * Constructor using action field.
	 * @param action
	 */
	public RepeatBuilder(RepeatUntilTrue action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public RepeatBuilder() {
		super(new RepeatUntilTrue());
	}

	/**
     * Sets Name of index variable.
     * @param indexName
     */
	public RepeatBuilder index(String indexName) {
		action.setIndexName(indexName);
		return this;
	}
	
	/**
     * Setter for looping index.
     * @param index the index to set
     */
	public RepeatBuilder startsWith(int index) {
		action.setStart(index);
		return this;
	}
	
	/**
     * Aborting condition.
     * @param condition
     */
	public RepeatBuilder until(String condition) {
		action.setCondition(condition);
		return this;
	}

	/**
	 * Aborting condition expression.
	 * @param condition
	 */
	public RepeatBuilder until(IteratingConditionExpression condition) {
		action.setConditionExpression(condition);
		return this;
	}
}
