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

import com.consol.citrus.container.RepeatOnErrorUntilTrue;

/**
 * @author Christoph Deppisch
 * @since 2.2.1
 */
public class RepeatOnErrorBuilder extends AbstractTestActionBuilder<RepeatOnErrorUntilTrue> {

	/**
     * Default constructor using action container.
     * @param action
     */
	public RepeatOnErrorBuilder(RepeatOnErrorUntilTrue action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public RepeatOnErrorBuilder() {
		super(new RepeatOnErrorUntilTrue());
	}

	/**
     * Adds a condition to this iterate container.
     * @param condition
     * @return
     */
	public RepeatOnErrorBuilder until(String condition) {
		action.setCondition(condition);
		return this;
	}
	
	/**
     * Sets the index variable name.
     * @param indexName
     * @return
     */
	public RepeatOnErrorBuilder index(String indexName) {
		action.setIndexName(indexName);
		return this;
	}
	
	/**
     * Sets the index start value.
     * @param index
     * @return
     */
	public RepeatOnErrorBuilder startsWith(int index) {
		action.setStart(index);
		return this;
	}
	
	/**
	 * Sets the auto sleep time in between repeats in milliseconds.
	 * @param autoSleep
	 * @return
	 */
	public RepeatOnErrorBuilder autoSleep(long autoSleep) {
		action.setAutoSleep(autoSleep);
		return this;
	}
}
