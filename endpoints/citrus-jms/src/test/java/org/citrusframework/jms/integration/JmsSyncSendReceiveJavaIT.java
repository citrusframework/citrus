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

package org.citrusframework.jms.integration;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 */
@Test
public class JmsSyncSendReceiveJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void JmsSyncSendReceiveJavaIT() {
        variable("correlationId", "citrus:randomNumber(10)");
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");

        given(echo("Test 1: Send JMS request and receive sync JMS response (inline CDATA payload)"));

        when(send("helloServiceJmsSyncEndpoint")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello Citrus</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        then(receive("helloServiceJmsSyncEndpoint")
            .message()
            .body("<HelloResponse xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                            "<MessageId>${messageId}</MessageId>" +
                            "<CorrelationId>${correlationId}</CorrelationId>" +
                            "<User>HelloService</User>" +
                            "<Text>Hello ${user}</Text>" +
                        "</HelloResponse>")
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}"));

        given(echo("Test 2: Send JMS request and receive sync JMS response (file resource payload)"));

        when(send("helloServiceJmsSyncEndpoint")
            .message()
            .body(Resources.fromClasspath("org/citrusframework/jms/integration/helloRequest.xml"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        then(receive("helloServiceJmsSyncEndpoint")
            .message()
            .body(Resources.fromClasspath("org/citrusframework/jms/integration/helloResponse.xml"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));
    }
}
