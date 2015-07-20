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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.TransformAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.TransformActionBuilder;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class TransformTestRunnerTest extends AbstractTestNGUnitTest {
    @Test
    public void testTransformBuilderWithData() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                transform(new BuilderSupport<TransformActionBuilder>() {
                    @Override
                    public void configure(TransformActionBuilder builder) {
                        builder.source("<TestRequest>" +
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
                                .result("result");
                    }
                });
            }
        };

        TestContext context = builder.createTestContext();
        Assert.assertNotNull(context.getVariable("result"));
        Assert.assertEquals(context.getVariable("result"), String.format("<html>%n" +
					"<body>%n" +
						"<h2>Test Request</h2>%n" +
						"<p>Message: Hello World!</p>%n" +
					"</body>%n" +
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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                transform(new BuilderSupport<TransformActionBuilder>() {
                    @Override
                    public void configure(TransformActionBuilder builder) {
                        builder.source(new ClassPathResource("com/consol/citrus/dsl/runner/transform-source.xml"))
								.xslt(new ClassPathResource("com/consol/citrus/dsl/runner/transform.xslt"))
                                .result("result");
                    }
                });
            }
        };

		TestContext context = builder.createTestContext();
		Assert.assertNotNull(context.getVariable("result"));
		Assert.assertEquals(context.getVariable("result"), String.format("<html>%n" +
					"<body>%n" +
						"<h2>Test Request</h2>%n" +
						"<p>Message: Hello World!</p>%n" +
					"</body>%n" +
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
