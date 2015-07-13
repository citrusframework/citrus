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

/**
 * @author Christoph Deppisch
 * @since 2.2.1
 */
public class AssertExceptionBuilder extends AbstractTestActionBuilder<Assert> {

	/**
	 * Constructor using action field.
	 * @param action
	 */
	public AssertExceptionBuilder(Assert action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public AssertExceptionBuilder() {
		super(new Assert());
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
