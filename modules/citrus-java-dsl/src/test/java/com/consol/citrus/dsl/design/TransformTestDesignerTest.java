/*
 * Copyright 2006-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.dsl.design;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.TransformAction;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.Mockito.*;


public class TransformTestDesignerTest extends AbstractTestNGUnitTest {
	private Resource xmlResource = Mockito.mock(Resource.class);
	private Resource xsltResource = Mockito.mock(Resource.class);
	
	@Test
	public void testTransformBuilderWithData() {
		MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
    		@Override
    		public void configure() {
    		    transform()
    				.source("<Test>XML</test>")
    				.xslt("XSLT")
    		        .result("result");
			}
		};

		builder.configure();

		TestCase test = builder.getTestCase();
		Assert.assertEquals(test.getActionCount(), 1);
		Assert.assertEquals(test.getActions().get(0).getClass(), TransformAction.class);
		
		TransformAction action = (TransformAction)test.getActions().get(0);
		
		Assert.assertEquals(action.getName(), "transform");
		Assert.assertEquals(action.getXmlData(), "<Test>XML</test>");
		Assert.assertEquals(action.getXsltData(), "XSLT");
		Assert.assertEquals(action.getTargetVariable(), "result");
	}
		
	@Test
	public void testTransformBuilderWithResource() throws IOException {
		MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
			@Override
			public void configure() {
				transform()
					.source(xmlResource)
					.xslt(xsltResource)
					.result("result");
						
			}
		};
		
		reset(xmlResource, xsltResource);
        when(xmlResource.getInputStream()).thenReturn(new ByteArrayInputStream("xmlData".getBytes()));
        when(xsltResource.getInputStream()).thenReturn(new ByteArrayInputStream("xsltSource".getBytes()));
		builder.configure();

		TestCase test = builder.getTestCase();
		Assert.assertEquals(test.getActionCount(), 1);
		Assert.assertEquals(test.getActions().get(0).getClass(), TransformAction.class);
		
		TransformAction action = (TransformAction)test.getActions().get(0);
		
		Assert.assertEquals(action.getName(), "transform");
		Assert.assertEquals(action.getXmlData(), "xmlData");
		Assert.assertEquals(action.getXsltData(), "xsltSource");
		Assert.assertEquals(action.getTargetVariable(), "result");
	}
}
