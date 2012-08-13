package com.consol.citrus.dsl;

import com.consol.citrus.container.Conditional;

/**
 * Action definition creates a conditional container, which executes nested test actions if condition expression evaluates to true.
 *
 */

public class ConditionalDefinition extends AbstractActionDefinition<Conditional> {

	public ConditionalDefinition(Conditional action) {
	    super(action);
    }
	
/**
 * Condition which allows execution if true.
 *
 * @param conditionIn
 */
	public ConditionalDefinition expression(String expressionIn) {
		action.setExpression(expressionIn);
		return this;
	}
}
