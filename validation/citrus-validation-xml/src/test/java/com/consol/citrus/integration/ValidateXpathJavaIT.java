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

package com.consol.citrus.integration;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.EchoAction.Builder.echo;
import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.dsl.XmlSupport.xml;
import static com.consol.citrus.dsl.XpathSupport.xpath;
import static com.consol.citrus.script.GroovyAction.Builder.groovy;

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

        $(groovy("assert context.getVariable('extractedText') == 'Hello ${user}'"));
    }
}
