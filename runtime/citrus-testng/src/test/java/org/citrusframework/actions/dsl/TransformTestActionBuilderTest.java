/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.actions.dsl;

import java.io.IOException;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.TransformAction;
import org.citrusframework.spi.Resources;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.TransformAction.Builder.transform;

public class TransformTestActionBuilderTest extends UnitTestSupport {
    @Test
    public void testTransformBuilderWithData() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(transform().source("<TestRequest>" +
                            "<Message>Hello World!</Message>" +
                        "</TestRequest>")
                .xslt(String.format("<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">%n" +
                        "<xsl:template match=\"/\">%n" +
                        "<html>%n" +
                        "<body>%n" +
                        "<h2>Test Request</h2>%n" +
                        "<p>Message: <xsl:value-of select=\"TestRequest/Message\"/></p>%n" +
                        "</body>%n" +
                        "</html>%n" +
                        "</xsl:template>%n" +
                        "</xsl:stylesheet>"))
                .result("result"));

        Assert.assertNotNull(context.getVariable("result"));
        Assert.assertEquals(context.getVariable("result"), String.format("<html>%n" +
					"    <body>%n" +
						"        <h2>Test Request</h2>%n" +
						"        <p>Message: Hello World!</p>%n" +
					"    </body>%n" +
				"</html>%n"));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), TransformAction.class);

        TransformAction action = (TransformAction)test.getActions().get(0);

        Assert.assertEquals(action.getName(), "transform");
        Assert.assertTrue(action.getXmlData().startsWith("<TestRequest>"));
        Assert.assertTrue(action.getXsltData().contains("<h2>Test Request</h2>"));
        Assert.assertEquals(action.getTargetVariable(), "result");
    }

    @Test
    public void testTransformBuilderWithResource() throws IOException {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(transform().source(Resources.fromClasspath("org/citrusframework/actions/dsl/transform-source.xml"))
                .xslt(Resources.fromClasspath("org/citrusframework/actions/dsl/transform.xslt"))
                .result("result"));

		Assert.assertNotNull(context.getVariable("result"));
		Assert.assertEquals(context.getVariable("result"), String.format("<html>%n" +
					"    <body>%n" +
						"        <h2>Test Request</h2>%n" +
						"        <p>Message: Hello World!</p>%n" +
					"    </body>%n" +
				"</html>%n"));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), TransformAction.class);

        TransformAction action = (TransformAction)test.getActions().get(0);

		Assert.assertEquals(action.getName(), "transform");
		Assert.assertTrue(action.getXmlData().contains("<TestRequest>"));
		Assert.assertTrue(action.getXsltData().contains("<h2>Test Request</h2>"));
		Assert.assertEquals(action.getTargetVariable(), "result");
    }
}
