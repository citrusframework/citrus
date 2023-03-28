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
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.DefaultMessageQueue;
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
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class DirectEndpointSyncProducerTest {

    private MessageQueue queue = Mockito.mock(MessageQueue.class);
    private MessageCorrelator messageCorrelator = Mockito.mock(MessageCorrelator.class);
    private ReferenceResolver resolver = Mockito.mock(ReferenceResolver.class);

    private TestContext context;

    @BeforeMethod
    public void setupMocks() {
        context = new TestContext();
    }

    @Test
    public void testSendMessage() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();
        endpoint.getEndpointConfiguration().setQueue(queue);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> responseHeaders = new HashMap<>();
        final Message response = new DefaultMessage("<TestResponse>Hello World!</TestResponse>", responseHeaders);

        reset(queue);
        doAnswer(invocation -> {
            Message request = invocation.getArgument(0);
            Assert.assertNotNull(request.getHeaders().get(DirectMessageHeaders.REPLY_QUEUE));
            ((MessageQueue) request.getHeaders().get(DirectMessageHeaders.REPLY_QUEUE)).send(response);
            return null;
        }).when(queue).send(any(Message.class));

        endpoint.createProducer().send(message, context);
    }

    @Test
    public void testSendMessageCustomReplyQueue() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();
        endpoint.getEndpointConfiguration().setQueue(queue);

        final MessageQueue replyQueue = new DefaultMessageQueue("testQueue");
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader(DirectMessageHeaders.REPLY_QUEUE, replyQueue);

        Map<String, Object> responseHeaders = new HashMap<>();
        final Message response = new DefaultMessage("<TestResponse>Hello World!</TestResponse>", responseHeaders);

        reset(queue);
        doAnswer(invocation -> {
            Message request = invocation.getArgument(0);
            Assert.assertNotNull(request.getHeaders().get(DirectMessageHeaders.REPLY_QUEUE));
            Assert.assertEquals(request.getHeaders().get(DirectMessageHeaders.REPLY_QUEUE), replyQueue);
            replyQueue.send(response);
            return null;
        }).when(queue).send(any(Message.class));

        endpoint.createProducer().send(message, context);
    }

    @Test
    public void testSendMessageQueueNameResolver() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();
        endpoint.getEndpointConfiguration().setQueueName("testQueue");

        context.setReferenceResolver(resolver);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> responseHeaders = new HashMap<>();
        final Message response = new DefaultMessage("<TestResponse>Hello World!</TestResponse>", responseHeaders);

        reset(queue, resolver);

        when(resolver.resolve("testQueue", MessageQueue.class)).thenReturn(queue);
        doAnswer(invocation -> {
            Message request = invocation.getArgument(0);
            Assert.assertNotNull(request.getHeaders().get(DirectMessageHeaders.REPLY_QUEUE));
            ((MessageQueue) request.getHeaders().get(DirectMessageHeaders.REPLY_QUEUE)).send(response);
            return null;
        }).when(queue).send(any(Message.class));

        endpoint.createProducer().send(message, context);
    }

    @Test
    public void testSendMessageWithReplyHandler() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();
        endpoint.getEndpointConfiguration().setQueue(queue);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> responseHeaders = new HashMap<>();
        final Message response = new DefaultMessage("<TestResponse>Hello World!</TestResponse>", responseHeaders);

        reset(queue);
        doAnswer(invocation -> {
            Message request = invocation.getArgument(0);
            Assert.assertNotNull(request.getHeaders().get(DirectMessageHeaders.REPLY_QUEUE));
            ((MessageQueue) request.getHeaders().get(DirectMessageHeaders.REPLY_QUEUE)).send(response);
            return null;
        }).when(queue).send(any(Message.class));

        DirectSyncProducer channelSyncProducer = (DirectSyncProducer) endpoint.createProducer();
        channelSyncProducer.send(message, context);

        Message replyMessage = channelSyncProducer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(message),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertEquals(replyMessage.getPayload(), response.getPayload());
        Assert.assertEquals(replyMessage.getHeader(MessageHeaders.ID), response.getId());
    }

    @Test
    public void testSendMessageWithCustomReplyTimeout() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();
        endpoint.getEndpointConfiguration().setQueue(queue);

        endpoint.getEndpointConfiguration().setTimeout(10000L);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> responseHeaders = new HashMap<>();
        final Message response = new DefaultMessage("<TestResponse>Hello World!</TestResponse>", responseHeaders);

        reset(queue);
        doAnswer(invocation -> {
            Message request = invocation.getArgument(0);
            Assert.assertNotNull(request.getHeaders().get(DirectMessageHeaders.REPLY_QUEUE));
            ((MessageQueue) request.getHeaders().get(DirectMessageHeaders.REPLY_QUEUE)).send(response);
            return null;
        }).when(queue).send(any(Message.class));

        DirectSyncProducer channelSyncProducer = (DirectSyncProducer) endpoint.createProducer();
        channelSyncProducer.send(message, context);

        Message replyMessage = channelSyncProducer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(message),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertEquals(replyMessage.getPayload(), response.getPayload());
        Assert.assertEquals(replyMessage.getHeader(MessageHeaders.ID), response.getId());
    }

    @Test
    public void testSendMessageWithReplyMessageCorrelator() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();
        endpoint.getEndpointConfiguration().setQueue(queue);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> responseHeaders = new HashMap<>();
        final Message response = new DefaultMessage("<TestResponse>Hello World!</TestResponse>", responseHeaders);

        endpoint.getEndpointConfiguration().setCorrelator(messageCorrelator);

        reset(queue, messageCorrelator);
        doAnswer(invocation -> {
            Message request = invocation.getArgument(0);
            Assert.assertNotNull(request.getHeaders().get(DirectMessageHeaders.REPLY_QUEUE));
            ((MessageQueue) request.getHeaders().get(DirectMessageHeaders.REPLY_QUEUE)).send(response);
            return null;
        }).when(queue).send(any(Message.class));

        when(messageCorrelator.getCorrelationKey(message)).thenReturn(MessageHeaders.ID + " = '123456789'");
        when(messageCorrelator.getCorrelationKeyName(any(String.class))).thenReturn("correlationKeyName");

        DirectSyncProducer channelSyncProducer = (DirectSyncProducer) endpoint.createProducer();
        channelSyncProducer.send(message, context);

        Message replyMessage = channelSyncProducer.getCorrelationManager().find(MessageHeaders.ID + " = '123456789'",
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertEquals(replyMessage.getPayload(), response.getPayload());
        Assert.assertEquals(replyMessage.getHeader(MessageHeaders.ID), response.getId());
    }

    @Test
    public void testSendMessageNoResponse() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();
        endpoint.getEndpointConfiguration().setQueue(queue);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(queue);
        when(queue.toString()).thenReturn("mockQueue");
        doAnswer(invocation -> {
            Message request = invocation.getArgument(0);
            Assert.assertNotNull(request.getHeaders().get(DirectMessageHeaders.REPLY_QUEUE));
            return null;
        }).when(queue).send(any(Message.class));

        try {
            endpoint.createProducer().send(message, context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Action timeout after 5000 milliseconds. " +
                    "Failed to receive synchronous reply message on endpoint: 'mockQueue'");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " because of reply timeout");
    }

    @Test
    public void testOnReplyMessage() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        DirectSyncProducer channelSyncProducer = (DirectSyncProducer) endpoint.createProducer();
        channelSyncProducer.getCorrelationManager().saveCorrelationKey(
                endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(channelSyncProducer.getName()),
                channelSyncProducer.toString(), context);
        channelSyncProducer.getCorrelationManager().store(channelSyncProducer.toString(), message);

        Assert.assertEquals(channelSyncProducer.receive(context), message);
    }

    @Test
    public void testOnReplyMessageWithCorrelatorKey() {
        DirectSyncEndpoint endpoint = new DirectSyncEndpoint();

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        DirectSyncProducer channelSyncProducer = (DirectSyncProducer) endpoint.createProducer();
        channelSyncProducer.getCorrelationManager().store(new DefaultMessageCorrelator().getCorrelationKey(message), message);

        Assert.assertEquals(channelSyncProducer.receive(new DefaultMessageCorrelator().getCorrelationKey(message), context), message);
    }

}
