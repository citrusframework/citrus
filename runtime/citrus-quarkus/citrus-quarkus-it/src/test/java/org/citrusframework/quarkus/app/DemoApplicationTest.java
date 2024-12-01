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

package org.citrusframework.quarkus.app;

import java.util.Collections;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTest;
import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.DirectEndpointBuilder;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfig;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.quarkus.ApplicationPropertiesSupplier;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.validation.MessageValidator;
import org.junit.jupiter.api.Test;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariables;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@CitrusSupport(applicationPropertiesSupplier = DemoApplicationTest.class)
public class DemoApplicationTest implements ApplicationPropertiesSupplier {

    @CitrusFramework
    private Citrus citrus;

    @BindToRegistry
    private final MessageQueue messageQueue = new DefaultMessageQueue("messages");

    @CitrusEndpoint
    @DirectEndpointConfig(queue = "messageQueue")
    private DirectEndpoint messages;

    @BindToRegistry
    private final DirectEndpoint moreMessages = new DirectEndpointBuilder()
            .queue(messageQueue)
            .build();

    @CitrusResource
    private TestCaseRunner testCaseRunner;

    @BindToRegistry
    private final DefaultTextEqualsMessageValidator textEqualsMessageValidator = new DefaultTextEqualsMessageValidator();

    @CitrusResource
    private TestContext context;

    @Test
    void shouldInjectCitrusResources() {
        assertNotNull(citrus);
        assertNotNull(context);
        assertNotNull(testCaseRunner);
        assertNotNull(messages);
        assertNotNull(moreMessages);
        assertEquals(context.getReferenceResolver().resolve("textEqualsMessageValidator", MessageValidator.class), textEqualsMessageValidator);

        testCaseRunner.variable("greeting", "Hello!");

        testCaseRunner.given(
                createVariables().variable("text", "Citrus rocks!")
        );

        testCaseRunner.when(
                send()
                        .endpoint(messages)
                        .message()
                        .body("${text}")
        );

        testCaseRunner.then(
                receive()
                        .endpoint(messages)
                        .message()
                        .body("${text}")
        );

        testCaseRunner.when(
                send()
                        .endpoint(moreMessages)
                        .message()
                        .body("${greeting}")
        );

        testCaseRunner.then(
                receive()
                        .endpoint(moreMessages)
                        .message()
                        .body("${greeting}")
        );
    }

    @Override
    public Map<String, String> get() {
        return Collections.singletonMap("greeting.message", "Hello, Citrus rocks!");
    }
}
