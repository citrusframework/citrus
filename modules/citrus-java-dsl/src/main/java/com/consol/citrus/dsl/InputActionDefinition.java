package com.consol.citrus.dsl;

import com.consol.citrus.actions.InputAction;

public class InputActionDefinition extends AbstractActionDefinition<InputAction> {

	public InputActionDefinition(InputAction action) {
	    super(action);
    }

	public InputActionDefinition message(String message) {
		action.setMessage(message);
		return this;
	}
	
	public InputActionDefinition variable(String variable) {
		action.setVariable(variable);
		return this;
	}
	
	public InputActionDefinition validAnswer(String... answers) {
		String validAnswerString = answers[0];
		for(int i = 1; i < answers.length; i++)
			validAnswerString += "/" + answers[i];
		action.setValidAnswers(validAnswerString);
		return this;
	}
}
