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

import com.consol.citrus.container.RepeatOnErrorUntilTrue;

/**
 * @author Max Argyo, Giulia DelBravo
 * @since 1.3
 * @deprecated since 2.3 in favor of using {@link com.consol.citrus.dsl.builder.RepeatOnErrorBuilder}
 */
public class RepeatOnErrorUntilTrueDefinition extends AbstractActionContainerDefinition<RepeatOnErrorUntilTrue> {

	/**
     * Default constructor using action container.
     * @param action
     */
	public RepeatOnErrorUntilTrueDefinition(RepeatOnErrorUntilTrue action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public RepeatOnErrorUntilTrueDefinition() {
		super(new RepeatOnErrorUntilTrue());
	}

	/**
     * Adds a condition to this iterate container.
     * @param condition
     * @return
     */
	public RepeatOnErrorUntilTrueDefinition until(String condition) {
		action.setCondition(condition);
		return this;
	}

	/**
     * Sets the index variable name.
     * @param indexName
     * @return
     */
	public RepeatOnErrorUntilTrueDefinition index(String indexName) {
		action.setIndexName(indexName);
		return this;
	}

	/**
     * Sets the index start value.
     * @param index
     * @return
     */
	public RepeatOnErrorUntilTrueDefinition startsWith(int index) {
		action.setStart(index);
		return this;
	}

	/**
	 * Sets the auto sleep time in between repeats in milliseconds.
	 * @param autoSleep
	 * @return
	 */
	public RepeatOnErrorUntilTrueDefinition autoSleep(long autoSleep) {
		action.setAutoSleep(autoSleep);
		return this;
	}
}
