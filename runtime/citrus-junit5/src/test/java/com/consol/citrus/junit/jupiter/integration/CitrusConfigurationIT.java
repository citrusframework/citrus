/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.junit.jupiter.integration;

import java.util.List;

import com.consol.citrus.Citrus;
import com.consol.citrus.TestActionRunner;
import com.consol.citrus.annotations.CitrusConfiguration;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusFramework;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.direct.DirectEndpoint;
import com.consol.citrus.endpoint.direct.DirectEndpointBuilder;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.junit.jupiter.CitrusSupport;
import com.consol.citrus.message.DefaultMessageQueue;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageQueue;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.spi.BindToRegistry;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.context.DefaultValidationContext;
import com.consol.citrus.validation.context.ValidationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 */
@CitrusSupport
@CitrusConfiguration(classes = CitrusConfigurationIT.Endpoints.class)
public class CitrusConfigurationIT {

    @CitrusFramework
    private Citrus citrus;

    @CitrusEndpoint
    private Endpoint directEndpoint;

    @CitrusEndpoint
    private Endpoint foo;

    @Test
    @CitrusTest
    public void shouldLoadConfiguration(@CitrusResource TestActionRunner $) {
        Assertions.assertNotNull(foo);

        $.run(send(directEndpoint)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hello!"));

        $.run(receive(directEndpoint)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hello!"));

        $.run(send("directEndpoint")
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hi!"));

        $.run(receive("directEndpoint")
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hi!"));

        $.run(send(foo)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hello Citrus!"));

        $.run(receive(foo)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hello Citrus!"));

        Assertions.assertNotNull(citrus);
    }

    public static class Endpoints {
        @BindToRegistry
        private Endpoint directEndpoint = new DirectEndpointBuilder()
                .queue("TEST.direct.queue")
                .build();

        @BindToRegistry
        private final MessageQueue messages = new DefaultMessageQueue("messages");

        @BindToRegistry
        public MessageValidator<DefaultValidationContext> plaintextValidator() {
            return new MessageValidator<>() {
                @Override
                public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, List<ValidationContext> validationContexts) throws ValidationException {
                    org.testng.Assert.assertEquals(receivedMessage.getPayload(String.class), controlMessage.getPayload());
                }

                @Override
                public boolean supportsMessageType(String messageType, Message message) {
                    return messageType.equalsIgnoreCase(MessageType.PLAINTEXT.name());
                }
            };
        }

        @BindToRegistry
        public DirectEndpoint foo() {
            return new DirectEndpointBuilder()
                    .queue(messages)
                    .build();
        }

        @BindToRegistry(name = "TEST.direct.queue")
        public MessageQueue queue() {
            return new DefaultMessageQueue("FOO.direct.queue");
        }
    }
}
