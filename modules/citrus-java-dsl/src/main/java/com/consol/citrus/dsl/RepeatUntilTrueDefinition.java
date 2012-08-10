package com.consol.citrus.dsl;

import com.consol.citrus.container.RepeatUntilTrue;

public class RepeatUntilTrueDefinition extends AbstractActionDefinition<RepeatUntilTrue> {

	public RepeatUntilTrueDefinition(RepeatUntilTrue action) {
	    super(action);
    }

	public RepeatUntilTrueDefinition index(String indexName) {
		action.setIndexName(indexName);
		return this;
	}
	
	public RepeatUntilTrueDefinition startsWith(int index) {
		action.setIndex(index);
		return this;
	}
	
	public RepeatUntilTrueDefinition condition(String condition) {
		action.setCondition(condition);
		return this;
	}
}
