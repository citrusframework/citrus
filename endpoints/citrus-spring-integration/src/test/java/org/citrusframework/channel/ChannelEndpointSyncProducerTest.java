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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class ChannelEndpointSyncProducerTest extends AbstractTestNGUnitTest {

    private MessagingTemplate messagingTemplate = Mockito.mock(MessagingTemplate.class);
    private MessageChannel channel = Mockito.mock(MessageChannel.class);
    private MessageCorrelator messageCorrelator = Mockito.mock(MessageCorrelator.class);
    private DestinationResolver channelResolver = Mockito.mock(DestinationResolver.class);

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSendMessage() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final org.springframework.messaging.Message response = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();

        reset(messagingTemplate, channel);

        when(messagingTemplate.sendAndReceive(eq(channel), any(org.springframework.messaging.Message.class))).thenReturn(response);

        endpoint.createProducer().send(message, context);

        verify(messagingTemplate).setReceiveTimeout(5000L);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSendMessageChannelNameResolver() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannelName("testChannel");

        endpoint.getEndpointConfiguration().setChannelResolver(channelResolver);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final org.springframework.messaging.Message response = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();

        reset(messagingTemplate, channel, channelResolver);

        when(channelResolver.resolveDestination("testChannel")).thenReturn(channel);
        when(messagingTemplate.sendAndReceive(eq(channel), any(org.springframework.messaging.Message.class))).thenReturn(response);

        endpoint.createProducer().send(message, context);

        verify(messagingTemplate).setReceiveTimeout(5000L);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSendMessageWithReplyHandler() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final org.springframework.messaging.Message response = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();

        reset(messagingTemplate, channel);

        when(messagingTemplate.sendAndReceive(eq(channel), any(org.springframework.messaging.Message.class))).thenReturn(response);

        ChannelSyncProducer channelSyncProducer = (ChannelSyncProducer) endpoint.createProducer();
        channelSyncProducer.send(message, context);

        Message replyMessage = channelSyncProducer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(message),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertEquals(replyMessage.getPayload(), response.getPayload());
        Assert.assertEquals(replyMessage.getHeader(org.springframework.messaging.MessageHeaders.ID), response.getHeaders().getId());

        verify(messagingTemplate).setReceiveTimeout(5000L);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSendMessageWithCustomReplyTimeout() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);

        endpoint.getEndpointConfiguration().setTimeout(10000L);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final org.springframework.messaging.Message response = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();

        reset(messagingTemplate, channel);

        when(messagingTemplate.sendAndReceive(eq(channel), any(org.springframework.messaging.Message.class))).thenReturn(response);

        ChannelSyncProducer channelSyncProducer = (ChannelSyncProducer) endpoint.createProducer();
        channelSyncProducer.send(message, context);

        Message replyMessage = channelSyncProducer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(message),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertEquals(replyMessage.getPayload(), response.getPayload());
        Assert.assertEquals(replyMessage.getHeader(org.springframework.messaging.MessageHeaders.ID), response.getHeaders().getId());

        verify(messagingTemplate).setReceiveTimeout(10000L);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSendMessageWithReplyMessageCorrelator() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final org.springframework.messaging.Message response = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();

        endpoint.getEndpointConfiguration().setCorrelator(messageCorrelator);

        reset(messagingTemplate, channel, messageCorrelator);

        when(messagingTemplate.sendAndReceive(eq(channel), any(org.springframework.messaging.Message.class))).thenReturn(response);

        when(messageCorrelator.getCorrelationKey(message)).thenReturn(MessageHeaders.ID + " = '123456789'");
        when(messageCorrelator.getCorrelationKeyName(any(String.class))).thenReturn("correlationKeyName");

        ChannelSyncProducer channelSyncProducer = (ChannelSyncProducer) endpoint.createProducer();
        channelSyncProducer.send(message, context);

        Message replyMessage = channelSyncProducer.getCorrelationManager().find(MessageHeaders.ID + " = '123456789'",
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertEquals(replyMessage.getPayload(), response.getPayload());
        Assert.assertEquals(replyMessage.getHeader(org.springframework.messaging.MessageHeaders.ID), response.getHeaders().getId());

        verify(messagingTemplate).setReceiveTimeout(5000L);
    }

    @Test
    public void testSendMessageNoResponse() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(messagingTemplate, channel);

        when(channel.toString()).thenReturn("mockChannel");
        when(messagingTemplate.sendAndReceive(eq(channel), any(org.springframework.messaging.Message.class))).thenReturn(null);

        try {
            endpoint.createProducer().send(message, context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Action timeout after 5000 milliseconds. " +
                    "Failed to receive synchronous reply message on endpoint: 'mockChannel'");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " because of reply timeout");
        verify(messagingTemplate).setReceiveTimeout(5000L);
    }

    @Test
    public void testOnReplyMessage() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        ChannelSyncProducer channelSyncProducer = (ChannelSyncProducer) endpoint.createProducer();
        channelSyncProducer.getCorrelationManager().saveCorrelationKey(
                endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(channelSyncProducer.getName()),
                channelSyncProducer.toString(), context);
        channelSyncProducer.getCorrelationManager().store(channelSyncProducer.toString(), message);

        Assert.assertEquals(channelSyncProducer.receive(context), message);
    }

    @Test
    public void testOnReplyMessageWithCorrelatorKey() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        ChannelSyncProducer channelSyncProducer = (ChannelSyncProducer) endpoint.createProducer();
        channelSyncProducer.getCorrelationManager().store(new DefaultMessageCorrelator().getCorrelationKey(message), message);

        Assert.assertEquals(channelSyncProducer.receive(new DefaultMessageCorrelator().getCorrelationKey(message), context), message);
    }

}
