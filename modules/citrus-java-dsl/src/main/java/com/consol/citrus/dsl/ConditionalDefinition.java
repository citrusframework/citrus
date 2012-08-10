package com.consol.citrus.dsl;

import com.consol.citrus.container.Conditional;

public class ConditionalDefinition extends AbstractActionDefinition<Conditional> {

	public ConditionalDefinition(Conditional action) {
	    super(action);
    }
	
	public ConditionalDefinition expression(String expressionIn) {
		action.setExpression(expressionIn);
		return this;
	}
}
