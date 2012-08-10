package com.consol.citrus.dsl;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.Sequence;

public class SequenceBuilderTest {
	@Test
	public void testSequenceBuilder() {
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
            	sequential(echo("${var}"), sleep(5.0));
            }
        };
        
        builder.configure();
        
        assertEquals(builder.getTestCase().getActions().size(), 1);
        assertEquals(builder.getTestCase().getActions().get(0).getClass(), Sequence.class);
        assertEquals(builder.getTestCase().getActions().get(0).getName(), Sequence.class.getSimpleName());
        
        Sequence container = (Sequence)builder.getTestCase().getActions().get(0);
        assertEquals(container.getActions().size(), 2);
        assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
	}
}
