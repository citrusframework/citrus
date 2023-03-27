/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.citrus.integration.inject;

import org.citrusframework.citrus.Citrus;
import org.citrusframework.citrus.CitrusContext;
import org.citrusframework.citrus.annotations.CitrusEndpoint;
import org.citrusframework.citrus.annotations.CitrusFramework;
import org.citrusframework.citrus.annotations.CitrusTest;
import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.endpoint.direct.DirectEndpointBuilder;
import org.citrusframework.citrus.endpoint.direct.annotation.DirectEndpointConfig;
import org.citrusframework.citrus.message.DefaultMessageQueue;
import org.citrusframework.citrus.message.MessageQueue;
import org.citrusframework.citrus.message.MessageType;
import org.citrusframework.citrus.spi.BindToRegistry;
import org.citrusframework.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.citrus.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.citrus.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class EndpointInjectionJavaIT extends TestNGCitrusSpringSupport {

    @CitrusFramework
    private Citrus citrus;

    @CitrusEndpoint
    @DirectEndpointConfig(queueName = "FOO.direct.queue")
    @BindToRegistry
    private Endpoint directEndpoint;

    @CitrusEndpoint
    private Endpoint foo;

    @Override
    public void before(CitrusContext context) {
        Assert.assertNotNull(citrus);

        context.bind("messages", new DefaultMessageQueue("messages"));
        context.bind("foo", new DirectEndpointBuilder()
                .queue("messages")
                .build());
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
