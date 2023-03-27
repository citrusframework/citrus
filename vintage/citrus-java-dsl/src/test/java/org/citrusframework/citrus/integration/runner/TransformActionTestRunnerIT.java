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

package org.citrusframework.citrus.integration.runner;

import org.citrusframework.citrus.annotations.CitrusTest;
import org.citrusframework.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class TransformActionTestRunnerIT extends TestNGCitrusTestRunner {

    @CitrusTest
    public void transformAction() {
        transform(builder -> builder.source("<TestRequest>" +
                "<Message>Hello World!</Message>" +
                "</TestRequest>")
                .xslt("<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
                        "<xsl:template match=\"/\">\n" +
                        "<html>\n" +
                        "<body>\n" +
                        "<h2>Test Request</h2>\n" +
                        "<p>Message: <xsl:value-of select=\"TestRequest/Message\"/></p>\n" +
                        "</body>\n" +
                        "</html>\n" +
                        "</xsl:template>\n" +
                        "</xsl:stylesheet>")
                .result("result"));

        echo("${result}");

        transform(builder -> builder.source(new ClassPathResource("org/citrusframework/citrus/actions/transform-source.xml"))
                .xslt(new ClassPathResource("org/citrusframework/citrus/actions/transform.xslt"))
                .result("result"));

        echo("${result}");
    }
}
