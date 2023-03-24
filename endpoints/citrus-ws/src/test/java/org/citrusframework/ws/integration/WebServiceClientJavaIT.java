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

package org.citrusframework.ws.integration;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.citrusframework.dsl.XmlSupport.xml;

/**
 * @author Christoph Deppisch
 */
@Test
public class WebServiceClientJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void soapClient() {
        variable("messageId", "123456789");
        variable("correlationId", "CORR123456789");

        when(send("helloSoapClient")
            .message()
            .body("<ns0:HelloStandaloneRequest xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                            "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                            "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                            "<ns0:User>User</ns0:User>" +
                            "<ns0:Text>Hello WebServer</ns0:Text>" +
                        "</ns0:HelloStandaloneRequest>")
            .header("{http://citrusframework.org/schemas/samples/HelloService.xsd}ns0:Request", "HelloRequest")
            .header("{http://citrusframework.org/schemas/samples/HelloService.xsd}ns0:Operation", "sayHello"));

        then(receive("helloSoapClient")
            .message()
            .body("<ns0:HelloStandaloneResponse xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                            "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                            "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                            "<ns0:User>WebServer</ns0:User>" +
                            "<ns0:Text>Hello User</ns0:Text>" +
                        "</ns0:HelloStandaloneResponse>")
            .header("Request", "HelloRequest")
            .header("Operation", "sayHelloResponse")
            .validate(xml().schemaValidation(false)));
    }
}
