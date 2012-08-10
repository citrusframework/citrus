package com.consol.citrus.dsl;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.consol.citrus.actions.TraceVariablesAction;

public class TraceVariablesBuilderTest {
	@Test
	public void testSequenceAfterSuiteBuilder() {
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
            	traceVariables().trace("asdf").trace("qwer");
            }
        };
        
        builder.configure();
        
        assertEquals(builder.getTestCase().getActions().size(), 1);
        assertEquals(builder.getTestCase().getActions().get(0).getClass(), TraceVariablesAction.class);
        assertEquals(builder.getTestCase().getActions().get(0).getName(), TraceVariablesAction.class.getSimpleName());
        
        TraceVariablesAction action = (TraceVariablesAction)builder.getTestCase().getActions().get(0);
        assertEquals(action.getVariableNames().size(), 2);
        assertEquals(action.getVariableNames().toString(), "[asdf, qwer]");
	}
}
