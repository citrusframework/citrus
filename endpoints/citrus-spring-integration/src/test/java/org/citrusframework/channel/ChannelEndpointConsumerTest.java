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

import org.citrusframework.channel.selector.DispatchingMessageSelector;
import org.citrusframework.channel.selector.HeaderMatchingMessageSelector;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.PollableChannel;
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
public class ChannelEndpointConsumerTest extends AbstractTestNGUnitTest {

    private MessagingTemplate messagingTemplate = Mockito.mock(MessagingTemplate.class);

    private PollableChannel channel = Mockito.mock(PollableChannel.class);

    private DestinationResolver channelResolver = Mockito.mock(DestinationResolver.class);

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessage() {
        ChannelEndpoint endpoint = new ChannelEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        endpoint.getEndpointConfiguration().setChannel(channel);

        Map<String, Object> headers = new HashMap<String, Object>();
        final org.springframework.messaging.Message message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();

        reset(messagingTemplate, channel);

        when(messagingTemplate.receive(channel)).thenReturn(message);

        Message receivedMessage = endpoint.createConsumer().receive(context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(MessageHeaders.ID), message.getHeaders().getId());
        verify(messagingTemplate).setReceiveTimeout(5000L);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageChannelNameResolver() {
        ChannelEndpoint endpoint = new ChannelEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        endpoint.getEndpointConfiguration().setChannelName("testChannel");

        endpoint.getEndpointConfiguration().setChannelResolver(channelResolver);

        Map<String, Object> headers = new HashMap<String, Object>();
        final org.springframework.messaging.Message message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();

        reset(messagingTemplate, channel, channelResolver);

        when(channelResolver.resolveDestination("testChannel")).thenReturn(channel);

        when(messagingTemplate.receive(channel)).thenReturn(message);

        Message receivedMessage = endpoint.createConsumer().receive(context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(MessageHeaders.ID), message.getHeaders().getId());

        verify(messagingTemplate).setReceiveTimeout(5000L);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithCustomTimeout() {
        ChannelEndpoint endpoint = new ChannelEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        endpoint.getEndpointConfiguration().setChannel(channel);
        endpoint.getEndpointConfiguration().setTimeout(10000L);

        Map<String, Object> headers = new HashMap<String, Object>();
        final org.springframework.messaging.Message message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();

        reset(messagingTemplate, channel);
        when(messagingTemplate.receive(channel)).thenReturn(message);

        Message receivedMessage = endpoint.createConsumer().receive(context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(MessageHeaders.ID), message.getHeaders().getId());

        verify(messagingTemplate).setReceiveTimeout(10000L);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageTimeoutOverride() {
        ChannelEndpoint endpoint = new ChannelEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        endpoint.getEndpointConfiguration().setChannel(channel);
        endpoint.getEndpointConfiguration().setTimeout(10000L);

        Map<String, Object> headers = new HashMap<String, Object>();
        final org.springframework.messaging.Message message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();

        reset(messagingTemplate, channel);
        when(messagingTemplate.receive(channel)).thenReturn(message);

        Message receivedMessage = endpoint.createConsumer().receive(context, 25000L);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(MessageHeaders.ID), message.getHeaders().getId());

        verify(messagingTemplate).setReceiveTimeout(25000L);
    }

    @Test
    public void testReceiveTimeout() {
        ChannelEndpoint endpoint = new ChannelEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        endpoint.getEndpointConfiguration().setChannel(channel);

        reset(messagingTemplate, channel);
        when(messagingTemplate.receive(channel)).thenReturn(null);

        try {
            endpoint.createConsumer().receive(context);
            Assert.fail("Missing " + ActionTimeoutException.class + " because no message was received");
        } catch(ActionTimeoutException e) {
            Assert.assertTrue(e.getLocalizedMessage().startsWith("Action timeout after 5000 milliseconds. Failed to receive message on endpoint"));
        }

        verify(messagingTemplate).setReceiveTimeout(5000L);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveSelected() {
        ChannelEndpoint endpoint = new ChannelEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        endpoint.getEndpointConfiguration().setChannel(channel);
        endpoint.getEndpointConfiguration().setTimeout(0L);

        try {
            endpoint.createConsumer().receive("Operation = 'sayHello'", context);
            Assert.fail("Missing exception due to unsupported operation");
        } catch (CitrusRuntimeException e) {
            Assert.assertNotNull(e.getMessage());
        }

        MessageSelectingQueueChannel queueChannel = Mockito.mock(MessageSelectingQueueChannel.class);
        org.springframework.messaging.Message message = MessageBuilder.withPayload("Hello").setHeader("Operation", "sayHello").build();

        when(queueChannel.receive(any(DispatchingMessageSelector.class)))
                            .thenReturn(message);

        endpoint.getEndpointConfiguration().setChannel(queueChannel);
        Message receivedMessage = endpoint.createConsumer().receive("Operation = 'sayHello'", context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(MessageHeaders.ID), message.getHeaders().getId());
        Assert.assertEquals(receivedMessage.getHeader("Operation"), "sayHello");

    }

    @Test
    public void testReceiveSelectedNoMessageWithTimeout() {
        ChannelEndpoint endpoint = new ChannelEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        MessageSelectingQueueChannel queueChannel = Mockito.mock(MessageSelectingQueueChannel.class);

        reset(queueChannel);

        when(queueChannel.receive(any(HeaderMatchingMessageSelector.class), eq(1500L)))
                            .thenReturn(null); // force retry

        endpoint.getEndpointConfiguration().setChannel(queueChannel);

        try {
            endpoint.createConsumer().receive("Operation = 'sayHello'", context, 1500L);
            Assert.fail("Missing " + ActionTimeoutException.class + " because no message was received");
        } catch(ActionTimeoutException e) {
            Assert.assertTrue(e.getLocalizedMessage().startsWith("Action timeout after 1500 milliseconds. Failed to receive message on endpoint: "));
        }

    }
}
