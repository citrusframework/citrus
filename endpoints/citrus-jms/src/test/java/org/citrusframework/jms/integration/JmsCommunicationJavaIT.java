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
public class JmsCommunicationJavaIT implements TestActionSupport {

    @Test
    @CitrusTest
    public void jmsQueues(@CitrusResource TestCaseRunner runner) {
        String operation = "sayHello";

        runner.variable("correlationId", "citrus:randomNumber(10)");
        runner.variable("messageId", "citrus:randomNumber(10)");
        runner.variable("user", "Christoph");

        runner.when(send("helloServiceJmsEndpoint")
            .description("Send asynchronous hello request: TestFramework -> HelloService")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                               "<MessageId>${messageId}</MessageId>" +
                               "<CorrelationId>${correlationId}</CorrelationId>" +
                               "<User>${user}</User>" +
                               "<Text>Hello TestFramework</Text>" +
                           "</HelloRequest>")
            .header("Operation", operation)
            .header("CorrelationId", "${correlationId}"));

        runner.then(receive("helloServiceResponseJmsEndpoint")
            .description("Receive asynchronous hello response: HelloService -> TestFramework")
            .message()
            .body("<HelloResponse xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                                "<MessageId>${messageId}</MessageId>" +
                                "<CorrelationId>${correlationId}</CorrelationId>" +
                                "<User>HelloService</User>" +
                                "<Text>Hello ${user}</Text>" +
                            "</HelloResponse>")
            .header("Operation", operation)
            .header("CorrelationId", "${correlationId}"));

        runner.when(send("helloServiceJmsEndpoint")
            .message()
            .body(Resources.fromClasspath("org/citrusframework/jms/integration/helloRequest.xml"))
            .header("Operation", operation)
            .header("CorrelationId", "${correlationId}"));

        runner.then(receive("helloServiceResponseJmsEndpoint")
            .message()
            .body(Resources.fromClasspath("org/citrusframework/jms/integration/helloResponse.xml"))
            .header("Operation", operation)
            .header("CorrelationId", "${correlationId}"));
    }

    @Test
    @CitrusTest
    public void JmsCommunicationEmptyReceiveIT(@CitrusResource TestCaseRunner runner) {
        String operation = "sayHello";

        runner.variable("correlationId", "citrus:randomNumber(10)");
        runner.variable("messageId", "citrus:randomNumber(10)");
        runner.variable("user", "Christoph");

        runner.when(send("helloServiceJmsEndpoint")
                .description("Send asynchronous hello request: TestFramework -> HelloService")
                .message()
                .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                        "<MessageId>${messageId}</MessageId>" +
                        "<CorrelationId>${correlationId}</CorrelationId>" +
                        "<User>${user}</User>" +
                        "<Text>Hello TestFramework</Text>" +
                        "</HelloRequest>")
                .header("Operation", operation)
                .header("CorrelationId", "${correlationId}"));

        runner.then(receive("helloServiceResponseJmsEndpoint")
                .description("Receive asynchronous hello response: HelloService -> TestFramework"));

        runner.when(send("helloServiceJmsEndpoint")
                .message()
                .body(Resources.fromClasspath("org/citrusframework/jms/integration/helloRequest.xml"))
                .header("Operation", operation)
                .header("CorrelationId", "${correlationId}"));

        runner.then(receive("helloServiceResponseJmsEndpoint"));
    }
}
