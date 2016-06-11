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
import com.consol.citrus.dsl.builder.*;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class SendSoapAttachmentTestRunnerIT extends TestNGCitrusTestRunner {

    @CitrusTest
    public void sendSoapAttachment() {
        parallel().actions(
                soap(new BuilderSupport<SoapActionBuilder>() {
                    @Override
                    public void configure(SoapActionBuilder builder) {
                        builder.client("webServiceClient")
                                .send()
                                .payload("<ns0:SoapMessageWithAttachmentRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                            "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                        "</ns0:SoapMessageWithAttachmentRequest>")
                                .attachment("MySoapAttachment", "text/plain", new ClassPathResource("com/consol/citrus/ws/soapAttachment.txt"));
                    }
                }),
                sequential().actions(
                        soap(new BuilderSupport<SoapActionBuilder>() {
                            @Override
                            public void configure(SoapActionBuilder builder) {
                                builder.server("webServiceRequestReceiver")
                                        .receive()
                                        .payload("<ns0:SoapMessageWithAttachmentRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                                    "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                                "</ns0:SoapMessageWithAttachmentRequest>")
                                        .schemaValidation(false)
                                        .extractFromHeader("citrus_jms_messageId", "internal_correlation_id")
                                        .attachment("MySoapAttachment", "text/plain", new ClassPathResource("com/consol/citrus/ws/soapAttachment.txt"))
                                        .timeout(5000L);
                            }
                        }),
                        soap(new BuilderSupport<SoapActionBuilder>() {
                            @Override
                            public void configure(SoapActionBuilder builder) {
                                builder.server("webServiceResponseSender")
                                        .send()
                                        .payload("<ns0:SoapMessageWithAttachmentResponse xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                                    "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                                    "<ns0:Success>true</ns0:Success>" +
                                                "</ns0:SoapMessageWithAttachmentResponse>")
                                        .header("citrus_jms_correlationId", "${internal_correlation_id}");
                            }
                        })
                )
        );

        soap(new BuilderSupport<SoapActionBuilder>() {
            @Override
            public void configure(SoapActionBuilder builder) {
                builder.client("webServiceClient")
                        .receive()
                        .payload("<ns0:SoapMessageWithAttachmentResponse xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                    "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                    "<ns0:Success>true</ns0:Success>" +
                                "</ns0:SoapMessageWithAttachmentResponse>")
                        .schemaValidation(false);
            }
        });

        parallel().actions(
                soap(new BuilderSupport<SoapActionBuilder>() {
                    @Override
                    public void configure(SoapActionBuilder builder) {
                        builder.client("webServiceClient")
                                .send()
                                .payload("<ns0:SoapMessageWithAttachmentRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                        "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                        "</ns0:SoapMessageWithAttachmentRequest>")
                                .attachment("MySoapAttachment", "text/plain", "This is an attachment!");
                    }
                }),
                sequential().actions(
                        soap(new BuilderSupport<SoapActionBuilder>() {
                            @Override
                            public void configure(SoapActionBuilder builder) {
                                builder.server("webServiceRequestReceiver")
                                        .receive()
                                        .payload("<ns0:SoapMessageWithAttachmentRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                                "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                                "</ns0:SoapMessageWithAttachmentRequest>")
                                        .schemaValidation(false)
                                        .extractFromHeader("citrus_jms_messageId", "internal_correlation_id")
                                        .attachment("MySoapAttachment", "text/plain", "This is an attachment!")
                                        .timeout(5000L);
                            }
                        }),
                        soap(new BuilderSupport<SoapActionBuilder>() {
                            @Override
                            public void configure(SoapActionBuilder builder) {
                                builder.server("webServiceResponseSender")
                                        .send()
                                        .payload("<ns0:SoapMessageWithAttachmentResponse xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                                "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                                "<ns0:Success>true</ns0:Success>" +
                                                "</ns0:SoapMessageWithAttachmentResponse>")
                                        .header("citrus_jms_correlationId", "${internal_correlation_id}");
                            }
                        })
                )
        );

        soap(new BuilderSupport<SoapActionBuilder>() {
            @Override
            public void configure(SoapActionBuilder builder) {
                builder.client("webServiceClient")
                        .receive()
                        .payload("<ns0:SoapMessageWithAttachmentResponse xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                "<ns0:Success>true</ns0:Success>" +
                                "</ns0:SoapMessageWithAttachmentResponse>")
                        .schemaValidation(false);
            }
        });
    }

    @CitrusTest
    public void sendSoapAttachmentDeprecated() {
        parallel().actions(
            send(new BuilderSupport<SendMessageBuilder>() {
                @Override
                public void configure(SendMessageBuilder builder) {
                    builder.endpoint("webServiceClient")
                            .soap()
                            .payload("<ns0:SoapMessageWithAttachmentRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                    "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                    "</ns0:SoapMessageWithAttachmentRequest>")
                            .attachment("MySoapAttachment", "text/plain", new ClassPathResource("com/consol/citrus/ws/soapAttachment.txt"));
                }
            }),
            sequential().actions(
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint("webServiceRequestReceiver")
                                .payload("<ns0:SoapMessageWithAttachmentRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                        "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                        "</ns0:SoapMessageWithAttachmentRequest>")
                                .schemaValidation(false)
                                .extractFromHeader("citrus_jms_messageId", "internal_correlation_id")
                                .soap()
                                .attachment("MySoapAttachment", "text/plain", new ClassPathResource("com/consol/citrus/ws/soapAttachment.txt"))
                                .timeout(5000L);
                    }
                }),
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint("webServiceResponseSender")
                                .payload("<ns0:SoapMessageWithAttachmentResponse xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                        "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                        "<ns0:Success>true</ns0:Success>" +
                                        "</ns0:SoapMessageWithAttachmentResponse>")
                                .header("citrus_jms_correlationId", "${internal_correlation_id}");
                    }
                })
            )
        );
        
        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("webServiceClient")
                        .payload("<ns0:SoapMessageWithAttachmentResponse xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                "<ns0:Success>true</ns0:Success>" +
                                "</ns0:SoapMessageWithAttachmentResponse>")
                        .schemaValidation(false);
            }
        });
        
        parallel().actions(
            send(new BuilderSupport<SendMessageBuilder>() {
                @Override
                public void configure(SendMessageBuilder builder) {
                    builder.endpoint("webServiceClient")
                            .soap()
                            .payload("<ns0:SoapMessageWithAttachmentRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                    "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                    "</ns0:SoapMessageWithAttachmentRequest>")
                            .attachment("MySoapAttachment", "text/plain", "This is an attachment!");
                }
            }),
            sequential().actions(
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint("webServiceRequestReceiver")
                                .payload("<ns0:SoapMessageWithAttachmentRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                        "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                        "</ns0:SoapMessageWithAttachmentRequest>")
                                .schemaValidation(false)
                                .extractFromHeader("citrus_jms_messageId", "internal_correlation_id")
                                .soap()
                                .attachment("MySoapAttachment", "text/plain", "This is an attachment!")
                                .timeout(5000L);
                    }
                }),
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint("webServiceResponseSender")
                                .payload("<ns0:SoapMessageWithAttachmentResponse xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                        "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                        "<ns0:Success>true</ns0:Success>" +
                                        "</ns0:SoapMessageWithAttachmentResponse>")
                                .header("citrus_jms_correlationId", "${internal_correlation_id}");
                    }
                })
            )
        );
        
        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("webServiceClient")
                        .payload("<ns0:SoapMessageWithAttachmentResponse xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                "<ns0:Operation>Read the attachment</ns0:Operation>" +
                                "<ns0:Success>true</ns0:Success>" +
                                "</ns0:SoapMessageWithAttachmentResponse>")
                        .schemaValidation(false);
            }
        });
    }
}