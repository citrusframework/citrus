package com.consol.citrus.dsl;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.InputAction;

public class InputBuilderTest {

	@Test
	public void TestInputBuilder(){
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
            	input()
            		.message("TestMessage")
            		.variable("TestVariable")
            		.validAnswer("Yes", "No", "Maybe");
            	}
            };
            
            builder.configure();
            
            Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
            Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), InputAction.class);
            
            InputAction action = (InputAction)builder.getTestCase().getActions().get(0);
            Assert.assertEquals(action.getName(), InputAction.class.getSimpleName());
            Assert.assertEquals(action.getMessage(), "TestMessage");
            Assert.assertEquals(action.getValidAnswers(), "Yes/No/Maybe");
            Assert.assertEquals(action.getVariable(), "TestVariable");
	}
}
