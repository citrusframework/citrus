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

package org.citrusframework.junit.jupiter.integration;

import java.util.List;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusContext;
import org.citrusframework.TestActionRunner;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.direct.DirectEndpointBuilder;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfig;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.junit.jupiter.CitrusExtension;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.DefaultValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testng.Assert;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 */
@CitrusSupport
public class EndpointInjectionIT implements CitrusExtension.TestListener {

    @CitrusFramework
    private Citrus citrus;

    @CitrusEndpoint
    @DirectEndpointConfig(queueName = "FOO.direct.queue")
    @BindToRegistry
    private Endpoint directEndpoint;

    @CitrusEndpoint
    private Endpoint foo;

    @BindToRegistry
    private final MessageQueue messages = new DefaultMessageQueue("messages");

    @Override
    public void before(CitrusContext context) {
        Assert.assertNotNull(citrus);

        context.bind("foo", new DirectEndpointBuilder()
                .queue("messages")
                .build());
    }

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

    @BindToRegistry(name = "FOO.direct.queue")
    public MessageQueue queue() {
        return new DefaultMessageQueue("FOO.direct.queue");
    }

    @Test
    @CitrusTest
    public void injectEndpoint(@CitrusResource TestActionRunner $) {
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
}
