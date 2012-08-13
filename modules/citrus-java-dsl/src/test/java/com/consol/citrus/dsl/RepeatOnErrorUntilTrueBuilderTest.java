package com.consol.citrus.dsl;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.RepeatOnErrorUntilTrue;

public class RepeatOnErrorUntilTrueBuilderTest {
	@Test
	public void testRepeatOnErrorUntilTrueBuilder() {
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
            	repeatOnErrorUntilTrue(echo("${var}"), sleep(3.0), echo("${var}"))
            		.autoSleep(100)
            		.index("i")
            		.condition("i lt 5")
            		.startsWith(2);
            }
        };
        
        builder.configure();
        
        assertEquals(builder.getTestCase().getActions().size(), 1);
        assertEquals(builder.getTestCase().getActions().get(0).getClass(), RepeatOnErrorUntilTrue.class);
        assertEquals(builder.getTestCase().getActions().get(0).getName(), RepeatOnErrorUntilTrue.class.getSimpleName());
        
        RepeatOnErrorUntilTrue container = (RepeatOnErrorUntilTrue)builder.getTestCase().getActions().get(0);
        assertEquals(container.getActions().size(), 3);
        assertEquals(container.getAutoSleep(), 100);
        assertEquals(container.getCondition(), "i lt 5");
        assertEquals(container.getIndex(), 2);
        assertEquals(container.getIndexName(), "i");
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);
	}
}
