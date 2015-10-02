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
import com.consol.citrus.dsl.builder.ReceiveMessageBuilder;
import com.consol.citrus.dsl.builder.SendMessageBuilder;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.message.MessageHeaders;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class SendReplyToTestRunnerIT extends TestNGCitrusTestRunner {
    
    @CitrusTest
    public void jmsSyncQueues() {
        variable("operation", "GetDate");
        variable("conversationId", "123456789");
        
        parallel().actions(
            send(new BuilderSupport<SendMessageBuilder>() {
                @Override
                public void configure(SendMessageBuilder builder) {
                    builder.endpoint("syncGetDateRequestSender")
                            .payload("<GetDateRequestMessage>" +
                                    "<MessageHeader>" +
                                    "<ConversationId>${conversationId}</ConversationId>" +
                                    "<Timestamp>citrus:currentDate()</Timestamp>" +
                                    "</MessageHeader>" +
                                    "<MessageBody>" +
                                    "<Format>yyyy-mm-dd</Format>" +
                                    "</MessageBody>" +
                                    "</GetDateRequestMessage>")
                            .header("Operation", "${operation}")
                            .header("ConversationId", "${conversationId}")
                            .extractFromHeader(MessageHeaders.ID, "syncRequestCorrelatorId");
                }
            }),
                
            sequential().actions(
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint("syncGetDateRequestReceiver")
                                .payload("<GetDateRequestMessage>" +
                                        "<MessageHeader>" +
                                        "<ConversationId>${conversationId}</ConversationId>" +
                                        "<Timestamp>citrus:currentDate()</Timestamp>" +
                                        "</MessageHeader>" +
                                        "<MessageBody>" +
                                        "<Format>yyyy-mm-dd</Format>" +
                                        "</MessageBody>" +
                                        "</GetDateRequestMessage>")
                                .header("Operation", "${operation}")
                                .header("ConversationId", "${conversationId}")
                                .validator("defaultXmlMessageValidator");
                    }
                }),
                    
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint("syncGetDateRequestReceiver")
                                .payload("<GetDateResponseMessage>" +
                                        "<MessageHeader>" +
                                        "<ConversationId>${conversationId}</ConversationId>" +
                                        "<Timestamp>citrus:currentDate()</Timestamp>" +
                                        "</MessageHeader>" +
                                        "<MessageBody>" +
                                        "<Value>citrus:currentDate()</Value>" +
                                        "</MessageBody>" +
                                        "</GetDateResponseMessage>")
                                .header("Operation", "${operation}")
                                .header("ConversationId", "${conversationId}");
                    }
                }),
                    
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint("syncGetDateRequestSender")
                                .selector("citrus_message_id = '${syncRequestCorrelatorId}'")
                                .payload("<GetDateResponseMessage>" +
                                        "<MessageHeader>" +
                                        "<ConversationId>${conversationId}</ConversationId>" +
                                        "<Timestamp>citrus:currentDate()</Timestamp>" +
                                        "</MessageHeader>" +
                                        "<MessageBody>" +
                                        "<Value>citrus:currentDate()</Value>" +
                                        "</MessageBody>" +
                                        "</GetDateResponseMessage>")
                                .header("Operation", "${operation}")
                                .header("ConversationId", "${conversationId}")
                                .timeout(5000L);
                    }
                })
            )
        );
    }
}