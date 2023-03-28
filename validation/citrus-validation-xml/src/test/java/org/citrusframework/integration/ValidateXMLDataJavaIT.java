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

package org.citrusframework.integration;

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
public class ValidateXMLDataJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void validateXMLData() {
        variable("correlationId", "citrus:randomNumber(10)");
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");

        $(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(receive("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>xxx</Text>" +
                       "</HelloRequest>")
            .validate(xml()
                        .ignore("HelloRequest.Text"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(receive("direct:hello")
            .message()
            .body("<ns0:HelloRequest xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                           "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                           "<ns0:User>${user}</ns0:User>" +
                           "<ns0:Text>xxx</ns0:Text>" +
                       "</ns0:HelloRequest>")
            .validate(xml()
                    .namespaceContext("ns", "http://citrusframework.org/schemas/samples/HelloService.xsd")
                    .ignore("//ns:HelloRequest/ns:Text"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(receive("direct:hello")
            .message()
            .body("<ns0:HelloRequest xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                           "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                           "<ns0:User>@equalsIgnoreCase('christoph')@</ns0:User>" +
                           "<ns0:Text>@ignore@</ns0:Text>" +
                       "</ns0:HelloRequest>")
            .validate(xml()
                        .schema("helloSchema"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(send("direct:hello")
            .message()
            .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<MessageId>${messageId}</MessageId>" +
                           "<CorrelationId>${correlationId}</CorrelationId>" +
                           "<User>${user}</User>" +
                           "<Text>Hello ${user}</Text>" +
                       "</HelloRequest>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(receive("direct:hello")
            .message()
            .body("<ns0:HelloRequest xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                           "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                           "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                           "<ns0:User>${user}</ns0:User>" +
                           "<ns0:Text>@ignore@</ns0:Text>" +
                       "</ns0:HelloRequest>")
            .validate(xml()
                    .schemaRepository("helloSchemaRepository"))
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));
    }
}
