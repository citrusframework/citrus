package com.consol.citrus.dsl;

import com.consol.citrus.actions.InputAction;

/**
 * Test action prompts user data from standard input stream. The input data is then stored as new
 * test variable. Test workflow stops until user input is complete.
 * 
 * Action can declare a set of valid answers, so user will be prompted until a valid 
 */
public class InputActionDefinition extends AbstractActionDefinition<InputAction> {

	public InputActionDefinition(InputAction action) {
	    super(action);
    }

	/**
     * Sets the message.
     * @param message the message to set
     */
	public InputActionDefinition message(String message) {
		action.setMessage(message);
		return this;
	}
	
	/**
     * Sets the variable.
     * @param variable the variable to set
     */
	public InputActionDefinition variable(String variable) {
		action.setVariable(variable);
		return this;
	}
	
	 /**
     * Sets the valid answers.
     * @param validAnswers the validAnswers to set
     */
	public InputActionDefinition validAnswer(String... answers) {
		String validAnswerString = answers[0];
		for(int i = 1; i < answers.length; i++)
			validAnswerString += "/" + answers[i];
		action.setValidAnswers(validAnswerString);
		return this;
	}
}
