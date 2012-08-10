package com.consol.citrus.dsl;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.JavaAction;

public class JavaBuilderTest {
	@Test
	public void testJavaBuilder() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		final List<Object> constructorArgs = new ArrayList<Object>();
		constructorArgs.add(5);
		constructorArgs.add(7);
		
		final List<Object> methodArgs = new ArrayList<Object>();
		methodArgs.add(4);
		
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
			@Override
			public void configure() {
				java("com.consol.citrus.dsl.util.JavaTest")
				      .constructorArgs(constructorArgs)
				      .methodArgs(methodArgs)
				      .methodName("add");
			}
		};
		
		builder.configure();
		
		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), JavaAction.class);
        
        JavaAction action = ((JavaAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), JavaAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMethodName(), "add");
        Assert.assertEquals(action.getMethodArgs().size(), 1);
	}

}
