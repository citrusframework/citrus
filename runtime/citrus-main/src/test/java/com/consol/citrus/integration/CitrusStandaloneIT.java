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

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusSpringContextProvider;
import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.direct.DirectEndpoint;
import com.consol.citrus.endpoint.direct.DirectEndpointBuilder;
import com.consol.citrus.message.DefaultMessageQueue;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageQueue;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.DefaultMessageValidator;
import com.consol.citrus.validation.context.ValidationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.EchoAction.Builder.echo;
import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
@ContextConfiguration(classes = { CitrusSpringConfig.class, CitrusStandaloneIT.Config.class } )
public class CitrusStandaloneIT extends AbstractTestNGSpringContextTests {

    private Citrus citrus;

    private TestCaseRunner test;

    @Autowired
    private DirectEndpoint endpoint;

    @BeforeClass
    public void beforeAll() {
        citrus = Citrus.newInstance(new CitrusSpringContextProvider(applicationContext));
    }

    @BeforeMethod
    public void start() {
        TestContext context = citrus.getCitrusContext().createTestContext();
        test = new DefaultTestCaseRunner(context);
        CitrusAnnotations.injectEndpoints(this, context);
        test.start();
    }

    @AfterMethod
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
                public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, ValidationContext
                        validationContext) {
                    Assert.isTrue(receivedMessage.getPayload(String.class).equals(controlMessage.getPayload(String.class)), "Validation failed - " +
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
