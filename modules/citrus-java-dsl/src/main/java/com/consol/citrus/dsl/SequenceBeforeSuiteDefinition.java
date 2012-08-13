package com.consol.citrus.dsl;

import com.consol.citrus.container.SequenceAfterSuite;
import com.consol.citrus.container.SequenceBeforeSuite;
import com.consol.citrus.report.TestSuiteListeners;

public class SequenceBeforeSuiteDefinition extends AbstractActionDefinition<SequenceBeforeSuite> {

	public SequenceBeforeSuiteDefinition(SequenceBeforeSuite action) {
	    super(action);
    }

	/**
     * Sets the testSuiteListener.
     * @param testSuiteListener the testSuiteListener to set
     */
	public SequenceBeforeSuiteDefinition testSuiteListener(TestSuiteListeners testSuiteListener) {
		action.setTestSuiteListener(testSuiteListener);
		return this;
	}
	
	/**
     * Sets the afterSuiteActions.
     * @param afterSuiteActions the afterSuiteActions to set
     */
	public SequenceBeforeSuiteDefinition afterSuiteActions(SequenceAfterSuite afterSuiteActions) {
		action.setAfterSuiteActions(afterSuiteActions);
		return this;
	}
}
