/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.integration;

import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.citrusframework.dsl.XmlSupport.xml;
import static org.citrusframework.dsl.XpathSupport.xpath;

/**
 * @author Christoph Deppisch
 */
@Test
public class ValidateXpathJavaIT extends TestNGCitrusSpringSupport {

    @Autowired
    private NamespaceContextBuilder namespaceContextBuilder;

    @CitrusTest
    public void validateXpath() {
        variable("correlationId", "citrus:randomNumber(10)");
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");

        $(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(receive("direct:hello")
            .message()
            .validate(xml().xpath().expression("//ns0:HelloRequest/ns0:MessageId", "${messageId}")
                             .expression("//ns0:HelloRequest/ns0:CorrelationId", "${correlationId}")
                             .expression("//ns0:HelloRequest/ns0:Text", "Hello ${user}")
                             .namespaceContext("ns0", "http://citrusframework.org/schemas/samples/HelloService.xsd"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(receive("direct:hello")
            .message()
            .validate(xml().xpath().expression("//ns1:HelloRequest/ns1:MessageId", "${messageId}")
                             .expression("//ns1:HelloRequest/ns1:CorrelationId", "${correlationId}")
                             .expression("//ns1:HelloRequest/ns1:Text", "Hello ${user}")
                             .namespaceContext("ns1", "http://citrusframework.org/schemas/samples/HelloService.xsd"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(receive("direct:hello")
            .message()
            .validate(xpath().expression("//:HelloRequest/:MessageId", "${messageId}")
                             .expression("//:HelloRequest/:CorrelationId", "${correlationId}")
                             .expression("//:HelloRequest/:Text", "Hello ${user}"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(receive("direct:hello")
            .message()
            .validate(xpath().expression("//:HelloRequest/:MessageId", "${messageId}"))
            .validate(xpath().expression("//:HelloRequest/:CorrelationId", "${correlationId}"))
            .validate(xpath().expression("//:HelloRequest/:Text", "Hello ${user}"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(echo("Now using xpath validation elements"));

        $(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(receive("direct:hello")
            .message()
            .validate(xml()
                    .xpath()
                    .expression("//ns0:HelloRequest/ns0:MessageId", "${messageId}")
                    .expression("//ns0:HelloRequest/ns0:CorrelationId", "${correlationId}")
                    .expression("//ns0:HelloRequest/ns0:Text", "Hello ${user}")
                    .namespaceContext("ns0", "http://citrusframework.org/schemas/samples/HelloService.xsd"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(receive("direct:hello")
            .message()
            .validate(xml()
                    .xpath().expression("//ns1:HelloRequest/ns1:MessageId", "${messageId}")
                             .expression("//ns1:HelloRequest/ns1:CorrelationId", "${correlationId}")
                             .expression("//ns1:HelloRequest/ns1:Text", "Hello ${user}")
                             .namespaceContext("ns1", "http://citrusframework.org/schemas/samples/HelloService.xsd"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(receive("direct:hello")
            .message()
            .validate(xpath().expression("//:HelloRequest/:MessageId", "${messageId}")
                             .expression("//:HelloRequest/:CorrelationId", "${correlationId}")
                             .expression("//:HelloRequest/:Text", "Hello ${user}"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(echo("Test: Default namespace mapping"));

        $(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(receive("direct:hello")
            .message()
            .validate(xml().xpath().expression("//def:HelloRequest/def:MessageId", "${messageId}")
                             .expression("//def:HelloRequest/def:CorrelationId", "${correlationId}")
                             .expression("//def:HelloRequest/def:Text", "Hello ${user}")
                             .namespaceContext(namespaceContextBuilder.getNamespaceMappings()))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}")
            .extract(xpath().expression("/def:HelloRequest/def:Text", "extractedText")));

        $(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assert.assertEquals(context.getVariable("extractedText"),
                                    context.replaceDynamicContentInString("Hello ${user}"));
            }
        });
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    @CitrusTest
    public void shouldFailOnMultipleXpathExpressionValidation() {
        variable("correlationId", "citrus:randomNumber(10)");
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");

        $(send("direct:hello")
                .message()
                .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                        "<MessageId>${messageId}</MessageId>" +
                        "<CorrelationId>${correlationId}</CorrelationId>" +
                        "<User>${user}</User>" +
                        "<Text>Hello ${user}</Text>" +
                        "</HelloRequest>")
                .header("Operation", "sayHello")
                .header("CorrelationId", "${correlationId}"));

        $(receive("direct:hello")
                .message()
                .validate(xpath().expression("//:HelloRequest/:MessageId", "${messageId}"))
                .validate(xpath().expression("//:HelloRequest/:CorrelationId", "${correlationId}"))
                .validate(xpath().expression("//:HelloRequest/:Text", "should fail"))
                .header("Operation", "sayHello")
                .header("CorrelationId", "${correlationId}"));
    }
}
