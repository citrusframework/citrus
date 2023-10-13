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

package org.citrusframework.junit.jupiter.integration.spring;

import org.citrusframework.TestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.container.BeforeTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.DirectEndpointBuilder;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.validation.DefaultMessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 */
@CitrusSpringSupport
@ContextConfiguration(classes = {CitrusSpringConfig.class, SpringBean_IT.EndpointConfig.class})
class SpringBean_IT {

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
    static class EndpointConfig {

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
