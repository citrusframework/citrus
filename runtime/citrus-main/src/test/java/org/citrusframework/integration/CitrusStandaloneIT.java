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

import org.citrusframework.Citrus;
import org.citrusframework.spring.CitrusSpringContextProvider;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.DirectEndpointBuilder;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.validation.DefaultMessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @since 2.2
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { CitrusSpringConfig.class, CitrusStandaloneIT.Config.class } )
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CitrusStandaloneIT implements TestActionSupport {

    private Citrus citrus;

    private TestCaseRunner test;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DirectEndpoint endpoint;

    @BeforeAll
    public void beforeAll() {
        citrus = Citrus.newInstance(new CitrusSpringContextProvider(applicationContext));
    }

    @BeforeEach
    public void start() {
        TestContext context = citrus.getCitrusContext().createTestContext();
        test = new DefaultTestCaseRunner(context);
        CitrusAnnotations.injectEndpoints(this, context);
        test.start();
    }

    @AfterEach
    public void stop() {
        test.stop();
    }

    @Test
    public void echoTest() {
        test.name("EchoIT");

        test.run(echo("Hello Citrus!"));

        test.stop();
    }

    @Test
    public void endpointTest() {
        test.name("EndpointIT");

        test.run(echo("Send message!"));

        test.run(send(endpoint)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hello from Citrus!"));

        test.run(echo("Receive message!"));

        test.run(receive(endpoint)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hello from Citrus!"));
    }

    @Test
    public void dynamicEndpointTest() {
        test.name("DynamicEndpointIT");

        test.run(echo("Send message!"));

        test.run(send("direct:my.queue")
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hello Citrus!"));

        test.run(echo("Receive message!"));

        test.run(receive("direct:my.queue")
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hello Citrus!"));
    }

    @Configuration
    protected static class Config {

        @Bean(name = "test.queue")
        public MessageQueue testQueue() {
            return new DefaultMessageQueue("test.queue");
        }

        @Bean(name = "my.queue")
        public MessageQueue myQueue() {
            return new DefaultMessageQueue("my.queue");
        }

        @Bean
        public DirectEndpoint endpoint() {
            return new DirectEndpointBuilder()
                    .queue(testQueue())
                    .build();
        }

        @Bean
        public DefaultMessageValidator plainTextValidator() {
            return new DefaultMessageValidator() {
                @Override
                public void validateMessage(Message receivedMessage, Message controlMessage,
                                            TestContext context, ValidationContext validationContext) {
                    Assertions.assertEquals(receivedMessage.getPayload(String.class), controlMessage.getPayload(String.class), "Validation failed - " +
                            "expected message contents not equal!");
                }

                @Override
                public boolean supportsMessageType(String messageType, Message message) {
                    return messageType.equalsIgnoreCase(MessageType.PLAINTEXT.toString());
                }
            };
        }
    }
}
