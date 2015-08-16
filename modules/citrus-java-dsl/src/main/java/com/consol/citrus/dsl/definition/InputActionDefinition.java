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

import com.consol.citrus.actions.InputAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Test action prompts user data from standard input stream. The input data is then stored as new
 * test variable. Test case stops until user input is complete.
 * 
 * Action can declare a set of valid answers, so user will be prompted until a valid 
 * 
 * @author Max Argyo, Giulia DelBravo
 * @since 1.3
 * @deprecated since 2.3 in favor of using {@link com.consol.citrus.dsl.builder.InputActionBuilder}
 */
public class InputActionDefinition extends AbstractActionDefinition<InputAction> {

	/**
	 * Constructor using action field.
	 * @param action
	 */
	public InputActionDefinition(InputAction action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public InputActionDefinition() {
		super(new InputAction());
	}

	/**
     * Sets the message displayed to the user.
     * @param message the message to set
     */
	public InputActionDefinition message(String message) {
		action.setMessage(message);
		return this;
	}

	/**
     * Stores the result to a test variable.
     * @param variable the variable to set
     */
	public InputActionDefinition result(String variable) {
		action.setVariable(variable);
		return this;
	}

	 /**
     * Sets the valid answers.
     * @param answers the validAnswers to set
     */
	public InputActionDefinition answers(String... answers) {
	    if (answers.length == 0) {
	        throw new CitrusRuntimeException("Please specify proper answer possibilities for input action");
	    }

		StringBuilder validAnswers = new StringBuilder();

		for (String answer : answers) {
		    validAnswers.append(InputAction.ANSWER_SEPARATOR);
		    validAnswers.append(answer);
        }

		action.setValidAnswers(validAnswers.toString().substring(1));
		return this;
	}
}
