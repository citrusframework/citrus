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

package com.consol.citrus.javadsl.design;

import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.annotations.CitrusTest;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class ValidateXMLDataJavaITest extends TestNGCitrusTestDesigner {
    
    @CitrusTest
    public void validateXMLData() {
        variable("correlationId", "citrus:randomNumber(10)");      
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");
        
        send("helloRequestSender")
            .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello TestFramework</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
        
        receive("helloResponseReceiver")
            .payload("<HelloResponse xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>@equalsIgnoreCase('HelloService')@</User>" +
                           "<Text>xxx</Text>" +
                       "</HelloResponse>")
            .ignore("HelloResponse.Text")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
        
        send("helloRequestSender")
            .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello TestFramework</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
        
        receive("helloResponseReceiver")
            .payload("<ns0:HelloResponse xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                           "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                           "<ns0:User>@equalsIgnoreCase('HelloService')@</ns0:User>" +
                           "<ns0:Text>xxx</ns0:Text>" +
                       "</ns0:HelloResponse>")
            .ignore("//ns0:HelloResponse/ns0:Text")
            .namespace("ns0", "http://www.consol.de/schemas/samples/sayHello.xsd")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
        
        send("helloRequestSender")
            .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello TestFramework</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
        
        receive("helloResponseReceiver")
            .xsd("helloSchema")
            .payload("<ns0:HelloResponse xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                           "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                           "<ns0:User>@equalsIgnoreCase('HelloService')@</ns0:User>" +
                           "<ns0:Text>@ignore@</ns0:Text>" +
                       "</ns0:HelloResponse>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
        
        send("helloRequestSender")
            .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello TestFramework</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
        
        receive("helloResponseReceiver")
            .xsdSchemaRepository("helloSchemaRepository")
            .payload("<ns0:HelloResponse xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                           "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                           "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                           "<ns0:User>@equalsIgnoreCase('HelloService')@</ns0:User>" +
                           "<ns0:Text>@ignore@</ns0:Text>" +
                       "</ns0:HelloResponse>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
    }
}