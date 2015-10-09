/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.dsl.builder;

import com.consol.citrus.actions.WaitAction;
import com.consol.citrus.condition.Condition;
import com.consol.citrus.condition.FileCondition;
import com.consol.citrus.condition.HttpCondition;

/**
 * Wait action pauses test execution until a condition is satisfied. If the condition is not satisfied after the
 * configured timeout then the test exits with an error.
 * 
 * @author Martin Maher
 * @since 2.4
 */
public class WaitActionBuilder extends AbstractTestActionBuilder<WaitAction> {

	/**
	 * Constructor using action field.
	 * @param action
	 */
	public WaitActionBuilder(WaitAction action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public WaitActionBuilder() {
		super(new WaitAction());
	}

	/**
	 * The total length of time to wait on the condition to be satisfied
	 * @param waitTime
	 * @return
	 */
	public WaitActionBuilder time(String waitTime) {
		action.setWaitForSeconds(waitTime);
		return this;
	}

	/**
	 * The interval in seconds to use between each test of the condition
	 * @param interval
	 * @return
	 */
	public WaitActionBuilder interval(String interval) {
		action.setTestIntervalSeconds(interval);
		return this;
	}

	/**
	 * Condition to wait for during execution.
	 * @param condition
	 * @return
	 */
	public WaitActionBuilder condition(Condition condition) {
		action.setCondition(condition);
		return this;
	}

	/**
	 * The HTTP Condition to wait for during execution.
	 * @param url
	 * @return
	 */
	public WaitActionBuilder httpCondition(String url) {
		HttpCondition condition = new HttpCondition();
		action.setCondition(condition);
		condition.setUrl(url);
		return this;
	}

	/**
	 * The File Condition to wait for during execution.
	 * @param path
	 * @return
	 */
	public WaitActionBuilder fileCondition(String path) {
		FileCondition condition = new FileCondition();
		action.setCondition(condition);
		condition.setFilename(path);
		return this;
	}
}
