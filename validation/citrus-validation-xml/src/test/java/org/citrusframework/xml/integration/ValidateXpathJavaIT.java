/*
 * Copyright the original author or authors.
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

package org.citrusframework.xml.integration;

import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.citrusframework.api.xml.namespace.NamespaceContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@CitrusSpringSupport
@ContextConfiguration(classes = {CitrusSpringConfig.class})
public class ValidateXpathJavaIT implements TestActionSupport {

    @Autowired
    private NamespaceContextBuilder namespaceContextBuilder;

    @Test
    @CitrusTest
    public void validateXpath(@CitrusResource TestCaseRunner runner) {
        runner.variable("correlationId", "citrus:randomNumber(10)");
        runner.variable("messageId", "citrus:randomNumber(10)");
        runner.variable("user", "Christoph");

        runner.run(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.run(receive("direct:hello")
            .message()
            .validate(validation().xpath().expression("//ns0:HelloRequest/ns0:MessageId", "${messageId}")
                             .expression("//ns0:HelloRequest/ns0:CorrelationId", "${correlationId}")
                             .expression("//ns0:HelloRequest/ns0:Text", "Hello ${user}")
                             .namespaceContext("ns0", "http://citrusframework.org/schemas/samples/HelloService.xsd"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.run(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.run(receive("direct:hello")
            .message()
            .validate(validation().xpath().expression("//ns1:HelloRequest/ns1:MessageId", "${messageId}")
                             .expression("//ns1:HelloRequest/ns1:CorrelationId", "${correlationId}")
                             .expression("//ns1:HelloRequest/ns1:Text", "Hello ${user}")
                             .namespaceContext("ns1", "http://citrusframework.org/schemas/samples/HelloService.xsd"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.run(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.run(receive("direct:hello")
            .message()
            .validate(validation().xpath().expression("//:HelloRequest/:MessageId", "${messageId}")
                             .expression("//:HelloRequest/:CorrelationId", "${correlationId}")
                             .expression("//:HelloRequest/:Text", "Hello ${user}"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.run(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.run(receive("direct:hello")
            .message()
            .validate(validation().xpath().expression("//:HelloRequest/:MessageId", "${messageId}"))
            .validate(validation().xpath().expression("//:HelloRequest/:CorrelationId", "${correlationId}"))
            .validate(validation().xpath().expression("//:HelloRequest/:Text", "Hello ${user}"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.run(echo("Now using xpath validation elements"));

        runner.run(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.run(receive("direct:hello")
            .message()
            .validate(validation().xml()
                    .xpath()
                    .expression("//ns0:HelloRequest/ns0:MessageId", "${messageId}")
                    .expression("//ns0:HelloRequest/ns0:CorrelationId", "${correlationId}")
                    .expression("//ns0:HelloRequest/ns0:Text", "Hello ${user}")
                    .namespaceContext("ns0", "http://citrusframework.org/schemas/samples/HelloService.xsd"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.run(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.run(receive("direct:hello")
            .message()
            .validate(validation().xml()
                    .xpath().expression("//ns1:HelloRequest/ns1:MessageId", "${messageId}")
                             .expression("//ns1:HelloRequest/ns1:CorrelationId", "${correlationId}")
                             .expression("//ns1:HelloRequest/ns1:Text", "Hello ${user}")
                             .namespaceContext("ns1", "http://citrusframework.org/schemas/samples/HelloService.xsd"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.run(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.run(receive("direct:hello")
            .message()
            .validate(validation().xpath().expression("//:HelloRequest/:MessageId", "${messageId}")
                             .expression("//:HelloRequest/:CorrelationId", "${correlationId}")
                             .expression("//:HelloRequest/:Text", "Hello ${user}"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.run(echo("Test: Default namespace mapping"));

        runner.run(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.run(receive("direct:hello")
            .message()
            .validate(validation().xpath().expression("//def:HelloRequest/def:MessageId", "${messageId}")
                             .expression("//def:HelloRequest/def:CorrelationId", "${correlationId}")
                             .expression("//def:HelloRequest/def:Text", "Hello ${user}")
                             .namespaceContext(namespaceContextBuilder.getNamespaceMappings()))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}")
            .extract(extractor().xpath().expression("/def:HelloRequest/def:Text", "extractedText")));

        runner.run(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assertions.assertEquals(context.replaceDynamicContentInString("Hello ${user}"),
                                    context.getVariable("extractedText"));
            }
        });
    }

    @Test
    @CitrusTest
    public void shouldFailOnMultipleXpathExpressionValidation(@CitrusResource TestCaseRunner runner) {
        assertThrows(TestCaseFailedException.class, () -> {
            runner.variable("correlationId", "citrus:randomNumber(10)");
            runner.variable("messageId", "citrus:randomNumber(10)");
            runner.variable("user", "Christoph");

            runner.run(send("direct:hello")
                    .message()
                    .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                            "<MessageId>${messageId}</MessageId>" +
                            "<CorrelationId>${correlationId}</CorrelationId>" +
                            "<User>${user}</User>" +
                            "<Text>Hello ${user}</Text>" +
                            "</HelloRequest>")
                    .header("Operation", "sayHello")
                    .header("CorrelationId", "${correlationId}"));

            runner.run(receive("direct:hello")
                    .message()
                    .validate(validation().xpath().expression("//:HelloRequest/:MessageId", "${messageId}"))
                    .validate(validation().xpath().expression("//:HelloRequest/:CorrelationId", "${correlationId}"))
                    .validate(validation().xpath().expression("//:HelloRequest/:Text", "should fail"))
                    .header("Operation", "sayHello")
                    .header("CorrelationId", "${correlationId}"));
        });
    }
}
