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

import com.consol.citrus.container.*;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.TestRunner;

/**
 * Typical implementation of repeat iteration loop. Nested test actions are executed until
 * aborting condition evaluates to true.
 * 
 * @author Christoph Deppisch
 * @since 2.3
 */
public class RepeatBuilder extends AbstractTestContainerBuilder<RepeatUntilTrue> {

	/**
	 * Constructor using action field.
	 * @param designer
	 * @param action
	 */
	public RepeatBuilder(TestDesigner designer, RepeatUntilTrue action) {
	    super(designer, action);
    }

	/**
	 * Default constructor.
	 * @param designer
	 */
	public RepeatBuilder(TestDesigner designer) {
		this(designer, new RepeatUntilTrue());
	}

	/**
	 * Default constructor using runner and action container.
	 * @param runner
	 * @param action
	 */
	public RepeatBuilder(TestRunner runner, RepeatUntilTrue action) {
		super(runner, action);
	}

	/**
	 * Default constructor using test runner.
	 * @param runner
	 */
	public RepeatBuilder(TestRunner runner) {
		this(runner, new RepeatUntilTrue());
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
