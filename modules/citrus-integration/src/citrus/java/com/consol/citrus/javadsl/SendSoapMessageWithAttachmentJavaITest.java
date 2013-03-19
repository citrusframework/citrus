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

package com.consol.citrus.javadsl;

import org.springframework.core.io.ClassPathResource;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;

/**
 * @author Christoph Deppisch
 */
public class SendSoapMessageWithAttachmentJavaITest extends TestNGCitrusTestBuilder {
    
    @Override
    public void configure() {
        variable("soapFaultCode", "TEC-1001");
        variable("soapFaultString", "Invalid request");
        
        parallel(
            send("webServiceRequestSender")
                .soap()
                .payload("<ns0:SoapMessageWithAttachmentRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sample.xsd\">" +
                                        "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                    "</ns0:SoapMessageWithAttachmentRequest>")
                .attatchment("MySoapAttachment", "text/plain", new ClassPathResource("com/consol/citrus/ws/soapAttachment.txt")),
            sequential(
                receive("webServiceRequestReceiver")
                    .payload("<ns0:SoapMessageWithAttachmentRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sample.xsd\">" +
                                    "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                "</ns0:SoapMessageWithAttachmentRequest>")
                    .schemaValidation(false)
                    .extractFromHeader("jms_messageId", "internal_correlation_id")
                    .soap()
                    .attatchment("MySoapAttachment", "text/plain", new ClassPathResource("com/consol/citrus/ws/soapAttachment.txt"))
                    .timeout(5000L),
                send("webServiceResponseSender")
                    .payload("<ns0:SoapMessageWithAttachmentResponse xmlns:ns0=\"http://www.consol.de/schemas/samples/sample.xsd\">" +
                                    "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                    "<ns0:Success>true</ns0:Success>" +
                                "</ns0:SoapMessageWithAttachmentResponse>")
                    .header("jms_correlationId", "${internal_correlation_id}")
            )
        );
        
        receive("webServiceReplyHandler")
            .payload("<ns0:SoapMessageWithAttachmentResponse xmlns:ns0=\"http://www.consol.de/schemas/samples/sample.xsd\">" +
                            "<ns0:Operation>Read the attachment</ns0:Operation>" +
                            "<ns0:Success>true</ns0:Success>" +
                        "</ns0:SoapMessageWithAttachmentResponse>")
            .schemaValidation(false);
        
        parallel(
            send("webServiceRequestSender")
                .soap()
                .payload("<ns0:SoapMessageWithAttachmentRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sample.xsd\">" +
                                        "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                    "</ns0:SoapMessageWithAttachmentRequest>")
                .attatchment("MySoapAttachment", "text/plain", "This is an attachment!"),
            sequential(
                receive("webServiceRequestReceiver")
                    .payload("<ns0:SoapMessageWithAttachmentRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sample.xsd\">" +
                                    "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                "</ns0:SoapMessageWithAttachmentRequest>")
                    .schemaValidation(false)
                    .extractFromHeader("jms_messageId", "internal_correlation_id")
                    .soap()
                    .attatchment("MySoapAttachment", "text/plain", "This is an attachment!")
                    .timeout(5000L),
                send("webServiceResponseSender")
                    .payload("<ns0:SoapMessageWithAttachmentResponse xmlns:ns0=\"http://www.consol.de/schemas/samples/sample.xsd\">" +
                                    "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                    "<ns0:Success>true</ns0:Success>" +
                                "</ns0:SoapMessageWithAttachmentResponse>")
                    .header("jms_correlationId", "${internal_correlation_id}")
            )
        );
        
        receive("webServiceReplyHandler")
            .payload("<ns0:SoapMessageWithAttachmentResponse xmlns:ns0=\"http://www.consol.de/schemas/samples/sample.xsd\">" +
                            "<ns0:Operation>Read the attachment</ns0:Operation>" +
                            "<ns0:Success>true</ns0:Success>" +
                        "</ns0:SoapMessageWithAttachmentResponse>")
            .schemaValidation(false);
    }
    
    @Test
    public void echoActionITest(ITestContext testContext) {
        executeTest(testContext);
    }
}