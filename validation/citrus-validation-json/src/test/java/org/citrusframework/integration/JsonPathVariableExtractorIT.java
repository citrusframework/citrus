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

package org.citrusframework.integration;

import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfig;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;

/**
 * @since 3.3.1
 */
public class JsonPathVariableExtractorIT extends TestNGCitrusSpringSupport {

    @BindToRegistry
    public MessageQueue test = new DefaultMessageQueue("test");

    @CitrusEndpoint
    @DirectEndpointConfig(
        queue = "test"
    )
    public DirectEndpoint direct;

    @CitrusResource
    private TestContext context;

    private static final String JSON_BODY = "{\"user\":\"christoph\", \"age\": 32}";

    @Test
    @CitrusTest
    public void shouldPerformJsonPathVariableExtract() {
        $(send(direct)
                .message()
                .type(MessageType.JSON)
                .body(JSON_BODY)
        );

        $(receive(direct)
                .message()
                .body(JSON_BODY)
                .extract(jsonPath()
                            .expression("$.user", "user")
                            .expression("$.age", "age"))
        );

        Assert.assertEquals(context.getVariable("user"), "christoph");
        Assert.assertEquals(context.getVariable("age", Long.class), 32L);
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    @CitrusTest
    public void shouldFailOnJsonPathVariableExtract() {
        $(send(direct)
                .message()
                .type(MessageType.JSON)
                .body(JSON_BODY)
        );

        $(receive(direct)
                .message()
                .body(JSON_BODY)
                .extract(jsonPath().expression("$.wrong", "user"))
        );
    }

    @Test
    @CitrusTest
    public void shouldPerformJsonPathValidationWithMultipleExpressions() {
        $(send(direct)
                .message()
                .type(MessageType.JSON)
                .body(JSON_BODY)
        );

        $(receive(direct)
                .message()
                .body(JSON_BODY)
                .extract(jsonPath().expression("$.user", "user"))
                .extract(jsonPath().expression("$.age", "age"))
        );

        Assert.assertEquals(context.getVariable("user"), "christoph");
        Assert.assertEquals(context.getVariable("age", Long.class), 32L);
    }
}
