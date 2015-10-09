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

package com.consol.citrus.javadsl.runner;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.builder.ReceiveMessageBuilder;
import com.consol.citrus.dsl.builder.SendMessageBuilder;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class JmsCommunicationTestRunnerIT extends TestNGCitrusTestRunner {
    
    @CitrusTest
    public void jmsQueues() {
        final String operation = "sayHello";
        
        variable("correlationId", "citrus:randomNumber(10)");
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");
        
        send(new BuilderSupport<SendMessageBuilder>() {
            @Override
            public void configure(SendMessageBuilder builder) {
                builder.endpoint("helloRequestSender")
                        .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                "<MessageId>${messageId}</MessageId>" +
                                "<CorrelationId>${correlationId}</CorrelationId>" +
                                "<User>${user}</User>" +
                                "<Text>Hello TestFramework</Text>" +
                                "</HelloRequest>")
                        .header("Operation", operation)
                        .header("CorrelationId", "${correlationId}")
                        .description("Send asynchronous hello request: TestFramework -> HelloService");
            }
        });
        
        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("helloResponseReceiver")
                        .payload("<HelloResponse xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                "<MessageId>${messageId}</MessageId>" +
                                "<CorrelationId>${correlationId}</CorrelationId>" +
                                "<User>HelloService</User>" +
                                "<Text>Hello ${user}</Text>" +
                                "</HelloResponse>")
                        .header("Operation", operation)
                        .header("CorrelationId", "${correlationId}")
                        .description("Receive asynchronous hello response: HelloService -> TestFramework");
            }
        });
        
        send(new BuilderSupport<SendMessageBuilder>() {
            @Override
            public void configure(SendMessageBuilder builder) {
                builder.endpoint("helloRequestSender")
                        .payload(new ClassPathResource("com/consol/citrus/actions/helloRequest.xml"))
                        .header("Operation", operation)
                        .header("CorrelationId", "${correlationId}");
            }
        });
        
        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("helloResponseReceiver")
                        .payload(new ClassPathResource("com/consol/citrus/actions/helloResponse.xml"))
                        .header("Operation", operation)
                        .header("CorrelationId", "${correlationId}");
            }
        });
    }

    @CitrusTest
    public void JmsCommunicationEmptyReceiveIT() {
        final String operation = "sayHello";

        variable("correlationId", "citrus:randomNumber(10)");
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");

        send(new BuilderSupport<SendMessageBuilder>() {
            @Override
            public void configure(SendMessageBuilder builder) {
                builder.endpoint("helloRequestSender")
                        .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                "<MessageId>${messageId}</MessageId>" +
                                "<CorrelationId>${correlationId}</CorrelationId>" +
                                "<User>${user}</User>" +
                                "<Text>Hello TestFramework</Text>" +
                                "</HelloRequest>")
                        .header("Operation", operation)
                        .header("CorrelationId", "${correlationId}")
                        .description("Send asynchronous hello request: TestFramework -> HelloService");
            }
        });

        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("helloResponseReceiver")
                        .description("Receive asynchronous hello response: HelloService -> TestFramework");
            }
        });

        send(new BuilderSupport<SendMessageBuilder>() {
            @Override
            public void configure(SendMessageBuilder builder) {
                builder.endpoint("helloRequestSender")
                        .payload(new ClassPathResource("com/consol/citrus/actions/helloRequest.xml"))
                        .header("Operation", operation)
                        .header("CorrelationId", "${correlationId}");
            }
        });

        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("helloResponseReceiver");
            }
        });
    }
}