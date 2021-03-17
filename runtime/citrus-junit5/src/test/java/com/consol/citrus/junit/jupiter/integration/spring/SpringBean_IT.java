/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.junit.jupiter.integration.spring;

import com.consol.citrus.TestActionRunner;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.container.BeforeTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.direct.DirectEndpoint;
import com.consol.citrus.endpoint.direct.DirectEndpointBuilder;
import com.consol.citrus.junit.jupiter.spring.CitrusSpringSupport;
import com.consol.citrus.message.DefaultMessageQueue;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageQueue;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.DefaultMessageValidator;
import com.consol.citrus.validation.context.ValidationContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 */
@CitrusSpringSupport
@ContextConfiguration(classes = {CitrusSpringConfig.class, SpringBean_IT.EndpointConfig.class})
public class SpringBean_IT {

    @Autowired
    private DirectEndpoint direct;

    @Test
    @CitrusTest
    void springBeanTest(@CitrusResource TestActionRunner actions) {
        actions.$(send().endpoint(direct)
                    .message()
                    .body("Hello from Citrus! Now is ${time}"));

        actions.$(receive().endpoint(direct)
                    .message()
                    .body("Hello from Citrus! Now is ${time}"));
    }

    @Configuration
    public static class EndpointConfig {

        @Bean
        public BeforeTest beforeTest() {
            return new SequenceBeforeTest.Builder()
                    .actions(context -> context.setVariable("time", "citrus:currentDate()"))
                    .build();
        }

        @Bean
        public DefaultMessageValidator validator() {
            return new DefaultMessageValidator() {
                @Override
                public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, ValidationContext validationContext) {
                    Assert.isTrue(receivedMessage.getPayload(String.class).equals(controlMessage.getPayload(String.class)), "Validation failed - " +
                            "expected message contents not equal!");
                }

                @Override
                public boolean supportsMessageType(String messageType, Message message) {
                    return messageType.equalsIgnoreCase(MessageType.XML.toString())
                            || messageType.equalsIgnoreCase(MessageType.PLAINTEXT.toString());
                }
            };
        }

        @Bean
        public MessageQueue directQueue() {
            return new DefaultMessageQueue("directQueue");
        }

        @Bean
        public DirectEndpoint direct(MessageQueue directQueue) {
            return new DirectEndpointBuilder()
                    .queue(directQueue)
                    .build();
        }
    }
}
