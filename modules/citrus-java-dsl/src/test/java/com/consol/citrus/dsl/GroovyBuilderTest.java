package com.consol.citrus.dsl;

import org.easymock.EasyMock;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.script.GroovyAction;

public class GroovyBuilderTest {
	Resource resource = EasyMock.createMock(Resource.class);
	Resource scriptTemplate = EasyMock.createMock(Resource.class);
			
	@Test
	public void testGroovyBuilderWithResource(){
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
			@Override
			protected void configure(){
				groovy()
				.fileResource(resource)
				.useScriptTemplate(false);
			}
		};
		
		builder.configure();
		
		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
		Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), GroovyAction.class);
		
		GroovyAction action = (GroovyAction)builder.getTestCase().getActions().get(0);
		Assert.assertEquals(action.getFileResource(), resource);
		Assert.assertEquals(action.isUseScriptTemplate(), false);
	}
	
	@Test
	public void testGroovyBuilderWithScript(){
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
			@Override
			protected void configure(){
				groovy()
				.script("//GroovyCode")
				.useScriptTemplate(false);
			}
		};
		
		builder.configure();
		
		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
		Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), GroovyAction.class);
		
		GroovyAction action = (GroovyAction)builder.getTestCase().getActions().get(0);
		Assert.assertEquals(action.getScript(), "//GroovyCode");
		Assert.assertEquals(action.isUseScriptTemplate(), false);
	}
	
	@Test
	public void testGroovyBuilderWithTemplate(){
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
			@Override
			protected void configure(){
				groovy()
				.scriptTemplateResource(scriptTemplate)
				.useScriptTemplate(true);
			}
		};
		
		builder.configure();
		
		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
		Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), GroovyAction.class);
		
		GroovyAction action = (GroovyAction)builder.getTestCase().getActions().get(0);
		Assert.assertEquals(action.getScriptTemplateResource(), scriptTemplate);
		Assert.assertEquals(action.isUseScriptTemplate(), true);
	}
}
