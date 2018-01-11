/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.actions;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractTestNGUnitTest;

/**
 * @author Philipp Komninos
 */
public class TransformActionTest extends AbstractTestNGUnitTest {
	
	@Test
	public void testTransform() {
		TransformAction transformAction = new TransformAction();
		transformAction.setXmlData("<TestRequest><Message>Hello World!</Message></TestRequest>");
		StringBuilder xsltDoc = new StringBuilder();
		xsltDoc.append("<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n");
		xsltDoc.append("<xsl:output method=\"text\"/>");
		xsltDoc.append("<xsl:template match=\"/\">\n");
		xsltDoc.append("Message: <xsl:value-of select=\"TestRequest/Message\"/>");
		xsltDoc.append("</xsl:template>\n");
		xsltDoc.append("</xsl:stylesheet>");
		transformAction.setXsltData(xsltDoc.toString());
		transformAction.setTargetVariable("var");
		
		transformAction.execute(context);
		
		Assert.assertEquals(context.getVariable("var").trim(), "Message: Hello World!");
	}
	
	@Test
	public void testTransformResource() {
		TransformAction transformAction = new TransformAction();
		transformAction.setXmlResourcePath("classpath:com/consol/citrus/actions/test-request-payload.xml");
		transformAction.setXsltResourcePath("classpath:com/consol/citrus/actions/test-transform.xslt");
		transformAction.setTargetVariable("var");
		
		transformAction.execute(context);
		
		Assert.assertEquals(context.getVariable("var").trim(), "Message: Hello World!");
	}
}
