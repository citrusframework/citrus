package com.consol.citrus.dsl;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.TraceVariablesAction;

public class TraceVariablesBuilderTest {

		@Test
		public void testTraceVariablesBuilder(){
			TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
			@Override
			protected void configure(){
				traceVariables().trace("Var1")
								.trace("Var2");
			}
		};
			
		builder.configure();
		
		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
		Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), TraceVariablesAction.class);
		
		TraceVariablesAction action = (TraceVariablesAction)builder.getTestCase().getActions().get(0);
		Assert.assertEquals(action.getName(), TraceVariablesAction.class.getSimpleName());
		Assert.assertEquals(action.getVariableNames().toString(), "[Var1, Var2]");
		}
}
