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
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class WebServiceClientTestRunnerIT extends TestNGCitrusTestRunner {
    
    @CitrusTest
    public void soapClient() {
        variable("messageId", "123456789");
        variable("correlationId", "CORR123456789");
        
        send(builder -> builder.endpoint("webServiceHelloClient")
                .payload("<ns0:HelloStandaloneRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                        "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                        "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                        "<ns0:User>User</ns0:User>" +
                        "<ns0:Text>Hello WebServer</ns0:Text>" +
                        "</ns0:HelloStandaloneRequest>")
                .header("{http://www.consol.de/schemas/samples/sayHello.xsd}ns0:Request", "HelloRequest")
                .header("{http://www.consol.de/schemas/samples/sayHello.xsd}ns0:Operation", "sayHello"));
        
        receive(builder -> builder.endpoint("webServiceHelloClient")
                .payload("<ns0:HelloStandaloneResponse xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                        "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                        "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                        "<ns0:User>WebServer</ns0:User>" +
                        "<ns0:Text>Hello User</ns0:Text>" +
                        "</ns0:HelloStandaloneResponse>")
                .header("Request", "HelloRequest")
                .header("Operation", "sayHelloResponse")
                .schemaValidation(false));
    }
}