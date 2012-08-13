package com.consol.citrus.dsl;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.CreateVariablesAction;

public class CreateVariablesBuilderTest {

		@Test
		public void testCreateVariablesBuilder(){
			TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
			@Override
			protected void configure(){
				createVariables().add("Var1", "Val1")
								 .add("Var2", "Val2");
			}
		};
		
		builder.configure();
		
		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
		Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), CreateVariablesAction.class);
		
		CreateVariablesAction action = (CreateVariablesAction)builder.getTestCase().getActions().get(0);
		Assert.assertEquals(action.getName(), CreateVariablesAction.class.getSimpleName());
		Assert.assertEquals(action.getVariables().toString(), "{Var1=Val1, Var2=Val2}");
		
	}
}
