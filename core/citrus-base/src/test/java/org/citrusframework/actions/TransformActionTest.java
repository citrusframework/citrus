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

package org.citrusframework.actions;

import org.citrusframework.UnitTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Philipp Komninos
 */
public class TransformActionTest extends UnitTestSupport {

	@Test
	public void testTransform() {
		StringBuilder xsltDoc = new StringBuilder();
		xsltDoc.append("<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n");
		xsltDoc.append("<xsl:output method=\"text\"/>");
		xsltDoc.append("<xsl:template match=\"/\">\n");
		xsltDoc.append("Message: <xsl:value-of select=\"TestRequest/Message\"/>");
		xsltDoc.append("</xsl:template>\n");
		xsltDoc.append("</xsl:stylesheet>");

		TransformAction transformAction = new TransformAction.Builder()
				.source("<TestRequest><Message>Hello World!</Message></TestRequest>")
				.xslt(xsltDoc.toString())
				.result("var")
				.build();
		transformAction.execute(context);

		Assert.assertEquals(context.getVariable("var").trim(), "Message: Hello World!");
	}

	@Test
	public void testTransformResource() {
		TransformAction transformAction = new TransformAction.Builder()
				.sourceFile("classpath:org/citrusframework/actions/test-request-payload.xml")
				.xsltFile("classpath:org/citrusframework/actions/test-transform.xslt")
				.result("var")
				.build();
		transformAction.execute(context);

		Assert.assertEquals(context.getVariable("var").trim(), "Message: Hello World!");
	}
}
