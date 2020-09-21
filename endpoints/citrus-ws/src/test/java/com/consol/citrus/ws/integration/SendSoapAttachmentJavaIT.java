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
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

import static com.consol.citrus.container.Parallel.Builder.parallel;
import static com.consol.citrus.container.Sequence.Builder.sequential;
import static com.consol.citrus.validation.xml.XmlMessageValidationContext.Builder.xml;
import static com.consol.citrus.variable.MessageHeaderVariableExtractor.Builder.headerValueExtractor;
import static com.consol.citrus.ws.actions.SoapActionBuilder.soap;

/**
 * @author Christoph Deppisch
 */
@Test
public class SendSoapAttachmentJavaIT extends TestNGCitrusSupport {

    @CitrusTest
    public void sendSoapAttachment() {
        given(parallel().actions(
                soap().client("helloSoapClient")
                        .send()
                        .payload("<ns0:SoapMessageWithAttachmentRequest xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                                    "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                "</ns0:SoapMessageWithAttachmentRequest>")
                        .attachment("MySoapAttachment", "text/plain", new ClassPathResource("com/consol/citrus/ws/soapAttachment.txt")),
                sequential().actions(
                        soap().server("soapRequestEndpoint")
                                .receive()
                                .payload("<ns0:SoapMessageWithAttachmentRequest xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                                            "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                        "</ns0:SoapMessageWithAttachmentRequest>")
                                .validate(xml().schemaValidation(false))
                                .extract(headerValueExtractor()
                                            .header("citrus_jms_messageId", "internal_correlation_id"))
                                .attachment("MySoapAttachment", "text/plain", new ClassPathResource("com/consol/citrus/ws/soapAttachment.txt"))
                                .timeout(5000L),
                        soap().server("soapResponseEndpoint")
                                .send()
                                .payload("<ns0:SoapMessageWithAttachmentResponse xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                                            "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                            "<ns0:Success>true</ns0:Success>" +
                                        "</ns0:SoapMessageWithAttachmentResponse>")
                                .header("citrus_jms_correlationId", "${internal_correlation_id}")
                )
        ));

        then(soap().client("helloSoapClient")
                .receive()
                .payload("<ns0:SoapMessageWithAttachmentResponse xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                            "<ns0:Operation>Read the attachment</ns0:Operation>" +
                            "<ns0:Success>true</ns0:Success>" +
                        "</ns0:SoapMessageWithAttachmentResponse>")
                .validate(xml().schemaValidation(false)));

        given(parallel().actions(
                soap().client("helloSoapClient")
                        .send()
                        .payload("<ns0:SoapMessageWithAttachmentRequest xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                                    "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                "</ns0:SoapMessageWithAttachmentRequest>")
                        .attachment("MySoapAttachment", "text/plain", "This is an attachment!"),
                sequential().actions(
                        soap().server("soapRequestEndpoint")
                                .receive()
                                .payload("<ns0:SoapMessageWithAttachmentRequest xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                                            "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                        "</ns0:SoapMessageWithAttachmentRequest>")
                                .validate(xml().schemaValidation(false))
                                .extract(headerValueExtractor()
                                            .header("citrus_jms_messageId", "internal_correlation_id"))
                                .attachment("MySoapAttachment", "text/plain", "This is an attachment!")
                                .timeout(5000L),
                        soap().server("soapResponseEndpoint")
                                .send()
                                .payload("<ns0:SoapMessageWithAttachmentResponse xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                                            "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                            "<ns0:Success>true</ns0:Success>" +
                                        "</ns0:SoapMessageWithAttachmentResponse>")
                                .header("citrus_jms_correlationId", "${internal_correlation_id}")
                )
        ));

        then(soap().client("helloSoapClient")
                .receive()
                .payload("<ns0:SoapMessageWithAttachmentResponse xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                        "<ns0:Operation>Read the attachment</ns0:Operation>" +
                        "<ns0:Success>true</ns0:Success>" +
                        "</ns0:SoapMessageWithAttachmentResponse>")
                .validate(xml().schemaValidation(false)));
    }
}
