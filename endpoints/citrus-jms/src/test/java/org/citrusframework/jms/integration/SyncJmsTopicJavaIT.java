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

package org.citrusframework.jms.integration;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.container.Parallel.Builder.parallel;
import static org.citrusframework.container.Sequence.Builder.sequential;

/**
 * @author Christoph Deppisch
 */
@Test
public class SyncJmsTopicJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void syncJmsTopic() {
        variable("correlationId", "citrus:randomNumber(10)");
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");

        given(parallel().actions(
            sequential().actions(
                sleep().milliseconds(2000L),
                send("syncJmsTopicEndpoint")
                    .message()
                    .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                                   "<MessageId>${messageId}</MessageId>" +
                                   "<CorrelationId>${correlationId}</CorrelationId>" +
                                   "<User>${user}</User>" +
                                   "<Text>Hello TestFramework</Text>" +
                               "</HelloRequest>")
                    .header("Operation", "sayHello")
                    .header("CorrelationId", "${correlationId}")
            ),
            sequential().actions(
                parallel().actions(
                    receive("syncJmsTopicSubscriberEndpoint")
                        .message()
                        .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                                   "<MessageId>${messageId}</MessageId>" +
                                   "<CorrelationId>${correlationId}</CorrelationId>" +
                                   "<User>${user}</User>" +
                                   "<Text>Hello TestFramework</Text>" +
                               "</HelloRequest>")
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}"),
                    sequential().actions(
                        sleep().milliseconds(500L),
                        receive("syncJmsTopicSubscriberEndpoint")
                            .message()
                            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                                       "<MessageId>${messageId}</MessageId>" +
                                       "<CorrelationId>${correlationId}</CorrelationId>" +
                                       "<User>${user}</User>" +
                                       "<Text>Hello TestFramework</Text>" +
                                   "</HelloRequest>")
                            .header("Operation", "sayHello")
                            .header("CorrelationId", "${correlationId}")
                    )
                ),
                send("syncJmsTopicSubscriberEndpoint")
                    .message()
                    .body("<HelloResponse xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                                    "<MessageId>${messageId}</MessageId>" +
                                    "<CorrelationId>${correlationId}</CorrelationId>" +
                                    "<User>HelloService</User>" +
                                    "<Text>Hello ${user}</Text>" +
                                "</HelloResponse>")
                    .header("Operation", "sayHello")
                    .header("CorrelationId", "${correlationId}")
            )
        ));

        then(receive("syncJmsTopicEndpoint")
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
