package com.consol.citrus.dsl;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.LoadPropertiesAction;

public class LoadBuilderTest {
	@Test
	public void TestLoadBuilder(){
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
            	load("TestFile.txt");
            	}
            };
            
            builder.configure();
            
            Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
            Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), LoadPropertiesAction.class);
            
            Assert.assertEquals(((LoadPropertiesAction)builder.getTestCase().getActions().get(0)).getName(), LoadPropertiesAction.class.getSimpleName());
            
            Assert.assertEquals(((LoadPropertiesAction)builder.getTestCase().getActions().get(0)).getFile(), "TestFile.txt");
	}
}
