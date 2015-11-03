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
import com.consol.citrus.condition.*;

import java.io.File;

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
	 * The total length of seconds to wait on the condition to be satisfied
	 * @param seconds
	 * @return
	 */
	public WaitActionBuilder seconds(String seconds) {
		action.setSeconds(seconds);
		return this;
	}

	/**
	 * The total length of seconds to wait on the condition to be satisfied
	 * @param seconds
	 * @return
	 */
	public WaitActionBuilder seconds(Long seconds) {
		action.setSeconds(seconds.toString());
		return this;
	}

	/**
	 * The total length of milliseconds to wait on the condition to be satisfied
	 * @param milliseconds
	 * @return
	 */
	public WaitActionBuilder ms(String milliseconds) {
		action.setMilliseconds(milliseconds);
		return this;
	}

	/**
	 * The total length of milliseconds to wait on the condition to be satisfied
	 * @param milliseconds
	 * @return
	 */
	public WaitActionBuilder ms(Long milliseconds) {
		action.setMilliseconds(milliseconds.toString());
		return this;
	}

	/**
	 * The interval in seconds to use between each test of the condition
	 * @param interval
	 * @return
	 */
	public WaitActionBuilder interval(String interval) {
		action.setInterval(interval);
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
	 * The HTTP condition to wait for during execution.
	 * @param url
	 * @return
	 */
	public WaitActionBuilder http(String url) {
		HttpCondition condition = new HttpCondition();
		action.setCondition(condition);
		condition.setUrl(url);
		return this;
	}

	/**
	 * The file condition to wait for during execution.
	 * @param path
	 * @return
	 */
	public WaitActionBuilder file(String path) {
		FileCondition condition = new FileCondition();
		action.setCondition(condition);
		condition.setFilePath(path);
		return this;
	}

	/**
	 * The file condition to wait for during execution.
	 * @param file
	 * @return
	 */
	public WaitActionBuilder file(File file) {
		FileCondition condition = new FileCondition();
		action.setCondition(condition);
		condition.setFilePath(file.getAbsolutePath());
		return this;
	}
}
