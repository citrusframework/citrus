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
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class SyncJmsTopicTestRunnerIT extends TestNGCitrusTestRunner {

    @CitrusTest
    public void syncJmsTopic() {
        variable("correlationId", "citrus:randomNumber(10)");
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");

        parallel().actions(
            sequential().actions(
                sleep(1000L),
                send(builder -> builder.endpoint("syncJmsTopicEndpoint")
                        .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                "<MessageId>${messageId}</MessageId>" +
                                "<CorrelationId>${correlationId}</CorrelationId>" +
                                "<User>${user}</User>" +
                                "<Text>Hello TestFramework</Text>" +
                                "</HelloRequest>")
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}"))
            ),
            sequential().actions(
                parallel().actions(
                    receive(builder -> builder.endpoint("syncJmsTopicSubscriberEndpoint")
                            .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                    "<MessageId>${messageId}</MessageId>" +
                                    "<CorrelationId>${correlationId}</CorrelationId>" +
                                    "<User>${user}</User>" +
                                    "<Text>Hello TestFramework</Text>" +
                                    "</HelloRequest>")
                            .header("Operation", "sayHello")
                            .header("CorrelationId", "${correlationId}")),
                    receive(builder -> builder.endpoint("syncJmsTopicSubscriberEndpoint")
                            .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                    "<MessageId>${messageId}</MessageId>" +
                                    "<CorrelationId>${correlationId}</CorrelationId>" +
                                    "<User>${user}</User>" +
                                    "<Text>Hello TestFramework</Text>" +
                                    "</HelloRequest>")
                            .header("Operation", "sayHello")
                            .header("CorrelationId", "${correlationId}"))
                ),
                send(builder -> builder.endpoint("syncJmsTopicSubscriberEndpoint")
                        .payload("<HelloResponse xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                "<MessageId>${messageId}</MessageId>" +
                                "<CorrelationId>${correlationId}</CorrelationId>" +
                                "<User>HelloService</User>" +
                                "<Text>Hello ${user}</Text>" +
                                "</HelloResponse>")
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}"))
            )
        );

        receive(builder -> builder.endpoint("syncJmsTopicEndpoint")
                .payload("<HelloResponse xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                        "<MessageId>${messageId}</MessageId>" +
                        "<CorrelationId>${correlationId}</CorrelationId>" +
                        "<User>HelloService</User>" +
                        "<Text>Hello ${user}</Text>" +
                        "</HelloResponse>")
                .header("Operation", "sayHello")
                .header("CorrelationId", "${correlationId}"));
    }
}
