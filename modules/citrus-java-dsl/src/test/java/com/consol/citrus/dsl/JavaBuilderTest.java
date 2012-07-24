package com.consol.citrus.dsl;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.JavaAction;

public class JavaBuilderTest {
	
	//FIXME: fix everything
	@Test
	public void testJavaBuilder() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		final List<Object> args = new ArrayList<Object>();
		args.add(5);
		
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
			@Override
			public void configure() {
				java("com.consol.citrus.dsl.Test")
				      .constructorArgs(args);
			}
		};
		
		builder.configure();
//		
//		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
//        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), JavaAction.class);
//        
//        JavaAction action = (JavaAction)(builder.getTestCase().getActions().get(0));
//        Assert.assertEquals(action.getName(), JavaAction.class.getSimpleName());
//        
//        Assert.assertEquals(action.getConstructorArgs().size(), 3);
//        Assert.assertEquals(action.getMethodArgs().size(), 3);
//        Assert.assertEquals(action.getMethodName(), "invoke");
	}

}
