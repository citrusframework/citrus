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

import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Philipp Komninos
 */
public class TransformActionTest extends AbstractBaseTest {
	
	@Test
	public void testTransform(){
		TransformAction transformAction = new TransformAction();
		transformAction.setXmlData("<TestRequest><Message>Hello World!</Message></TestRequest>");
		StringBuilder xsltDoc = new StringBuilder();
		xsltDoc.append("<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n");
		xsltDoc.append("<xsl:template match=\"/\">\n");
		xsltDoc.append("<html>\n");
		xsltDoc.append("<body>\n");
		xsltDoc.append("<h2>Test Request</h2>\n");
		xsltDoc.append("<p>Message: <xsl:value-of select=\"TestRequest/Message\"/></p>\n");
		xsltDoc.append("</body>\n");  
		xsltDoc.append("</html>\n");
		xsltDoc.append("</xsl:template>\n");
		xsltDoc.append("</xsl:stylesheet>");
		transformAction.setXsltData(xsltDoc.toString());
		transformAction.setTargetVariable("var");
		
		transformAction.execute(context);
		
		StringBuilder transformedDoc = new StringBuilder();
		transformedDoc.append("<html>\n");
		transformedDoc.append("<body>\n");
		transformedDoc.append("<h2>Test Request</h2>\n");
		transformedDoc.append("<p>Message: Hello World!</p>\n");
		transformedDoc.append("</body>\n");
		transformedDoc.append("</html>\n");
		
		Assert.assertEquals(context.getVariable("var"), transformedDoc.toString());
	}
	
	@Test
	public void testTransformResource(){
		TransformAction transformAction = new TransformAction();
		transformAction.setXmlResource(new ClassPathResource("test-request-payload.xml", TransformActionTest.class));
		transformAction.setXsltResource(new ClassPathResource("test-transform.xslt", TransformActionTest.class));
		transformAction.setTargetVariable("var");
		
		transformAction.execute(context);
		
		StringBuilder transformedDoc = new StringBuilder();
		transformedDoc.append("<html>\n");
		transformedDoc.append("<body>\n");
		transformedDoc.append("<h2>Test Request</h2>\n");
		transformedDoc.append("<p>Message: Hello World!</p>\n");
		transformedDoc.append("</body>\n");
		transformedDoc.append("</html>\n");
		
		Assert.assertEquals(context.getVariable("var"), transformedDoc.toString());
	}
}
