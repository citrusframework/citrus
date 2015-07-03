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

/**
 * @author Christoph Deppisch
 */
@Test
public class JmsSyncSendReceiveJavaITest extends AbstractJmsTestDesigner {
    
    @CitrusTest
    public void JmsSyncSendReceiveJavaITest() {
        variable("correlationId", "citrus:randomNumber(10)");
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");
        
        echo("Test 1: Send JMS request and receive sync JMS response (inline CDATA payload)");
        
        send("helloSyncJmsEndpoint")
            .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello Citrus</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
        
        receive("helloSyncJmsEndpoint")
            .payload("<HelloResponse xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                            "<MessageId>${messageId}</MessageId>" +
                            "<CorrelationId>${correlationId}</CorrelationId>" +
                            "<User>HelloService</User>" +
                            "<Text>Hello ${user}</Text>" +
                        "</HelloResponse>")
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}");
        
        echo("Test 2: Send JMS request and receive sync JMS response (file resource payload)");
        
        send("helloSyncJmsEndpoint")
            .payload(new ClassPathResource("com/consol/citrus/jms/helloRequest.xml"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
        
        receive("helloSyncJmsEndpoint")
            .payload(new ClassPathResource("com/consol/citrus/jms/helloResponse.xml"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
    }
}