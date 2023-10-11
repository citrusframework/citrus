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

package org.citrusframework.channel;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.core.DestinationResolver;
import org.springframework.messaging.support.GenericMessage;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class ChannelEndpointSyncConsumerTest extends AbstractTestNGUnitTest {

    private final MessagingTemplate messagingTemplate = Mockito.mock(MessagingTemplate.class);

    private final PollableChannel channel = Mockito.mock(PollableChannel.class);
    private final MessageChannel replyChannel = Mockito.mock(MessageChannel.class);

    private final MessageCorrelator messageCorrelator = Mockito.mock(MessageCorrelator.class);

    private final DestinationResolver channelResolver = Mockito.mock(DestinationResolver.class);

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithReplyChannel() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);

        Map<String, Object> headers = new HashMap<String, Object>();
        final org.springframework.messaging.Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .setReplyChannel(replyChannel)
                                .build();

        reset(messagingTemplate, channel, replyChannel);

        when(messagingTemplate.receive(channel)).thenReturn(message);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive(context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.ID), message.getHeaders().getId());
        Assert.assertEquals(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL), message.getHeaders().getReplyChannel());

        MessageChannel savedReplyChannel = channelSyncConsumer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(receivedMessage),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);

        verify(messagingTemplate).setReceiveTimeout(5000L);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageChannelNameResolver() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannelName("testChannel");

        endpoint.getEndpointConfiguration().setChannelResolver(channelResolver);

        Map<String, Object> headers = new HashMap<String, Object>();
        final org.springframework.messaging.Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .setReplyChannel(replyChannel)
                                .build();

        reset(messagingTemplate, channel, replyChannel, channelResolver);

        when(channelResolver.resolveDestination("testChannel")).thenReturn(channel);

        when(messagingTemplate.receive(channel)).thenReturn(message);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive(context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.ID), message.getHeaders().getId());
        Assert.assertEquals(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL), message.getHeaders().getReplyChannel());

        MessageChannel savedReplyChannel = channelSyncConsumer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(receivedMessage),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);

        verify(messagingTemplate).setReceiveTimeout(5000L);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithReplyChannelName() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);

        ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
        Map<String, Object> headers = new HashMap<>();
        final org.springframework.messaging.Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .setReplyChannelName("replyChannel")
                                .build();

        reset(messagingTemplate, channel, replyChannel, referenceResolver);

        when(messagingTemplate.receive(channel)).thenReturn(message);
        when(referenceResolver.resolve("replyChannel", MessageChannel.class)).thenReturn(replyChannel);

        context.setReferenceResolver(referenceResolver);
        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive(context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.ID), message.getHeaders().getId());
        Assert.assertEquals(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL), "replyChannel");

        MessageChannel savedReplyChannel = channelSyncConsumer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(receivedMessage),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);

        verify(messagingTemplate).setReceiveTimeout(5000L);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithCustomTimeout() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);

        endpoint.getEndpointConfiguration().setTimeout(10000L);

        Map<String, Object> headers = new HashMap<String, Object>();
        final org.springframework.messaging.Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .setReplyChannel(replyChannel)
                                .build();

        reset(messagingTemplate, channel, replyChannel);

        when(messagingTemplate.receive(channel)).thenReturn(message);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive(context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.ID), message.getHeaders().getId());

        MessageChannel savedReplyChannel = channelSyncConsumer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(receivedMessage),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);

        verify(messagingTemplate).setReceiveTimeout(10000L);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithReplyMessageCorrelator() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);

        endpoint.getEndpointConfiguration().setCorrelator(messageCorrelator);

        endpoint.getEndpointConfiguration().setTimeout(500L);
        endpoint.getEndpointConfiguration().setPollingInterval(100);

        Map<String, Object> headers = new HashMap<String, Object>();
        final org.springframework.messaging.Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .setReplyChannel(replyChannel)
                                .build();

        reset(messagingTemplate, channel, replyChannel, messageCorrelator);

        when(messagingTemplate.receive(channel)).thenReturn(message);

        when(messageCorrelator.getCorrelationKey(any(Message.class))).thenReturn(MessageHeaders.ID + " = '123456789'");
        when(messageCorrelator.getCorrelationKeyName(any(String.class))).thenReturn("correlationKeyName");

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive(context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.ID), message.getHeaders().getId());

        Assert.assertNull(channelSyncConsumer.getCorrelationManager().find("", endpoint.getEndpointConfiguration().getTimeout()));
        Assert.assertNull(channelSyncConsumer.getCorrelationManager().find(MessageHeaders.ID + " = 'totally_wrong'",
                endpoint.getEndpointConfiguration().getTimeout()));

        MessageChannel savedReplyChannel = channelSyncConsumer.getCorrelationManager().find(MessageHeaders.ID + " = '123456789'",
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);

        verify(messagingTemplate).setReceiveTimeout(500L);
    }

    @Test
    public void testReceiveNoMessage() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);

        reset(messagingTemplate, channel, replyChannel);

        when(messagingTemplate.receive(channel)).thenReturn(null);


        try {
            ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
            channelSyncConsumer.receive(context);
        } catch(ActionTimeoutException e) {
            Assert.assertTrue(e.getLocalizedMessage().startsWith("Action timeout after 5000 milliseconds. Failed to receive message on endpoint: "));
            return;
        }

        Assert.fail("Missing " + ActionTimeoutException.class + " because no message was received");
        verify(messagingTemplate).setReceiveTimeout(5000L);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageNoReplyChannel() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);

        endpoint.getEndpointConfiguration().setTimeout(500L);
        endpoint.getEndpointConfiguration().setPollingInterval(150L);

        Map<String, Object> headers = new HashMap<String, Object>();
        final org.springframework.messaging.Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .build();

        reset(messagingTemplate, channel, replyChannel);

        when(messagingTemplate.receive(channel)).thenReturn(message);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive(context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.ID), message.getHeaders().getId());

        MessageChannel savedReplyChannel = channelSyncConsumer.getCorrelationManager().find("", endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertNull(savedReplyChannel);

        verify(messagingTemplate).setReceiveTimeout(500L);
    }

    @Test
    public void testSendReplyMessage() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(messagingTemplate, replyChannel);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        channelSyncConsumer.saveReplyMessageChannel(new DefaultMessage("").setHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL, replyChannel), context);
        channelSyncConsumer.send(message, context);

        verify(messagingTemplate).send(eq(replyChannel), any(org.springframework.messaging.Message.class));
    }

    @Test
    public void testSendReplyMessageWithReplyMessageCorrelator() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        MessageCorrelator correlator = new DefaultMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        Message request = new DefaultMessage("").setHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL, replyChannel);

        ((ChannelSyncConsumer)endpoint.createConsumer()).getCorrelationManager().saveCorrelationKey(
                endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(endpoint.createConsumer().getName()),
                request.getId(), context);

        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(messagingTemplate, replyChannel);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(((GenericMessage)invocation.getArguments()[1]).getPayload(), message.getPayload());
                return null;
            }
        }).when(messagingTemplate).send(eq(replyChannel), any(org.springframework.messaging.Message.class));

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        channelSyncConsumer.saveReplyMessageChannel(request, context);
        channelSyncConsumer.send(message, context);

    }

    @Test
    public void testSendReplyMessageWithMissingCorrelatorKey() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        MessageCorrelator correlator = new DefaultMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        try {
            ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
            channelSyncConsumer.send(message, context);
        } catch(CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Failed to get correlation key for"), e.getMessage());
            return;
        }

        Assert.fail("Missing " + IllegalArgumentException.class + " because of missing correlation key");
    }

    @Test
    public void testNoCorrelationKeyFound() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        MessageCorrelator correlator = new DefaultMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        ChannelSyncEndpoint dummyEndpoint = new ChannelSyncEndpoint();
        dummyEndpoint.setName("dummyEndpoint");
        ((ChannelSyncConsumer)dummyEndpoint.createConsumer()).getCorrelationManager().saveCorrelationKey(
                dummyEndpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(dummyEndpoint.createConsumer().getName()),
                "123456789", context);

        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        try {
            ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
            channelSyncConsumer.send(message, context);
        } catch(CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Failed to get correlation key"));
            return;
        }

        Assert.fail("Missing " + IllegalArgumentException.class + " because no reply destination found");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Failed to find reply channel for message correlation key: 123456789")
    public void testNoReplyDestinationFound() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        endpoint.getEndpointConfiguration().setTimeout(1000L);

        MessageCorrelator correlator = new DefaultMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        ((ChannelSyncConsumer)endpoint.createConsumer()).getCorrelationManager().saveCorrelationKey(
                endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(endpoint.createConsumer().getName()),
                "123456789", context);

        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        channelSyncConsumer.send(message, context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Can not send empty message")
    public void testSendEmptyMessage() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        channelSyncConsumer.send(null, context);
    }

    @Test
    public void testSendReplyMessageFail() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(messagingTemplate, replyChannel);

        doThrow(new MessageDeliveryException("Internal error!")).when(messagingTemplate).send(eq(replyChannel), any(org.springframework.messaging.Message.class));

        try {
            ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
            channelSyncConsumer.saveReplyMessageChannel(new DefaultMessage("").setHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL, replyChannel), context);
            channelSyncConsumer.send(message, context);
        } catch(CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Failed to send message to channel: "));
            Assert.assertNotNull(e.getCause());
            Assert.assertEquals(e.getCause().getClass(), MessageDeliveryException.class);
            Assert.assertEquals(e.getCause().getLocalizedMessage(), "Internal error!");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " because of message channel template returned false");
    }
}
