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

import com.consol.citrus.actions.InputAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Test action prompts user data from standard input stream. The input data is then stored as new
 * test variable. Test case stops until user input is complete.
 * 
 * Action can declare a set of valid answers, so user will be prompted until a valid 
 * 
 * @author Christoph Deppisch
 * @since 2.3
 */
public class InputActionBuilder extends AbstractTestActionBuilder<InputAction> {

	/**
	 * Constructor using action field.
	 * @param action
	 */
	public InputActionBuilder(InputAction action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public InputActionBuilder() {
		super(new InputAction());
	}

	/**
     * Sets the message displayed to the user.
     * @param message the message to set
     */
	public InputActionBuilder message(String message) {
		action.setMessage(message);
		return this;
	}
	
	/**
     * Stores the result to a test variable.
     * @param variable the variable to set
     */
	public InputActionBuilder result(String variable) {
		action.setVariable(variable);
		return this;
	}
	
	 /**
     * Sets the valid answers.
     * @param answers the validAnswers to set
     */
	public InputActionBuilder answers(String... answers) {
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
