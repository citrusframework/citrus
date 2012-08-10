package com.consol.citrus.dsl;

import com.consol.citrus.container.RepeatOnErrorUntilTrue;

public class RepeatOnErrorUntilTrueDefinition extends AbstractActionDefinition<RepeatOnErrorUntilTrue> {

	/**
     * Default constructor using action container.
     * @param action
     */
	public RepeatOnErrorUntilTrueDefinition(RepeatOnErrorUntilTrue action) {
	    super(action);
    }

	/**
     * Adds a condition to this iterate container.
     * @param condition
     * @return
     */
	public RepeatOnErrorUntilTrueDefinition condition(String condition) {
		action.setCondition(condition);
		return this;
	}
	
	/**
     * Sets the index variable name.
     * @param name
     * @return
     */
	public RepeatOnErrorUntilTrueDefinition index(String indexName) {
		action.setIndexName(indexName);
		return this;
	}
	
	/**
     * Sets the index start value.
     * @param index
     * @return
     */
	public RepeatOnErrorUntilTrueDefinition startsWith(int index) {
		action.setIndex(index);
		return this;
	}
	
	/**
	 * Sets the autosleep time.
	 * @param autoSleep
	 * @return
	 */
	public RepeatOnErrorUntilTrueDefinition autoSleep(long autoSleep) {
		action.setAutoSleep(autoSleep);
		return this;
	}
}
