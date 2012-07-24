package com.consol.citrus.dsl;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.FailAction;

public class FailBuilderTest {

		@Test
		public void testFailBuilder(){
			TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
			@Override
			protected void configure(){
				fail("This test shall not pass.");
			}
		};
		
		builder.configure();
		
		
		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
		Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), FailAction.class);
		
		Assert.assertEquals(((FailAction)builder.getTestCase().getActions().get(0)).getName(), FailAction.class.getSimpleName());
        Assert.assertEquals(((FailAction)builder.getTestCase().getActions().get(0)).getMessage(), "This test shall not pass.");
		}
}