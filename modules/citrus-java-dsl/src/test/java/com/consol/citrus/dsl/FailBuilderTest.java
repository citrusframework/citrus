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
				fail("This test will fail.");
			}
		};
		
		builder.configure();
		
		
		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
		Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), FailAction.class);
		
		FailAction action = (FailAction)builder.getTestCase().getActions().get(0);
		Assert.assertEquals(action.getName(), FailAction.class.getSimpleName());
        Assert.assertEquals(action.getMessage(), "This test will fail.");
		}
}