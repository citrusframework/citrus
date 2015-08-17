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
import com.consol.citrus.dsl.builder.*;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class ValidateXpathTestRunnerITest extends TestNGCitrusTestRunner {
    
    @Autowired
    private NamespaceContextBuilder namespaceContextBuilder;
    
    @CitrusTest
    public void validateXpath() {
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
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}");
            }
        });
        
        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("helloResponseReceiver")
                        .validate("//ns0:HelloResponse/ns0:MessageId", "${messageId}")
                        .validate("//ns0:HelloResponse/ns0:CorrelationId", "${correlationId}")
                        .validate("//ns0:HelloResponse/ns0:Text", "citrus:concat('Hello ', ${user})")
                        .namespace("ns0", "http://www.consol.de/schemas/samples/sayHello.xsd")
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}");
            }
        });
        
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
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}");
            }
        });
        
        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("helloResponseReceiver")
                        .validate("//ns1:HelloResponse/ns1:MessageId", "${messageId}")
                        .validate("//ns1:HelloResponse/ns1:CorrelationId", "${correlationId}")
                        .validate("//ns1:HelloResponse/ns1:Text", "citrus:concat('Hello ', ${user})")
                        .namespace("ns1", "http://www.consol.de/schemas/samples/sayHello.xsd")
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}");
            }
        });
        
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
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}");
            }
        });
        
        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("helloResponseReceiver")
                        .validate("//:HelloResponse/:MessageId", "${messageId}")
                        .validate("//:HelloResponse/:CorrelationId", "${correlationId}")
                        .validate("//:HelloResponse/:Text", "citrus:concat('Hello ', ${user})")
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}");
            }
        });
        
        echo("Now using xpath validation elements");
        
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
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}");
            }
        });
        
        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("helloResponseReceiver")
                        .xpath("//ns0:HelloResponse/ns0:MessageId", "${messageId}")
                        .xpath("//ns0:HelloResponse/ns0:CorrelationId", "${correlationId}")
                        .xpath("//ns0:HelloResponse/ns0:Text", "citrus:concat('Hello ', ${user})")
                        .namespace("ns0", "http://www.consol.de/schemas/samples/sayHello.xsd")
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}");
            }
        });
        
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
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}");
            }
        });
        
        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("helloResponseReceiver")
                        .xpath("//ns1:HelloResponse/ns1:MessageId", "${messageId}")
                        .xpath("//ns1:HelloResponse/ns1:CorrelationId", "${correlationId}")
                        .xpath("//ns1:HelloResponse/ns1:Text", "citrus:concat('Hello ', ${user})")
                        .namespace("ns1", "http://www.consol.de/schemas/samples/sayHello.xsd")
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}");
            }
        });
        
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
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}");
            }
        });
        
        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("helloResponseReceiver")
                        .xpath("//:HelloResponse/:MessageId", "${messageId}")
                        .xpath("//:HelloResponse/:CorrelationId", "${correlationId}")
                        .xpath("//:HelloResponse/:Text", "citrus:concat('Hello ', ${user})")
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}");
            }
        });
        
        echo("Test: Default namespace mapping");
        
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
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}");
            }
        });
        
        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("helloResponseReceiver")
                        .validate("//def:HelloResponse/def:MessageId", "${messageId}")
                        .validate("//def:HelloResponse/def:CorrelationId", "${correlationId}")
                        .validate("//def:HelloResponse/def:Text", "citrus:concat('Hello ', ${user})")
                        .namespaces(namespaceContextBuilder.getNamespaceMappings())
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}")
                        .extractFromPayload("/def:HelloResponse/def:Text", "extractedText");
            }
        });
        
        groovy(new BuilderSupport<GroovyActionBuilder>() {
            @Override
            public void configure(GroovyActionBuilder builder) {
                builder.script("assert context.getVariable('extractedText') == 'Hello ${user}'");
            }
        });
    }
}