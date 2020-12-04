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

package com.consol.citrus.ws.integration;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.testng.TestNGCitrusSupport;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.container.Assert.Builder.assertException;
import static com.consol.citrus.container.Parallel.Builder.parallel;
import static com.consol.citrus.container.Sequence.Builder.sequential;
import static com.consol.citrus.dsl.XmlSupport.xml;
import static com.consol.citrus.variable.MessageHeaderVariableExtractor.Builder.headerValueExtractor;

/**
 * @author Christoph Deppisch
 */
@Test
public class SoapHttpErrorJavaIT extends TestNGCitrusSupport {

    @CitrusTest
    public void soapHttpError() {
        variable("correlationId", "citrus:randomNumber(10)");
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");

        run(parallel().actions(
            assertException()
                .exception(org.springframework.ws.client.WebServiceTransportException.class)
                .message("Server Error [500]")
                .when(send("helloSoapClient")
                    .payload("<ns0:HelloRequest xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                                  "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                  "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                  "<ns0:User>${user}</ns0:User>" +
                                  "<ns0:Text>Hello WebServer</ns0:Text>" +
                              "</ns0:HelloRequest>")
                    .header("{http://citrusframework.org/test}Operation", "sayHello")
                    .header("citrus_http_operation", "sayHello")
                    .header("citrus_soap_action", "sayHello")
            ),
            sequential().actions(
                receive("soapRequestEndpoint")
                    .payload("<ns0:HelloRequest xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                                  "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                  "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                  "<ns0:User>${user}</ns0:User>" +
                                  "<ns0:Text>Hello WebServer</ns0:Text>" +
                              "</ns0:HelloRequest>")
                    .header("Operation", "sayHello")
                    .header("operation", "sayHello")
                    .header("citrus_soap_action", "sayHello")
                    .validate(xml().validate().schemaValidation(false))
                    .process(headerValueExtractor()
                                    .header("citrus_jms_messageId", "internal_correlation_id")),
                send("soapResponseEndpoint")
                    .header("citrus_http_status_code", "500")
                    .header("citrus_jms_correlationId", "${internal_correlation_id}")
            )
        ));
    }
}
