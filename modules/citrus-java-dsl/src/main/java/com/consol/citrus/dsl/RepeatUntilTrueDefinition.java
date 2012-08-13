package com.consol.citrus.dsl;

import com.consol.citrus.container.RepeatUntilTrue;

/**
 * Typical implementation of repeat iteration loop. Nested test actions are executed until
 * aborting condition evaluates to true.
 */
public class RepeatUntilTrueDefinition extends AbstractActionDefinition<RepeatUntilTrue> {

	public RepeatUntilTrueDefinition(RepeatUntilTrue action) {
	    super(action);
    }

	/**
     * Sets Name of index variable.
     * @param indexName
     */
	public RepeatUntilTrueDefinition index(String indexName) {
		action.setIndexName(indexName);
		return this;
	}
	
	/**
     * Setter for looping index.
     * @param index the index to set
     */
	public RepeatUntilTrueDefinition startsWith(int index) {
		action.setIndex(index);
		return this;
	}
	
	/**
     * Aborting condition.
     * @param condition
     */
	public RepeatUntilTrueDefinition condition(String condition) {
		action.setCondition(condition);
		return this;
	}
}
