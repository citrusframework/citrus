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

package com.consol.citrus.dsl.definition;

import org.easymock.EasyMock;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.TransformAction;
import com.consol.citrus.dsl.TestNGCitrusTestBuilder;

public class TransformDefinitionTest {
	private Resource xmlResource = EasyMock.createMock(Resource.class);
	private Resource xsltResource = EasyMock.createMock(Resource.class);
	
	@Test
	public void testTransformBuilderWithData() {
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
    		@Override
    		public void configure() {
    		    transform()
    				.source("<Test>XML</test>")
    				.xslt("XSLT")
    		        .result("result");
			}
		};
	
		builder.configure();
		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
		Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), TransformAction.class);
		
		TransformAction action = (TransformAction)builder.getTestCase().getActions().get(0);
		
		Assert.assertEquals(action.getName(), TransformAction.class.getSimpleName());
		Assert.assertEquals(action.getXmlData(), "<Test>XML</test>");
		Assert.assertEquals(action.getXsltData(), "XSLT");
		Assert.assertEquals(action.getTargetVariable(), "result");
	}
		
	@Test
	public void testTransformBuilderWithResource() {
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
			@Override
			public void configure() {
				transform()
					.source(xmlResource)
					.xslt(xsltResource)
					.result("result");
						
			}
		};
		
		builder.configure();
		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
		Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), TransformAction.class);
		
		TransformAction action = (TransformAction)builder.getTestCase().getActions().get(0);
		
		Assert.assertEquals(action.getName(), TransformAction.class.getSimpleName());
		Assert.assertEquals(action.getXmlResource(), xmlResource);
		Assert.assertEquals(action.getXsltResource(), xsltResource);
		Assert.assertEquals(action.getTargetVariable(), "result");
	}
}
