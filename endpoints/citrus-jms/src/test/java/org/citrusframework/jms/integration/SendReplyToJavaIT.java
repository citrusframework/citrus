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

package org.citrusframework.jms.integration;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.citrusframework.container.Parallel.Builder.parallel;
import static org.citrusframework.container.Sequence.Builder.sequential;
import static org.citrusframework.dsl.MessageSupport.MessageHeaderSupport.fromHeaders;

/**
 * @author Christoph Deppisch
 */
@Test
public class SendReplyToJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void jmsSyncQueues() {
        variable("operation", "GetDate");
        variable("conversationId", "123456789");

        run(parallel().actions(
            send("syncGetDateRequestSender")
                .message()
                .body("<GetDateRequestMessage>" +
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
                .extract(fromHeaders()
                            .header(MessageHeaders.ID, "syncRequestCorrelatorId")),

            sequential().actions(
                receive("syncGetDateRequestReceiver")
                    .message()
                    .body("<GetDateRequestMessage>" +
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
                    .validator("defaultXmlMessageValidator"),

                send("syncGetDateRequestReceiver")
                    .message()
                    .body("<GetDateResponseMessage>" +
                                "<MessageHeader>" +
                                "<ConversationId>${conversationId}</ConversationId>" +
                                "<Timestamp>citrus:currentDate()</Timestamp>" +
                            "</MessageHeader>" +
                            "<MessageBody>" +
                                "<Value>citrus:currentDate()</Value>" +
                            "</MessageBody>" +
                        "</GetDateResponseMessage>")
                    .header("Operation", "${operation}")
                    .header("ConversationId", "${conversationId}"),

                receive("syncGetDateRequestSender")
                    .selector("citrus_message_id = '${syncRequestCorrelatorId}'")
                    .message()
                    .body("<GetDateResponseMessage>" +
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
                    .timeout(5000L)
            )
        ));
    }
}
