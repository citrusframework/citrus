/*
 * Copyright 2006-2010 the original author or authors.
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
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.message.MessageQueue;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class DirectEndpointSyncConsumerTest {

    private final MessageQueue queue = Mockito.mock(MessageQueue.class);
    private final MessageQueue replyQueue = Mockito.mock(MessageQueue.class);

    private final MessageCorrelator messageCorrelator = Mockito.mock(MessageCorrelator.class);

    private final ReferenceResolver resolver = Mockito.mock(ReferenceResolver.class);

    private TestContext context;

    @BeforeMethod
    public void setupMocks() {
        context = new TestContext();
    }

    @Test
    public void testReceiveMessageWithReplyQueue() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();
        endpoint.getEndpointConfiguration().setQueue(queue);

        Map<String, Object> headers = new HashMap<>();
        final Message message = new DefaultMessage("<TestResponse>Hello World!</TestResponse>", headers)
                                .setHeader(DirectMessageHeaders.REPLY_QUEUE, replyQueue);

        reset(queue, replyQueue);

        when(queue.receive(5000L)).thenReturn(message);

        DirectSyncConsumer channelSyncConsumer = (DirectSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive(context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(MessageHeaders.ID), message.getId());
        Assert.assertEquals(receivedMessage.getHeader(DirectMessageHeaders.REPLY_QUEUE), message.getHeader(DirectMessageHeaders.REPLY_QUEUE));

        MessageQueue savedReplyQueue = channelSyncConsumer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(receivedMessage),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertNotNull(savedReplyQueue);
        Assert.assertEquals(savedReplyQueue, replyQueue);
    }

    @Test
    public void testReceiveMessageQueueNameResolver() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();
        endpoint.getEndpointConfiguration().setQueueName("testQueue");

        context.setReferenceResolver(resolver);

        Map<String, Object> headers = new HashMap<>();
        final Message message = new DefaultMessage("<TestResponse>Hello World!</TestResponse>", headers)
                                .setHeader(DirectMessageHeaders.REPLY_QUEUE, replyQueue);

        reset(queue, replyQueue, resolver);

        when(resolver.resolve("testQueue", MessageQueue.class)).thenReturn(queue);

        when(queue.receive(5000L)).thenReturn(message);

        DirectSyncConsumer channelSyncConsumer = (DirectSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive(context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(MessageHeaders.ID), message.getId());
        Assert.assertEquals(receivedMessage.getHeader(DirectMessageHeaders.REPLY_QUEUE), message.getHeader(DirectMessageHeaders.REPLY_QUEUE));

        MessageQueue savedReplyQueue = channelSyncConsumer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(receivedMessage),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertNotNull(savedReplyQueue);
        Assert.assertEquals(savedReplyQueue, replyQueue);
    }

    @Test
    public void testReceiveMessageWithReplyQueueName() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();
        endpoint.getEndpointConfiguration().setQueue(queue);

        Map<String, Object> headers = new HashMap<>();
        final Message message = new DefaultMessage("<TestResponse>Hello World!</TestResponse>", headers)
                                .setHeader(DirectMessageHeaders.REPLY_QUEUE, "replyQueue");

        context.setReferenceResolver(resolver);

        reset(queue, replyQueue, resolver);

        when(queue.receive(5000L)).thenReturn(message);
        when(resolver.resolve("replyQueue", MessageQueue.class)).thenReturn(replyQueue);
        DirectSyncConsumer channelSyncConsumer = (DirectSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive(context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(MessageHeaders.ID), message.getId());
        Assert.assertEquals(receivedMessage.getHeader(DirectMessageHeaders.REPLY_QUEUE), "replyQueue");

        MessageQueue savedReplyQueue = channelSyncConsumer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(receivedMessage),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertNotNull(savedReplyQueue);
        Assert.assertEquals(savedReplyQueue, replyQueue);
    }

    @Test
    public void testReceiveMessageWithCustomTimeout() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();
        endpoint.getEndpointConfiguration().setQueue(queue);

        endpoint.getEndpointConfiguration().setTimeout(10000L);

        Map<String, Object> headers = new HashMap<>();
        final Message message = new DefaultMessage("<TestResponse>Hello World!</TestResponse>", headers)
                                .setHeader(DirectMessageHeaders.REPLY_QUEUE, replyQueue);

        reset(queue, replyQueue);

        when(queue.receive(10000L)).thenReturn(message);

        DirectSyncConsumer channelSyncConsumer = (DirectSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive(context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(MessageHeaders.ID), message.getId());

        MessageQueue savedReplyQueue = channelSyncConsumer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(receivedMessage),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertNotNull(savedReplyQueue);
        Assert.assertEquals(savedReplyQueue, replyQueue);
    }

    @Test
    public void testReceiveMessageWithReplyMessageCorrelator() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();
        endpoint.getEndpointConfiguration().setQueue(queue);

        endpoint.getEndpointConfiguration().setCorrelator(messageCorrelator);

        endpoint.getEndpointConfiguration().setTimeout(500L);
        endpoint.getEndpointConfiguration().setPollingInterval(100);

        Map<String, Object> headers = new HashMap<>();
        final Message message = new DefaultMessage("<TestResponse>Hello World!</TestResponse>", headers)
                                .setHeader(DirectMessageHeaders.REPLY_QUEUE, replyQueue);

        reset(queue, replyQueue, messageCorrelator);

        when(queue.receive(500L)).thenReturn(message);

        when(messageCorrelator.getCorrelationKey(any(Message.class))).thenReturn(MessageHeaders.ID + " = '123456789'");
        when(messageCorrelator.getCorrelationKeyName(any(String.class))).thenReturn("correlationKeyName");

        DirectSyncConsumer channelSyncConsumer = (DirectSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive(context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(MessageHeaders.ID), message.getId());

        Assert.assertNull(channelSyncConsumer.getCorrelationManager().find("", endpoint.getEndpointConfiguration().getTimeout()));
        Assert.assertNull(channelSyncConsumer.getCorrelationManager().find(MessageHeaders.ID + " = 'totally_wrong'",
                endpoint.getEndpointConfiguration().getTimeout()));

        MessageQueue savedReplyQueue = channelSyncConsumer.getCorrelationManager().find(MessageHeaders.ID + " = '123456789'",
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertNotNull(savedReplyQueue);
        Assert.assertEquals(savedReplyQueue, replyQueue);
    }

    @Test
    public void testReceiveNoMessage() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();
        endpoint.getEndpointConfiguration().setQueue(queue);

        reset(queue, replyQueue);

        when(queue.receive(5000L)).thenReturn(null);


        try {
            DirectSyncConsumer channelSyncConsumer = (DirectSyncConsumer) endpoint.createConsumer();
            channelSyncConsumer.receive(context);
        } catch(ActionTimeoutException e) {
            Assert.assertTrue(e.getLocalizedMessage().startsWith("Action timeout after 5000 milliseconds. Failed to receive message on endpoint"));
            return;
        }

        Assert.fail("Missing " + ActionTimeoutException.class + " because no message was received");
    }

    @Test
    public void testReceiveMessageNoReplyQueue() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();
        endpoint.getEndpointConfiguration().setQueue(queue);

        endpoint.getEndpointConfiguration().setTimeout(500L);
        endpoint.getEndpointConfiguration().setPollingInterval(150L);

        Map<String, Object> headers = new HashMap<>();
        final Message message = new DefaultMessage("<TestResponse>Hello World!</TestResponse>", headers);

        reset(queue, replyQueue);

        when(queue.receive(500L)).thenReturn(message);

        DirectSyncConsumer channelSyncConsumer = (DirectSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive(context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(MessageHeaders.ID), message.getId());

        MessageQueue savedReplyQueue = channelSyncConsumer.getCorrelationManager().find("", endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertNull(savedReplyQueue);
    }

    @Test
    public void testSendReplyMessage() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(replyQueue);

        DirectSyncConsumer channelSyncConsumer = (DirectSyncConsumer) endpoint.createConsumer();
        channelSyncConsumer.saveReplyMessageQueue(new DefaultMessage("").setHeader(DirectMessageHeaders.REPLY_QUEUE, replyQueue), context);
        channelSyncConsumer.send(message, context);

        verify(replyQueue).send(any(Message.class));
    }

    @Test
    public void testSendReplyMessageWithReplyMessageCorrelator() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();

        MessageCorrelator correlator = new DefaultMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        Message request = new DefaultMessage("").setHeader(DirectMessageHeaders.REPLY_QUEUE, replyQueue);

        ((DirectSyncConsumer)endpoint.createConsumer()).getCorrelationManager().saveCorrelationKey(
                endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(endpoint.createConsumer().getName()),
                request.getId(), context);

        Map<String, Object> headers = new HashMap<>();
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(replyQueue);

        doAnswer(invocation -> {
            Assert.assertEquals(((DefaultMessage)invocation.getArguments()[0]).getPayload(), message.getPayload());
            return null;
        }).when(replyQueue).send(any(Message.class));

        DirectSyncConsumer channelSyncConsumer = (DirectSyncConsumer) endpoint.createConsumer();
        channelSyncConsumer.saveReplyMessageQueue(request, context);
        channelSyncConsumer.send(message, context);

    }

    @Test
    public void testSendReplyMessageWithMissingCorrelatorKey() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();

        MessageCorrelator correlator = new DefaultMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        try {
            DirectSyncConsumer channelSyncConsumer = (DirectSyncConsumer) endpoint.createConsumer();
            channelSyncConsumer.send(message, context);
        } catch(CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Failed to get correlation key for"), e.getMessage());
            return;
        }

        Assert.fail("Missing " + IllegalArgumentException.class + " because of missing correlation key");
    }

    @Test
    public void testNoCorrelationKeyFound() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();

        MessageCorrelator correlator = new DefaultMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        DirectSyncEndpoint dummyEndpoint = new DirectSyncEndpoint();
        dummyEndpoint.setName("dummyEndpoint");
        ((DirectSyncConsumer)dummyEndpoint.createConsumer()).getCorrelationManager().saveCorrelationKey(
                dummyEndpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(dummyEndpoint.createConsumer().getName()),
                "123456789", context);

        Map<String, Object> headers = new HashMap<>();
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        try {
            DirectSyncConsumer channelSyncConsumer = (DirectSyncConsumer) endpoint.createConsumer();
            channelSyncConsumer.send(message, context);
        } catch(CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Failed to get correlation key"));
            return;
        }

        Assert.fail("Missing " + IllegalArgumentException.class + " because no reply destination found");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Failed to find reply channel for message correlation key: 123456789")
    public void testNoReplyDestinationFound() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();

        endpoint.getEndpointConfiguration().setTimeout(1000L);

        MessageCorrelator correlator = new DefaultMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        ((DirectSyncConsumer)endpoint.createConsumer()).getCorrelationManager().saveCorrelationKey(
                endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(endpoint.createConsumer().getName()),
                "123456789", context);

        Map<String, Object> headers = new HashMap<>();
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        DirectSyncConsumer channelSyncConsumer = (DirectSyncConsumer) endpoint.createConsumer();
        channelSyncConsumer.send(message, context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Can not send empty message")
    public void testSendEmptyMessage() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();
        DirectSyncConsumer channelSyncConsumer = (DirectSyncConsumer) endpoint.createConsumer();
        channelSyncConsumer.send(null, context);
    }

    @Test
    public void testSendReplyMessageFail() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(replyQueue);

        doThrow(new CitrusRuntimeException("Internal error!")).when(replyQueue).send(any(Message.class));

        try {
            DirectSyncConsumer channelSyncConsumer = (DirectSyncConsumer) endpoint.createConsumer();
            channelSyncConsumer.saveReplyMessageQueue(new DefaultMessage("").setHeader(DirectMessageHeaders.REPLY_QUEUE, replyQueue), context);
            channelSyncConsumer.send(message, context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getClass(), CitrusRuntimeException.class);
            Assert.assertEquals(e.getLocalizedMessage(), "Internal error!");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " because of message channel template returned false");
    }
}
