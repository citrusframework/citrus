/*
 * Copyright the original author or authors.
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

package org.citrusframework.junit.integration;

import org.citrusframework.Citrus;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.DirectEndpointBuilder;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfig;
import org.citrusframework.junit.spring.JUnit4CitrusSpringSupport;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.BindToRegistry;
import org.junit.Assert;
import org.junit.Test;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

public class EndpointInjectionIT extends JUnit4CitrusSpringSupport {

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

    @BindToRegistry
    public DirectEndpoint foo() {
        return new DirectEndpointBuilder()
                .queue(messages)
                .build();
    }

    @BindToRegistry(name = "FOO.direct.queue")
    public MessageQueue queue() {
        return new DefaultMessageQueue("FOO.direct.queue");
    }

    @Test
    @CitrusTest
    public void injectEndpoint() {
        Assert.assertNotNull(foo);

        run(send(directEndpoint)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hello!"));

        run(receive(directEndpoint)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hello!"));

        run(send("directEndpoint")
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hi!"));

        run(receive("directEndpoint")
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hi!"));

        run(send(foo)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hello Citrus!"));

        run(receive(foo)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hello Citrus!"));

        Assert.assertNotNull(citrus);
    }
}
