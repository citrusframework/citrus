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

package com.consol.citrus.actions;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;

/**
 * @author Christoph Deppisch
 */
public class SendReplyToJavaITest extends TestNGCitrusTestBuilder {
    
    @Override
    protected void configure() {
        variable("operation", "GetDate");
        variable("conversationId", "123456789");
        
        parallel(
            send("syncGetDateRequestSender")
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
                .extractFromHeader("id", "syncRequestCorrelatorId"),
                
            sequential(
                receive("syncGetDateRequestReceiver")
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
                    .extractFromHeader("id", "syncMessageCorrelatorId")
                    .validator("xmlMessageValidator"),
                    
                send("getDateRequestReplySender")
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
                    .header("citrus_sync_message_correlator", "${syncMessageCorrelatorId}"),
                    
                sleep(1000L),
                
                receive("syncGetDateResponseHandler")
                    .selector("id = '${syncRequestCorrelatorId}'")
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
            )
        );
    }
    
    @Test
    public void sendReplyToITest(ITestContext testContext) {
        executeTest(testContext);
    }
}