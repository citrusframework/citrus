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

package com.consol.citrus.jms.integration;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.testng.TestNGCitrusSupport;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 */
@Test
public class SyncJmsQueueJavaIT extends TestNGCitrusSupport {

    @CitrusTest
    public void syncJmsQueue() {
        variable("correlationId", "citrus:randomNumber(10)");
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");

        when(send("helloServiceJmsSyncEndpoint")
            .payload("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello TestFramework</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}")
            .description("Send synchronous hello request: TestFramework -> HelloService"));

        then(receive("helloServiceJmsSyncEndpoint")
            .description("Receive sync hello response: HelloService -> TestFramework")
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
