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

package org.citrusframework.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReceiveMessageActionLoggingTest {

    @Test
    public void shouldLogBeforeWaitingForMessage() {
        Endpoint endpoint = mock(Endpoint.class);
        EndpointConfiguration endpointConfiguration = mock(EndpointConfiguration.class);
        Consumer consumer = mock(Consumer.class);
        TestContext context = mock(TestContext.class);

        when(endpoint.getName()).thenReturn("jiraServer.inbound");
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(120000L);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(consumer.receive(context, 120000L)).thenThrow(new IllegalStateException("stop after receive starts"));

        TestAppender appender = new TestAppender();
        Logger logger = (Logger) LogManager.getLogger(ReceiveMessageAction.class);
        Level previousLevel = logger.getLevel();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);

        try {
            ReceiveMessageAction action = new ReceiveMessageAction.Builder()
                    .endpoint(endpoint)
                    .build();

            assertThatThrownBy(() -> action.doExecute(context))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("stop after receive starts");

            assertThat(appender.messages)
                    .contains("Waiting to receive message on endpoint: 'jiraServer.inbound' (timeout: 120000ms)");
        } finally {
            logger.removeAppender(appender);
            logger.setLevel(previousLevel);
            appender.stop();
        }
    }

    @Test
    public void shouldLogBeforeWaitingForSelectedMessage() {
        Endpoint endpoint = mock(Endpoint.class);
        SelectiveConsumer consumer = mock(SelectiveConsumer.class);
        TestContext context = mock(TestContext.class);
        String selector = "operation = 'sayHello'";

        when(endpoint.getName()).thenReturn("requests.inbound");
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(context.replaceDynamicContentInString(selector)).thenReturn(selector);
        when(consumer.receive(selector, context, 3000L)).thenThrow(new IllegalStateException("stop after receive starts"));

        TestAppender appender = new TestAppender();
        Logger logger = (Logger) LogManager.getLogger(ReceiveMessageAction.class);
        Level previousLevel = logger.getLevel();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);

        try {
            ReceiveMessageAction action = new ReceiveMessageAction.Builder()
                    .endpoint(endpoint)
                    .selector(selector)
                    .timeout(3000L)
                    .build();

            assertThatThrownBy(() -> action.doExecute(context))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("stop after receive starts");

            assertThat(appender.messages)
                    .contains("Waiting to receive message on endpoint: 'requests.inbound' (timeout: 3000ms)");
        } finally {
            logger.removeAppender(appender);
            logger.setLevel(previousLevel);
            appender.stop();
        }
    }

    private static class TestAppender extends AbstractAppender {

        private final List<String> messages = new ArrayList<>();

        TestAppender() {
            super("receive-message-test", null, PatternLayout.createDefaultLayout(), false, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            messages.add(event.getMessage().getFormattedMessage());
        }
    }
}
