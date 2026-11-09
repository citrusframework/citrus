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
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.test.context.ContextConfiguration;

@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Isolated("Shares embedded Artemis broker on fixed tcp://localhost:61616 with static queues/topics")
public class SyncJmsTopicJavaIT implements TestActionSupport {

    @Test
    @CitrusTest
    public void syncJmsTopic(@CitrusResource TestCaseRunner runner) {
        runner.variable("correlationId", "citrus:randomNumber(10)");
        runner.variable("messageId", "citrus:randomNumber(10)");
        runner.variable("user", "Christoph");

        runner.given(
            async().actions(receive("syncJmsTopicSubscriberEndpoint")
                .message()
                .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                        "<MessageId>${messageId}</MessageId>" +
                        "<CorrelationId>${correlationId}</CorrelationId>" +
                        "<User>${user}</User>" +
                        "<Text>Hello TestFramework</Text>" +
                        "</HelloRequest>")
                .header("Operation", "sayHello")
                .header("CorrelationId", "${correlationId}"))
        );

        runner.then(delay().milliseconds(2000));

        runner.when(send("syncJmsTopicEndpoint")
            .fork(true)
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                    "<MessageId>${messageId}</MessageId>" +
                    "<CorrelationId>${correlationId}</CorrelationId>" +
                    "<User>${user}</User>" +
                    "<Text>Hello TestFramework</Text>" +
                    "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.then(send("syncJmsTopicSubscriberEndpoint")
            .message()
            .body("<HelloResponse xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                    "<MessageId>${messageId}</MessageId>" +
                    "<CorrelationId>${correlationId}</CorrelationId>" +
                    "<User>HelloService</User>" +
                    "<Text>Hello ${user}</Text>" +
                    "</HelloResponse>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        runner.then(receive("syncJmsTopicEndpoint")
            .message()
            .body("<HelloResponse xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                                    "<MessageId>${messageId}</MessageId>" +
                                    "<CorrelationId>${correlationId}</CorrelationId>" +
                                    "<User>HelloService</User>" +
                                    "<Text>Hello ${user}</Text>" +
                                "</HelloResponse>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));
    }
}
