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

package com.consol.citrus.jms;

import com.consol.citrus.annotations.CitrusTest;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * @author Christoph Deppisch
 */
@Test
public class JmsSendReceiveJavaIT extends AbstractJmsTestDesigner {
    
    @CitrusTest
    public void JmsSendReceiveJavaIT() {
        variable("correlationId", "citrus:randomNumber(10)");
        variable("correlationIdA", "citrus:randomNumber(10)");
        variable("correlationIdB", "citrus:randomNumber(10)");      
        variable("messageId", "citrus:randomNumber(10)");
        variable("messageIdA", "citrus:randomNumber(10)");
        variable("messageIdB", "citrus:randomNumber(10)");
        variable("user", "Christoph");
        
        echo("Test 1: Send JMS request and receive async JMS response (inline CDATA payload)");
        
        send("helloRequestJmsMessageSender")
            .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello Citrus</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
        
        receive("helloResponseJmsMessageReceiver")
            .payload("<HelloResponse xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                            "<MessageId>${messageId}</MessageId>" +
                            "<CorrelationId>${correlationId}</CorrelationId>" +
                            "<User>HelloService</User>" +
                            "<Text>Hello ${user}</Text>" +
                        "</HelloResponse>")
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}");
        
        echo("Test 2: Send JMS request and receive async JMS response (file resource payload)");
        
        send("helloRequestJmsMessageSender")
            .payload(new ClassPathResource("com/consol/citrus/jms/helloRequest.xml"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
        
        receive("helloResponseJmsMessageReceiver")
            .payload(new ClassPathResource("com/consol/citrus/jms/helloResponse.xml"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
        
        echo("Test 3: Send JMS request and receive async JMS response (JMS message selector)");
        
        send("helloRequestJmsMessageSender")
            .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<MessageId>${messageIdA}</MessageId>" +
                           "<CorrelationId>${correlationIdA}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello Citrus first time</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationIdA}");
        
        send("helloRequestJmsMessageSender")
            .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<MessageId>${messageIdB}</MessageId>" +
                           "<CorrelationId>${correlationIdB}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello Citrus second time</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationIdB}");
        
        receive("helloResponseJmsMessageReceiver")
            .selector(Collections.singletonMap("CorrelationId", "${correlationIdB}"))
            .payload("<HelloResponse xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                            "<MessageId>${messageIdB}</MessageId>" +
                            "<CorrelationId>${correlationIdB}</CorrelationId>" +
                            "<User>HelloService</User>" +
                            "<Text>Hello ${user}</Text>" +
                        "</HelloResponse>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationIdB}")
            .timeout(500);
        
        receive("helloResponseJmsMessageReceiver")
            .selector(Collections.singletonMap("CorrelationId", "${correlationIdA}"))
            .payload("<HelloResponse xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                            "<MessageId>${messageIdA}</MessageId>" +
                            "<CorrelationId>${correlationIdA}</CorrelationId>" +
                            "<User>HelloService</User>" +
                            "<Text>Hello ${user}</Text>" +
                        "</HelloResponse>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationIdA}")
            .timeout(500);
        
        echo("Test 4: Receive JMS message timeout response");
        
        assertException(
            receive("helloResponseJmsMessageReceiver")
                .selector(Collections.singletonMap("CorrelationId", "doesNotExist"))
                .payload("<HelloResponse xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                              "<MessageId>?</MessageId>" +
                              "<CorrelationId>?</CorrelationId>" +
                              "<User>HelloService</User>" +
                              "<Text>Hello ?</Text>" +
                          "</HelloResponse>")
                .header("Operation", "sayHello")
                .timeout(300))
            .exception(com.consol.citrus.exceptions.ActionTimeoutException.class);
    }
}