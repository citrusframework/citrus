package com.consol.citrus.dsl;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.SequenceBeforeSuite;
import com.consol.citrus.report.TestSuiteListeners;

public class SequenceBeforeSuiteBuilderTest {
	@Test
	public void testSequenceBeforeSuiteBuilder() {
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
            	SequenceAfterSuiteDefinition afterSuite = sequenceAfterSuite(echo("${var}"));
            	
            	sequenceBeforeSuite(echo("asdf"), sleep(2.0))
            	.afterSuiteActions(afterSuite.getAction())
            	.testSuiteListener(new TestSuiteListeners());
            }
        };
        
        builder.configure();
        
        assertEquals(builder.getTestCase().getActions().size(), 2);
        assertEquals(builder.getTestCase().getActions().get(1).getClass(), SequenceBeforeSuite.class);
        assertEquals(builder.getTestCase().getActions().get(1).getName(), SequenceBeforeSuite.class.getSimpleName());
        
        SequenceBeforeSuite container = (SequenceBeforeSuite)builder.getTestCase().getActions().get(1);
        assertEquals(container.getActions().size(), 2);
        assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
	}
}
