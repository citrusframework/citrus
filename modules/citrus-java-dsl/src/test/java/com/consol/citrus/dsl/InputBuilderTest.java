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
            
            Assert.assertEquals(((InputAction)builder.getTestCase().getActions().get(0)).getName(), InputAction.class.getSimpleName());
            
            Assert.assertEquals(((InputAction)builder.getTestCase().getActions().get(0)).getMessage(), "TestMessage");
            Assert.assertEquals(((InputAction)builder.getTestCase().getActions().get(0)).getValidAnswers(), "Yes/No/Maybe");
            Assert.assertEquals(((InputAction)builder.getTestCase().getActions().get(0)).getVariable(), "TestVariable");
	}
}
