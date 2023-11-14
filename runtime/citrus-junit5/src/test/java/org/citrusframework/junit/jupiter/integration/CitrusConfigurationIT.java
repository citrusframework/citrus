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

import org.citrusframework.Citrus;
import org.citrusframework.TestActionRunner;
import org.citrusframework.annotations.CitrusConfiguration;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.DirectEndpointBuilder;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

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
        public DefaultTextEqualsMessageValidator plaintextValidator() {
            return new DefaultTextEqualsMessageValidator();
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
