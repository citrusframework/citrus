/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.integration;

import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.JsonSupport;
import com.consol.citrus.endpoint.direct.DirectEndpoint;
import com.consol.citrus.endpoint.direct.annotation.DirectEndpointConfig;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.message.DefaultMessageQueue;
import com.consol.citrus.message.MessageQueue;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.spi.BindToRegistry;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 * @since 3.3.1
 */
public class JsonMappingValidationProcessorIT extends TestNGCitrusSpringSupport {

    @BindToRegistry
    public MessageQueue test = new DefaultMessageQueue("test");

    @CitrusEndpoint
    @DirectEndpointConfig(
        queue = "test"
    )
    public DirectEndpoint direct;

    private static final String JSON_BODY = "{\"name\":\"christoph\", \"age\": 32}";

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @CitrusTest
    public void shouldPerformJsonMappingValidationProcessor() {
        $(send(direct)
                .message()
                .type(MessageType.JSON)
                .body(JSON_BODY)
        );

        $(receive(direct)
                .message()
                .validate(JsonSupport.validate(Person.class)
                    .mapper(mapper)
                    .validator((person, headers, context) -> {
                        Assert.assertEquals(person.name, "christoph");
                        Assert.assertTrue(person.age > 30);
                    }).build())
        );
    }

    @Test
    @CitrusTest
    public void shouldPerformMultipleJsonMappingValidationProcessor() {
        $(send(direct)
                .message()
                .type(MessageType.JSON)
                .body(JSON_BODY)
        );

        $(receive(direct)
                .message()
                .validate(JsonSupport.validate(Person.class)
                    .mapper(mapper)
                    .validator((person, headers, context) -> {
                        Assert.assertEquals(person.name, "christoph");
                    }).build())
                .validate(JsonSupport.validate(Person.class)
                    .mapper(mapper)
                    .validator((person, headers, context) -> {
                        Assert.assertTrue(person.age > 30);
                    }).build())
        );
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    @CitrusTest
    public void shouldFailOnJsonMultipleMappingValidationProcessor() {
        $(send(direct)
                .message()
                .type(MessageType.JSON)
                .body(JSON_BODY)
        );

        $(receive(direct)
                .message()
                .validate(JsonSupport.validate(Person.class)
                        .mapper(mapper)
                        .validator((person, headers, context) -> {
                            Assert.assertTrue(person.age > 30);
                        }).build())
                .validate(JsonSupport.validate(Person.class)
                        .mapper(mapper)
                        .validator((person, headers, context) -> {
                            Assert.assertEquals(person.name, "should fail");
                        }).build())
        );
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    @CitrusTest
    public void shouldFailOnJsonMappingValidationProcessor() {
        $(send(direct)
                .message()
                .type(MessageType.JSON)
                .body(JSON_BODY)
        );

        $(receive(direct)
                .message()
                .validate(JsonSupport.validate(Person.class)
                        .mapper(mapper)
                        .validator((person, headers, context) -> {
                            Assert.assertEquals(person.name, "should fail");
                        }).build())
        );
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
