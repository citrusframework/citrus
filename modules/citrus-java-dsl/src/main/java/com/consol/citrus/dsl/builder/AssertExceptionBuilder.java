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

import com.consol.citrus.container.Assert;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.TestRunner;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class AssertExceptionBuilder extends AbstractExceptionContainerBuilder<Assert> {

	/**
	 * Constructor using action field.
	 * @param designer
	 * @param action
	 */
	public AssertExceptionBuilder(TestDesigner designer, Assert action) {
	    super(designer, action);
    }

	/**
	 * Default constructor.
	 * @param designer
	 */
	public AssertExceptionBuilder(TestDesigner designer) {
		this(designer, new Assert());
	}

	/**
	 * Default constructor using runner and action container.
	 * @param runner
	 * @param action
	 */
	public AssertExceptionBuilder(TestRunner runner, Assert action) {
		super(runner, action);
	}

	/**
	 * Default constructor using test runner.
	 * @param runner
	 */
	public AssertExceptionBuilder(TestRunner runner) {
		this(runner, new Assert());
	}

	/**
	 * Expected exception during execution.
	 * @param exception
	 * @return
	 */
	public AssertExceptionBuilder exception(Class<? extends Throwable> exception) {
	    action.setException(exception);
	    return this;
	}
	
    /**
     * Expect error message in exception.
     * @param message
     */
	public AssertExceptionBuilder message(String message) {
		action.setMessage(message);
		return this;
	}
}
