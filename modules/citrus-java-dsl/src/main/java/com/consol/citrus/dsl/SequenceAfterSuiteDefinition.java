package com.consol.citrus.dsl;

import com.consol.citrus.container.SequenceAfterSuite;
import com.consol.citrus.report.TestSuiteListeners;

public class SequenceAfterSuiteDefinition extends AbstractActionDefinition<SequenceAfterSuite> {

	public SequenceAfterSuiteDefinition(SequenceAfterSuite action) {
	    super(action);
    }

	public SequenceAfterSuiteDefinition testSuiteListener(TestSuiteListeners testSuiteListener) {
		action.setTestSuiteListener(testSuiteListener);
		return this;
	}
}
