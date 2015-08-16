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

import com.consol.citrus.container.Assert;

/**
 * @author Christoph Deppisch
 * @since 1.3
 * @deprecated since 2.3 in favor of using {@link com.consol.citrus.dsl.builder.AssertExceptionBuilder}
 */
public class AssertDefinition extends AbstractActionContainerDefinition<Assert> {

	/**
	 * Constructor using action field.
	 * @param action
	 */
	public AssertDefinition(Assert action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public AssertDefinition() {
		super(new Assert());
	}

	/**
	 * Expected exception during execution.
	 * @param exception
	 * @return
	 */
	public AssertDefinition exception(Class<? extends Throwable> exception) {
	    action.setException(exception);
	    return this;
	}

    /**
     * Expect error message in exception.
     * @param message
     */
	public AssertDefinition message(String message) {
		action.setMessage(message);
		return this;
	}
}
