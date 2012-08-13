package com.consol.citrus.dsl;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.consol.citrus.container.Conditional;

public class ConditionalBuilderTest {
	@Test
	public void testConditionalBuilder() {
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
			@Override
			public void configure() {
				conditional(echo("${var}")).expression("${var} = 5");
			}
		};
		
		builder.configure();
		
		assertEquals(builder.getTestCase().getActions().size(), 1);
        assertEquals(builder.getTestCase().getActions().get(0).getClass(), Conditional.class);
        assertEquals(builder.getTestCase().getActions().get(0).getName(), Conditional.class.getSimpleName());
        
        Conditional container = (Conditional)builder.getTestCase().getActions().get(0);
        assertEquals(container.getActions().size(), 1);
        assertEquals(container.getExpression(), "${var} = 5");
	}
}
