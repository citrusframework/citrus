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

package org.citrusframework.citrus.jms.integration;

import org.citrusframework.citrus.annotations.CitrusTest;
import org.citrusframework.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

import static org.citrusframework.citrus.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.citrus.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 */
@Test
public class JmsCommunicationJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void jmsQueues() {
        String operation = "sayHello";

        variable("correlationId", "citrus:randomNumber(10)");
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");

        when(send("helloServiceJmsEndpoint")
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

        then(receive("helloServiceResponseJmsEndpoint")
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

        when(send("helloServiceJmsEndpoint")
            .message()
            .body(new ClassPathResource("org/citrusframework/citrus/jms/integration/helloRequest.xml"))
            .header("Operation", operation)
            .header("CorrelationId", "${correlationId}"));

        then(receive("helloServiceResponseJmsEndpoint")
            .message()
            .body(new ClassPathResource("org/citrusframework/citrus/jms/integration/helloResponse.xml"))
            .header("Operation", operation)
            .header("CorrelationId", "${correlationId}"));
    }

    @CitrusTest
    public void JmsCommunicationEmptyReceiveIT() {
        String operation = "sayHello";

        variable("correlationId", "citrus:randomNumber(10)");
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");

        when(send("helloServiceJmsEndpoint")
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

        then(receive("helloServiceResponseJmsEndpoint")
                .description("Receive asynchronous hello response: HelloService -> TestFramework"));

        when(send("helloServiceJmsEndpoint")
                .message()
                .body(new ClassPathResource("org/citrusframework/citrus/jms/integration/helloRequest.xml"))
                .header("Operation", operation)
                .header("CorrelationId", "${correlationId}"));

        then(receive("helloServiceResponseJmsEndpoint"));
    }
}
