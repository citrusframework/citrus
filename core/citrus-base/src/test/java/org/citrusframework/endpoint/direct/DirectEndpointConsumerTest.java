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

package org.citrusframework.endpoint.direct;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.selector.DelegatingMessageSelector;
import org.citrusframework.message.selector.HeaderMatchingMessageSelector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class DirectEndpointConsumerTest {

    private MessageQueue queue = Mockito.mock(MessageQueue.class);

    private ReferenceResolver resolver = Mockito.mock(ReferenceResolver.class);

    private TestContext context;

    @BeforeEach
    public void setupMocks() {
        context = new TestContext();
    }

    @Test
    public void testReceiveMessage() {
        DirectEndpoint endpoint = new DirectEndpoint();
        endpoint.getEndpointConfiguration().setQueue(queue);

        Map<String, Object> headers = new HashMap<>();
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(queue);

        when(queue.receive(5000L)).thenReturn(message);

        Message receivedMessage = endpoint.createConsumer().receive(context);

        Assertions.assertEquals(message.getPayload(), receivedMessage.getPayload());
        Assertions.assertEquals(message.getId(), receivedMessage.getHeader(MessageHeaders.ID));
    }

    @Test
    public void testReceiveMessageQueueNameResolver() {
        DirectEndpoint endpoint = new DirectEndpoint();

        endpoint.getEndpointConfiguration().setQueueName("testQueue");

        context.setReferenceResolver(resolver);

        Map<String, Object> headers = new HashMap<>();
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(queue, resolver);

        when(resolver.resolve("testQueue", MessageQueue.class)).thenReturn(queue);

        when(queue.receive(5000L)).thenReturn(message);

        Message receivedMessage = endpoint.createConsumer().receive(context);

        Assertions.assertEquals(message.getPayload(), receivedMessage.getPayload());
        Assertions.assertEquals(message.getId(), receivedMessage.getHeader(MessageHeaders.ID));
    }

    @Test
    public void testReceiveMessageWithCustomTimeout() {
        DirectEndpoint endpoint = new DirectEndpoint();

        endpoint.getEndpointConfiguration().setQueue(queue);
        endpoint.getEndpointConfiguration().setTimeout(10000L);

        Map<String, Object> headers = new HashMap<>();
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(queue);
        when(queue.receive(10000L)).thenReturn(message);

        Message receivedMessage = endpoint.createConsumer().receive(context);

        Assertions.assertEquals(message.getPayload(), receivedMessage.getPayload());
        Assertions.assertEquals(message.getId(), receivedMessage.getHeader(MessageHeaders.ID));
    }

    @Test
    public void testReceiveMessageTimeoutOverride() {
        DirectEndpoint endpoint = new DirectEndpoint();

        endpoint.getEndpointConfiguration().setQueue(queue);
        endpoint.getEndpointConfiguration().setTimeout(10000L);

        Map<String, Object> headers = new HashMap<>();
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(queue);
        when(queue.receive(25000L)).thenReturn(message);

        Message receivedMessage = endpoint.createConsumer().receive(context, 25000L);

        Assertions.assertEquals(message.getPayload(), receivedMessage.getPayload());
        Assertions.assertEquals(message.getId(), receivedMessage.getHeader(MessageHeaders.ID));
    }

    @Test
    public void testReceiveTimeout() {
        DirectEndpoint endpoint = new DirectEndpoint();

        endpoint.getEndpointConfiguration().setQueue(queue);

        reset(queue);
        when(queue.receive(5000L)).thenReturn(null);

        try {
            endpoint.createConsumer().receive(context);
            Assertions.fail("Missing " + ActionTimeoutException.class + " because no message was received");
        } catch(ActionTimeoutException e) {
            Assertions.assertTrue(e.getLocalizedMessage().startsWith("Action timeout after 5000 milliseconds. Failed to receive message on endpoint"));
        }
    }

    @Test
    public void testReceiveSelected() {
        DirectEndpoint endpoint = new DirectEndpoint();

        endpoint.getEndpointConfiguration().setQueue(queue);
        endpoint.getEndpointConfiguration().setTimeout(0L);

        try {
            endpoint.createConsumer().receive("Operation = 'sayHello'", context);
            Assertions.fail("Missing exception due to unsupported operation");
        } catch (CitrusRuntimeException e) {
            Assertions.assertNotNull(e.getMessage());
        }

        MessageQueue queueQueue = Mockito.mock(MessageQueue.class);
        Message message = new DefaultMessage("Hello").setHeader("Operation", "sayHello");

        when(queueQueue.receive(any(DelegatingMessageSelector.class)))
                            .thenReturn(message);

        endpoint.getEndpointConfiguration().setQueue(queueQueue);
        Message receivedMessage = endpoint.createConsumer().receive("Operation = 'sayHello'", context);

        Assertions.assertEquals(message.getPayload(), receivedMessage.getPayload());
        Assertions.assertEquals(message.getId(), receivedMessage.getHeader(MessageHeaders.ID));
        Assertions.assertEquals("sayHello", receivedMessage.getHeader("Operation"));

    }

    @Test
    public void testReceiveSelectedNoMessageWithTimeout() {
        DirectEndpoint endpoint = new DirectEndpoint();

        reset(queue);
        when(queue.receive(any(HeaderMatchingMessageSelector.class), eq(1500L)))
                            .thenReturn(null); // force retry

        endpoint.getEndpointConfiguration().setQueue(queue);

        try {
            endpoint.createConsumer().receive("Operation = 'sayHello'", context, 1500L);
            Assertions.fail("Missing " + ActionTimeoutException.class + " because no message was received");
        } catch(ActionTimeoutException e) {
            Assertions.assertTrue(e.getLocalizedMessage().startsWith("Action timeout after 1500 milliseconds. Failed to receive message on endpoint"));
        }

    }
}
