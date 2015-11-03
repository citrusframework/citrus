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
	 * Condition to wait for during execution.
	 * @param condition
	 * @return
	 */
	public WaitConditionBuilder condition(Condition condition) {
		action.setCondition(condition);
		return new WaitConditionBuilder(action, condition);
	}

	/**
	 * The HTTP condition to wait for during execution.
	 * @param url
	 * @return
	 */
	public WaitHttpConditionBuilder http(String url) {
		HttpCondition condition = new HttpCondition();
		action.setCondition(condition);
		condition.setUrl(url);
		return new WaitHttpConditionBuilder(action, condition);
	}

	/**
	 * The file condition to wait for during execution.
	 * @param path
	 * @return
	 */
	public WaitConditionBuilder file(String path) {
		FileCondition condition = new FileCondition();
		condition.setFilePath(path);
		return condition(condition);
	}

	/**
	 * The file condition to wait for during execution.
	 * @param file
	 * @return
	 */
	public WaitConditionBuilder file(File file) {
		FileCondition condition = new FileCondition();
		condition.setFilePath(file.getAbsolutePath());
		return condition(condition);
	}
}
