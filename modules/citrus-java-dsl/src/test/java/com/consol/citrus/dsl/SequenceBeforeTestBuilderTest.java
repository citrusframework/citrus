package com.consol.citrus.dsl;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.SequenceBeforeTest;

public class SequenceBeforeTestBuilderTest {
	@Test
	public void testSequenceBeforeTestBuilder() {
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
            	sequenceBeforeTest(echo("${var}"), sleep(2.0));
            }
        };
        
        builder.configure();
        
        assertEquals(builder.getTestCase().getActions().size(), 1);
        assertEquals(builder.getTestCase().getActions().get(0).getClass(), SequenceBeforeTest.class);
        assertEquals(builder.getTestCase().getActions().get(0).getName(), SequenceBeforeTest.class.getSimpleName());
        
        SequenceBeforeTest container = (SequenceBeforeTest)builder.getTestCase().getActions().get(0);
        assertEquals(container.getActions().size(), 2);
        assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
	}
}
