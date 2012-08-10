package com.consol.citrus.dsl;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.RepeatUntilTrue;

public class RepeatUntilTrueBuilderTest {
	@Test
	public void testRepeatUntilTrueBuilder() {
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
            	repeatUntilTrue(echo("${var}"), sleep(3.0), echo("${var}"))
            	.condition("i lt 5")
            	.index("i")
            	.startsWith(2);
            }
        };
        
        builder.configure();
        
        assertEquals(builder.getTestCase().getActions().size(), 1);
        assertEquals(builder.getTestCase().getActions().get(0).getClass(), RepeatUntilTrue.class);
        assertEquals(builder.getTestCase().getActions().get(0).getName(), RepeatUntilTrue.class.getSimpleName());
        
        RepeatUntilTrue container = (RepeatUntilTrue)builder.getTestCase().getActions().get(0);
        assertEquals(container.getActions().size(), 3);
        assertEquals(container.getCondition(), "i lt 5");
        assertEquals(container.getIndex(), 2);
        assertEquals(container.getIndexName(), "i");
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);
	}
}
