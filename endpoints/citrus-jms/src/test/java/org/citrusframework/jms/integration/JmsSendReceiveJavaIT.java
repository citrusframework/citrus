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

package org.citrusframework.jms.integration;

import java.util.Collections;

import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.spi.Resources;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.parallel.Isolated;

@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Isolated("Shares embedded Artemis broker on fixed tcp://localhost:61616 with static queues/topics")
public class JmsSendReceiveJavaIT implements TestActionSupport {

    @Test
    @CitrusTest
    public void JmsSendReceiveJavaIT(@CitrusResource TestCaseRunner runner) {
        runner.variable("correlationId", "citrus:randomNumber(10)");
        runner.variable("correlationIdA", "citrus:randomNumber(10)");
        runner.variable("correlationIdB", "citrus:randomNumber(10)");
        runner.variable("messageId", "citrus:randomNumber(10)");
        runner.variable("messageIdA", "citrus:randomNumber(10)");
        runner.variable("messageIdB", "citrus:randomNumber(10)");
        runner.variable("user", "Christoph");

        runner.given(echo("Test 1: Send JMS request and receive async JMS response (inline CDATA payload)"));

        runner.when(send("helloServiceJmsEndpoint")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello Citrus</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.then(receive("helloServiceResponseJmsEndpoint")
            .message()
            .body("<HelloResponse xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                            "<MessageId>${messageId}</MessageId>" +
                            "<CorrelationId>${correlationId}</CorrelationId>" +
                            "<User>HelloService</User>" +
                            "<Text>Hello ${user}</Text>" +
                        "</HelloResponse>")
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}"));

        runner.given(echo("Test 2: Send JMS request and receive async JMS response (file resource payload)"));

        runner.when(send("helloServiceJmsEndpoint")
            .message()
            .body(Resources.fromClasspath("org/citrusframework/jms/integration/helloRequest.xml"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.then(receive("helloServiceResponseJmsEndpoint")
            .message()
            .body(Resources.fromClasspath("org/citrusframework/jms/integration/helloResponse.xml"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.given(echo("Test 3: Send JMS request and receive async JMS response (JMS message selector)"));

        runner.when(send("helloServiceJmsEndpoint")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageIdA}</MessageId>" +
                           "<CorrelationId>${correlationIdA}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello Citrus first time</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationIdA}"));

        runner.when(send("helloServiceJmsEndpoint")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageIdB}</MessageId>" +
                           "<CorrelationId>${correlationIdB}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello Citrus second time</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationIdB}"));

        runner.then(receive("helloServiceResponseJmsEndpoint")
            .selector(Collections.singletonMap("CorrelationId", "${correlationIdB}"))
            .message()
            .body("<HelloResponse xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                            "<MessageId>${messageIdB}</MessageId>" +
                            "<CorrelationId>${correlationIdB}</CorrelationId>" +
                            "<User>HelloService</User>" +
                            "<Text>Hello ${user}</Text>" +
                        "</HelloResponse>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationIdB}")
            .timeout(500));

        runner.then(receive("helloServiceResponseJmsEndpoint")
            .selector(Collections.singletonMap("CorrelationId", "${correlationIdA}"))
            .message()
            .body("<HelloResponse xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                            "<MessageId>${messageIdA}</MessageId>" +
                            "<CorrelationId>${correlationIdA}</CorrelationId>" +
                            "<User>HelloService</User>" +
                            "<Text>Hello ${user}</Text>" +
                        "</HelloResponse>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationIdA}")
            .timeout(500));

        runner.given(echo("Test 4: Receive JMS message timeout response"));

        runner.then(assertException()
            .exception(org.citrusframework.exceptions.ActionTimeoutException.class)
            .when(receive("helloServiceResponseJmsEndpoint")
                .selector(Collections.singletonMap("CorrelationId", "doesNotExist"))
                .message()
                .body("<HelloResponse xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                              "<MessageId>?</MessageId>" +
                              "<CorrelationId>?</CorrelationId>" +
                              "<User>HelloService</User>" +
                              "<Text>Hello ?</Text>" +
                          "</HelloResponse>")
                .header("Operation", "sayHello")
                .timeout(300)));
    }
}
