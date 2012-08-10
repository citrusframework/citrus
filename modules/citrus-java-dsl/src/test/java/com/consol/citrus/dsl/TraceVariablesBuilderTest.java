package com.consol.citrus.dsl;

import org.testng.Assert;
import org.testng.annotations.Test;


import com.consol.citrus.actions.TraceVariablesAction;

public class TraceVariablesBuilderTest 
{

	@Test
	public void testTraceVariablesBuilder(){
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
		@Override
		protected void configure(){
			traceVariables().trace("test1");
			
		}
	};
	
	builder.configure();
	
	
	Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
	Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), TraceVariablesAction.class);
	
	Assert.assertEquals(((TraceVariablesAction)builder.getTestCase().getActions().get(0)).getName(), TraceVariablesAction.class.getSimpleName());
	Assert.assertEquals(((TraceVariablesAction)builder.getTestCase().getActions().get(0)).getVariableNames().toString(), "[test1]");

	}
}