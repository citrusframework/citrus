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

package org.citrusframework.json.integration;

import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfig;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.TestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @since 3.3.1
 */
@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Isolated
public class JsonPathExpressionValidationIT implements TestActionSupport {

    @BindToRegistry
    public MessageQueue test = new DefaultMessageQueue("test");

    @CitrusEndpoint
    @DirectEndpointConfig(
        queue = "test"
    )
    public DirectEndpoint direct;

    private static final String JSON_BODY = "{\"user\":\"christoph\", \"age\": 32}";

    @Test
    @CitrusTest
    public void shouldPerformJsonPathValidation(@CitrusResource TestActionRunner runner) {
        runner.$(send(direct)
                .message()
                .type(MessageType.JSON)
                .body(JSON_BODY)
        );

        runner.$(receive(direct)
                .message()
                .body("{\"user\":\"@ignore@\", \"age\": \"@ignore@\"}")
                .validate(validation().jsonPath()
                            .expression("$.user", "christoph")
                            .expression("$.age", 32))
        );
    }

    @Test
    @CitrusTest
    public void shouldPerformJsonPathValidationWithMessageProcessing(@CitrusResource TestActionRunner runner) {
        runner.$(send(direct)
                .message()
                .type(MessageType.JSON)
                .body("{\"user\":\"?\", \"age\": 0}")
                .process(processor().jsonPath().expression("$.user", "christoph"))
                .process(processor().jsonPath().expression("$.age", 32))
        );

        runner.$(receive(direct)
                .message()
                .type(MessageType.JSON)
                .body("{\"user\":\"x\", \"age\": \"99\"}")
                .process(processor().jsonPath().expression("$.age", 32))
                .process(processor().jsonPath().expression("$.user", "christoph"))
                .validate(validation().jsonPath()
                            .expression("$.user", "christoph")
                            .expression("$.age", 32))
        );
    }

    @Test
    @CitrusTest
    public void shouldFailOnJsonPathValidation(@CitrusResource TestActionRunner runner) {
        runner.$(send(direct)
                .message()
                .type(MessageType.JSON)
                .body(JSON_BODY)
        );

        assertThrows(TestCaseFailedException.class, () -> runner.$(receive(direct)
                .message()
                .body("{\"user\":\"@ignore@\", \"age\": \"@ignore@\"}")
                .validate(validation().jsonPath().expression("$.user", "wrong"))
        ));
    }

    @Test
    @CitrusTest
    public void shouldPerformJsonPathValidationWithMultipleExpressions(@CitrusResource TestActionRunner runner) {
        runner.$(send(direct)
                .message()
                .type(MessageType.JSON)
                .body(JSON_BODY)
        );

        runner.$(receive(direct)
                .message()
                .body("{\"user\":\"@ignore@\", \"age\": \"@ignore@\"}")
                .validate(validation().jsonPath().expression("$.user", "christoph"))
                .validate(validation().jsonPath().expression("$.age", 32))
        );
    }

    @Test
    @CitrusTest
    public void shouldFailOnJsonPathValidationWithMultipleExpressions(@CitrusResource TestActionRunner runner) {
        runner.$(send(direct)
                .message()
                .type(MessageType.JSON)
                .body(JSON_BODY)
        );

        assertThrows(TestCaseFailedException.class, () -> runner.$(receive(direct)
                .message()
                .body("{\"user\":\"@ignore@\", \"age\": \"@ignore@\"}")
                .validate(validation().jsonPath().expression("$.user", "christoph"))
                .validate(validation().jsonPath().expression("$.age", 0))
        ));
    }
}
