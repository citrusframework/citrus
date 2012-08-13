package com.consol.citrus.dsl;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.SequenceAfterSuite;
import com.consol.citrus.report.TestSuiteListeners;

public class SequenceAfterSuiteBuilderTest {
	@Test
	public void testSequenceAfterSuiteBuilder() {
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
            	sequenceAfterSuite(echo("${var}"), sleep(2.0), echo("asdf"))
            	.testSuiteListener(new TestSuiteListeners());
            }
        };
        
        builder.configure();
        
        assertEquals(builder.getTestCase().getActions().size(), 1);
        assertEquals(builder.getTestCase().getActions().get(0).getClass(), SequenceAfterSuite.class);
        assertEquals(builder.getTestCase().getActions().get(0).getName(), SequenceAfterSuite.class.getSimpleName());
        
        SequenceAfterSuite container = (SequenceAfterSuite)builder.getTestCase().getActions().get(0);
        assertEquals(container.getActions().size(), 3);
        assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
	}
}
