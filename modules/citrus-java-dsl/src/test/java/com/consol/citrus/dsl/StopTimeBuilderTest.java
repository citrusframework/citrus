package com.consol.citrus.dsl;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.StopTimeAction;

public class StopTimeBuilderTest {
	@Test
	public void testStopTimeBuilder(){
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
		@Override
		protected void configure(){
			stopTime("TestId");
		}
	};
	
	builder.configure();
	
	
	Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
	Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), StopTimeAction.class);
	
	StopTimeAction action = (StopTimeAction)builder.getTestCase().getActions().get(0);
	Assert.assertEquals(action.getName(), StopTimeAction.class.getSimpleName());
    Assert.assertEquals(action.getId(), "TestId");
	}
}
