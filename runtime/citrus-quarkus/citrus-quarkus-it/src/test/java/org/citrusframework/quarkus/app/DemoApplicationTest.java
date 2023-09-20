/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.quarkus.app;

import io.quarkus.test.junit.QuarkusTest;
import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfig;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.quarkus.app.validation.TextEqualsMessageValidator;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.validation.MessageValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariables;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

@QuarkusTest
@CitrusSupport
public class DemoApplicationTest {

    @CitrusFramework
    private Citrus citrus;

    @BindToRegistry
    private final MessageQueue messages = new DefaultMessageQueue("messages");

    @CitrusEndpoint
    @DirectEndpointConfig(
        queue = "messages"
    )
    private DirectEndpoint messageEndpoint;

    @CitrusResource
    private TestCaseRunner t;

    @BindToRegistry
    private final TextEqualsMessageValidator textEqualsMessageValidator = new TextEqualsMessageValidator();

    @CitrusResource
    private TestContext context;

    @Test
    void shouldInjectCitrusResources() {
        Assertions.assertNotNull(citrus);
        Assertions.assertNotNull(context);
        Assertions.assertNotNull(t);
        Assertions.assertNotNull(messageEndpoint);
        Assertions.assertEquals(context.getReferenceResolver().resolve("textEqualsMessageValidator", MessageValidator.class), textEqualsMessageValidator);

        t.given(
            createVariables().variable("text", "Citrus rocks!")
        );

        t.when(
            send()
                .endpoint(messageEndpoint)
                .message()
                .body("${text}")
        );

        t.when(
            receive()
                .endpoint(messageEndpoint)
                .message()
                .body("${text}")
        );
    }
}
