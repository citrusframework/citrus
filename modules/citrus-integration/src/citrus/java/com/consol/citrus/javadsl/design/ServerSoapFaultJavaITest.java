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
import com.consol.citrus.ws.validation.SoapFaultValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class ServerSoapFaultJavaITest extends TestNGCitrusTestDesigner {
    
    @Autowired
    @Qualifier("xmlSoapFaultValidator")
    private SoapFaultValidator soapFaultValidator;
    
    @CitrusTest
    public void soapFaultAction() {
        variable("correlationId", "citrus:randomNumber(10)");      
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");
        
        parallel(
            assertSoapFault(
                send("webServiceClient")
                    .payload("<ns0:HelloRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                  "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                  "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                  "<ns0:User>${user}</ns0:User>" +
                                  "<ns0:Text>Hello WebServer</ns0:Text>" +
                              "</ns0:HelloRequest>")
            ).faultCode("{http://www.citrusframework.org/faults}TEC-1000")
            .faultString("Invalid request"),
            sequential(
                receive("webServiceRequestReceiver")
                    .payload("<ns0:HelloRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                  "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                  "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                  "<ns0:User>${user}</ns0:User>" +
                                  "<ns0:Text>Hello WebServer</ns0:Text>" +
                              "</ns0:HelloRequest>")
                    .schemaValidation(false)
                    .extractFromHeader("citrus_jms_messageId", "internal_correlation_id"),
                sendSoapFault("webServiceResponseSender")
                    .faultCode("{http://www.citrusframework.org/faults}citrus-ns:TEC-1000")
                    .faultString("Invalid request")
                    .faultDetail("<ns0:FaultDetail xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                    "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                    "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                    "<ns0:ErrorCode>TEC-1000</ns0:ErrorCode>" +
                                    "<ns0:Text>Invalid request</ns0:Text>" +
                                "</ns0:FaultDetail>")
                    .header("citrus_jms_correlationId", "${internal_correlation_id}")
            )
        );
        
        echo("Test Soap fault actor support");
        
        parallel(
            assertSoapFault(
                send("webServiceClient")
                    .payload("<ns0:HelloRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                  "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                  "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                  "<ns0:User>${user}</ns0:User>" +
                                  "<ns0:Text>Hello WebServer</ns0:Text>" +
                              "</ns0:HelloRequest>")
            ).faultCode("{http://www.citrusframework.org/faults}TEC-1000")
            .faultString("Invalid request")
            .faultActor("SERVER"),
            sequential(
                receive("webServiceRequestReceiver")
                    .payload("<ns0:HelloRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                  "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                  "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                  "<ns0:User>${user}</ns0:User>" +
                                  "<ns0:Text>Hello WebServer</ns0:Text>" +
                              "</ns0:HelloRequest>")
                    .schemaValidation(false)
                    .extractFromHeader("citrus_jms_messageId", "internal_correlation_id"),
                sendSoapFault("webServiceResponseSender")
                    .faultCode("{http://www.citrusframework.org/faults}citrus-ns:TEC-1000")
                    .faultString("Invalid request")
                    .faultActor("SERVER")
                    .faultDetail("<ns0:FaultDetail xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                    "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                    "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                    "<ns0:ErrorCode>TEC-1000</ns0:ErrorCode>" +
                                    "<ns0:Text>Invalid request</ns0:Text>" +
                                "</ns0:FaultDetail>")
                    .header("citrus_jms_correlationId", "${internal_correlation_id}")
            )
        );
        
        echo("Test XML Soap fault validation");
        
        parallel(
            assertSoapFault(
                send("webServiceClient")
                    .payload("<ns0:HelloRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                  "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                  "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                  "<ns0:User>${user}</ns0:User>" +
                                  "<ns0:Text>Hello WebServer</ns0:Text>" +
                              "</ns0:HelloRequest>")
            ).faultCode("{http://www.citrusframework.org/faults}TEC-1000")
            .faultString("Invalid request")
            .faultDetail("<ns0:FaultDetail xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                            "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                            "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                            "<ns0:ErrorCode>TEC-1000</ns0:ErrorCode>" +
                            "<ns0:Text>Invalid request</ns0:Text>" +
                        "</ns0:FaultDetail>")
            .validator(soapFaultValidator),
            sequential(
                receive("webServiceRequestReceiver")
                    .payload("<ns0:HelloRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                  "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                  "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                  "<ns0:User>${user}</ns0:User>" +
                                  "<ns0:Text>Hello WebServer</ns0:Text>" +
                              "</ns0:HelloRequest>")
                    .schemaValidation(false)
                    .extractFromHeader("citrus_jms_messageId", "internal_correlation_id"),
                sendSoapFault("webServiceResponseSender")
                    .faultCode("{http://www.citrusframework.org/faults}citrus-ns:TEC-1000")
                    .faultString("Invalid request")
                    .faultDetail("<ns0:FaultDetail xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                    "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                    "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                    "<ns0:ErrorCode>TEC-1000</ns0:ErrorCode>" +
                                    "<ns0:Text>Invalid request</ns0:Text>" +
                                "</ns0:FaultDetail>")
                    .header("citrus_jms_correlationId", "${internal_correlation_id}")
            )
        );
        
        echo("Test XML schema validation skip");
        
        parallel(
            assertSoapFault(
                send("webServiceClient")
                    .payload("<ns0:HelloRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                  "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                  "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                  "<ns0:User>${user}</ns0:User>" +
                                  "<ns0:Text>Hello WebServer</ns0:Text>" +
                              "</ns0:HelloRequest>")
            ).faultCode("{http://www.citrusframework.org/faults}TEC-1000")
            .faultString("Invalid request")
            .faultDetail("<ns0:FaultDetail xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                            "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                            "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                            "<ns0:ErrorCode>TEC-1000</ns0:ErrorCode>" +
                            "<ns0:Text>Invalid request</ns0:Text>" +
                        "</ns0:FaultDetail>")
            .schemaValidation(false),
            sequential(
                receive("webServiceRequestReceiver")
                    .payload("<ns0:HelloRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                  "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                  "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                  "<ns0:User>${user}</ns0:User>" +
                                  "<ns0:Text>Hello WebServer</ns0:Text>" +
                              "</ns0:HelloRequest>")
                    .schemaValidation(false)
                    .extractFromHeader("citrus_jms_messageId", "internal_correlation_id"),
                sendSoapFault("webServiceResponseSender")
                    .faultCode("{http://www.citrusframework.org/faults}citrus-ns:TEC-1000")
                    .faultString("Invalid request")
                    .faultDetail("<ns0:FaultDetail xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                    "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                    "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                    "<ns0:ErrorCode>TEC-1000</ns0:ErrorCode>" +
                                    "<ns0:Text>Invalid request</ns0:Text>" +
                                "</ns0:FaultDetail>")
                    .header("citrus_jms_correlationId", "${internal_correlation_id}")
            )
        );
        
        echo("Test explicit XML schema repository");
        
        parallel(
            assertSoapFault(
                send("webServiceClient")
                    .payload("<ns0:HelloRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                  "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                  "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                  "<ns0:User>${user}</ns0:User>" +
                                  "<ns0:Text>Hello WebServer</ns0:Text>" +
                              "</ns0:HelloRequest>")
            ).faultCode("{http://www.citrusframework.org/faults}TEC-1000")
            .faultString("Invalid request")
            .faultDetail("<ns0:FaultDetail xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHelloExtended.xsd\">" +
                            "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                            "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                            "<ns0:ErrorCode>TEC-1000</ns0:ErrorCode>" +
                            "<ns0:Text>Invalid request</ns0:Text>" +
                            "<ns0:Reason>Client</ns0:Reason>" +
                        "</ns0:FaultDetail>")
            .validator(soapFaultValidator)
            .xsdSchemaRepository("helloSchemaRepository"),
            sequential(
                receive("webServiceRequestReceiver")
                    .payload("<ns0:HelloRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                  "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                  "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                  "<ns0:User>${user}</ns0:User>" +
                                  "<ns0:Text>Hello WebServer</ns0:Text>" +
                              "</ns0:HelloRequest>")
                    .schemaValidation(false)
                    .extractFromHeader("citrus_jms_messageId", "internal_correlation_id"),
                sendSoapFault("webServiceResponseSender")
                    .faultCode("{http://www.citrusframework.org/faults}citrus-ns:TEC-1000")
                    .faultString("Invalid request")
                    .faultDetail("<ns0:FaultDetail xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHelloExtended.xsd\">" +
                                    "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                    "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                    "<ns0:ErrorCode>TEC-1000</ns0:ErrorCode>" +
                                    "<ns0:Text>Invalid request</ns0:Text>" +
                                    "<ns0:Reason>Client</ns0:Reason>" +
                                "</ns0:FaultDetail>")
                    .header("citrus_jms_correlationId", "${internal_correlation_id}")
            )
        );
        
        echo("Test explicit XML schema instance");
        
        parallel(
            assertSoapFault(
                send("webServiceClient")
                    .payload("<ns0:HelloRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                  "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                  "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                  "<ns0:User>${user}</ns0:User>" +
                                  "<ns0:Text>Hello WebServer</ns0:Text>" +
                              "</ns0:HelloRequest>")
            ).faultCode("{http://www.citrusframework.org/faults}TEC-1000")
            .faultString("Invalid request")
            .faultDetail("<ns0:FaultDetail xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHelloExtended.xsd\">" +
                            "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                            "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                            "<ns0:ErrorCode>TEC-1000</ns0:ErrorCode>" +
                            "<ns0:Text>Invalid request</ns0:Text>" +
                            "<ns0:Reason>Client</ns0:Reason>" +
                        "</ns0:FaultDetail>")
            .validator(soapFaultValidator)
            .xsd("helloSchemaExtended"),
            sequential(
                receive("webServiceRequestReceiver")
                    .payload("<ns0:HelloRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                  "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                  "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                  "<ns0:User>${user}</ns0:User>" +
                                  "<ns0:Text>Hello WebServer</ns0:Text>" +
                              "</ns0:HelloRequest>")
                    .schemaValidation(false)
                    .extractFromHeader("citrus_jms_messageId", "internal_correlation_id"),
                sendSoapFault("webServiceResponseSender")
                    .faultCode("{http://www.citrusframework.org/faults}citrus-ns:TEC-1000")
                    .faultString("Invalid request")
                    .faultDetail("<ns0:FaultDetail xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHelloExtended.xsd\">" +
                                    "<ns0:MessageId>${messageId}</ns0:MessageId>" +
                                    "<ns0:CorrelationId>${correlationId}</ns0:CorrelationId>" +
                                    "<ns0:ErrorCode>TEC-1000</ns0:ErrorCode>" +
                                    "<ns0:Text>Invalid request</ns0:Text>" +
                                    "<ns0:Reason>Client</ns0:Reason>" +
                                "</ns0:FaultDetail>")
                    .header("citrus_jms_correlationId", "${internal_correlation_id}")
            )
        );
        
        echo("Test XML multiple soap fault detail elements validation");
        
        //TODO code test
    }
}