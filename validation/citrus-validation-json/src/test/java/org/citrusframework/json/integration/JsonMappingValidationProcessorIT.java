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
import org.citrusframework.json.dsl.JsonSupport;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import static org.junit.jupiter.api.Assertions.assertThrows;
import tools.jackson.databind.json.JsonMapper;

/**
 * @since 3.3.1
 */
@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Isolated
public class JsonMappingValidationProcessorIT implements TestActionSupport {

    @BindToRegistry
    public MessageQueue test = new DefaultMessageQueue("test");

    @CitrusEndpoint
    @DirectEndpointConfig(
        queue = "test"
    )
    public DirectEndpoint direct;

    private static final String JSON_BODY = "{\"name\":\"christoph\", \"age\": 32}";

    private final JsonMapper mapper = JsonMapper.shared();

    @Test
    @CitrusTest
    public void shouldPerformJsonMappingValidationProcessor(@CitrusResource TestActionRunner runner) {
        runner.$(send(direct)
                .message()
                .type(MessageType.JSON)
                .body(JSON_BODY)
        );

        runner.$(receive(direct)
                .message()
                .validate(JsonSupport.validate(Person.class)
                    .mapper(mapper)
                    .validator((person, headers, context) -> {
                        Assertions.assertEquals("christoph", person.name);
                        Assertions.assertTrue(person.age > 30);
                    }).build())
        );
    }

    @Test
    @CitrusTest
    public void shouldPerformMultipleJsonMappingValidationProcessor(@CitrusResource TestActionRunner runner) {
        runner.$(send(direct)
                .message()
                .type(MessageType.JSON)
                .body(JSON_BODY)
        );

        runner.$(receive(direct)
                .message()
                .validate(JsonSupport.validate(Person.class)
                    .mapper(mapper)
                    .validator((person, headers, context) -> Assertions.assertEquals("christoph", person.name)).build())
                .validate(JsonSupport.validate(Person.class)
                    .mapper(mapper)
                    .validator((person, headers, context) -> Assertions.assertTrue(person.age > 30)).build())
        );
    }

    @Test
    @CitrusTest
    public void shouldFailOnJsonMultipleMappingValidationProcessor(@CitrusResource TestActionRunner runner) {
        runner.$(send(direct)
                .message()
                .type(MessageType.JSON)
                .body(JSON_BODY)
        );

        assertThrows(TestCaseFailedException.class, () -> runner.$(receive(direct)
                .message()
                .validate(JsonSupport.validate(Person.class)
                        .mapper(mapper)
                        .validator((person, headers, context) -> Assertions.assertTrue(person.age > 30)).build())
                .validate(JsonSupport.validate(Person.class)
                        .mapper(mapper)
                        .validator((person, headers, context) -> Assertions.assertEquals("should fail", person.name)).build())
        ));
    }

    @Test
    @CitrusTest
    public void shouldFailOnJsonMappingValidationProcessor(@CitrusResource TestActionRunner runner) {
        runner.$(send(direct)
                .message()
                .type(MessageType.JSON)
                .body(JSON_BODY)
        );

        assertThrows(TestCaseFailedException.class, () -> runner.$(receive(direct)
                .message()
                .validate(JsonSupport.validate(Person.class)
                        .mapper(mapper)
                        .validator((person, headers, context) -> Assertions.assertEquals("should fail", person.name)).build())
        ));
    }

    private static class Person {
        String name;
        int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
