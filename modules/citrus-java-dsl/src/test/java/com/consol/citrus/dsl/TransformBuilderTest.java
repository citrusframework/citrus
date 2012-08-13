package com.consol.citrus.dsl;

import org.easymock.EasyMock;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.TransformAction;

public class TransformBuilderTest {
	Resource xmlResource = EasyMock.createMock(Resource.class);
	Resource xsltResource = EasyMock.createMock(Resource.class);
	
	
		@Test
		public void testTransformBuilderWithData(){
			TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
			@Override
			protected void configure(){
				transform()
					.variable("Var1")
					.source("XML")
					.withXSLT("XSLT");
						
			}
		};
	
		builder.configure();
		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
		Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), TransformAction.class);
		
		TransformAction action = (TransformAction)builder.getTestCase().getActions().get(0);
		
		Assert.assertEquals(action.getName(), TransformAction.class.getSimpleName());
		Assert.assertEquals(action.getXmlData(), "XML");
		Assert.assertEquals(action.getXsltData(), "XSLT");
		Assert.assertEquals(action.getTargetVariable(), "Var1");
		
		
	}
		
		@Test
		public void testTransformBuilderWithResource(){
			TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
			@Override
			protected void configure(){
				transform()
					.variable("Var1")
					.source(xmlResource)
					.withXSLT(xsltResource);
						
			}
		};
		
		builder.configure();
		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
		Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), TransformAction.class);
		
		TransformAction action = (TransformAction)builder.getTestCase().getActions().get(0);
		
		Assert.assertEquals(action.getName(), TransformAction.class.getSimpleName());
		Assert.assertEquals(action.getXmlResource(), xmlResource);
		Assert.assertEquals(action.getXsltResource(), xsltResource);
		Assert.assertEquals(action.getTargetVariable(), "Var1");
	}
}
