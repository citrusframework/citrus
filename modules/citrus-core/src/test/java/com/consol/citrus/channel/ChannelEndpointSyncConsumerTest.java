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

package com.consol.citrus.channel;

import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.support.channel.HeaderChannelRegistry;
import org.springframework.messaging.*;
import org.springframework.messaging.core.DestinationResolver;
import org.springframework.messaging.support.GenericMessage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class ChannelEndpointSyncConsumerTest extends AbstractTestNGUnitTest {

    private MessagingTemplate messagingTemplate = EasyMock.createMock(MessagingTemplate.class);
    
    private PollableChannel channel = EasyMock.createMock(PollableChannel.class);
    private MessageChannel replyChannel = EasyMock.createMock(MessageChannel.class);

    private MessageCorrelator messageCorrelator = EasyMock.createMock(MessageCorrelator.class);
    
    private DestinationResolver channelResolver = EasyMock.createMock(DestinationResolver.class);
    
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
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(message).once();
        
        replay(messagingTemplate, channel, replyChannel);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive(context);
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.ID), message.getHeaders().getId());
        Assert.assertEquals(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL), message.getHeaders().getReplyChannel());
        
        MessageChannel savedReplyChannel = channelSyncConsumer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(receivedMessage),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);
        
        verify(messagingTemplate, channel, replyChannel);
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
        
        expect(channelResolver.resolveDestination("testChannel")).andReturn(channel).once();
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(message).once();
        
        replay(messagingTemplate, channel, replyChannel, channelResolver);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive(context);
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.ID), message.getHeaders().getId());
        Assert.assertEquals(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL), message.getHeaders().getReplyChannel());
        
        MessageChannel savedReplyChannel = channelSyncConsumer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(receivedMessage),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);
        
        verify(messagingTemplate, channel, replyChannel, channelResolver);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithReplyChannelName() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);
        
        BeanFactory factory = EasyMock.createMock(BeanFactory.class);
        endpoint.getEndpointConfiguration().setBeanFactory(factory);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final org.springframework.messaging.Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .setReplyChannelName("replyChannel")
                                .build();

        reset(messagingTemplate, channel, replyChannel, factory);
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(message).once();
        
        expect(factory.getBean("replyChannel", MessageChannel.class)).andReturn(replyChannel).once();
        expect(factory.getBean(IntegrationContextUtils.INTEGRATION_HEADER_CHANNEL_REGISTRY_BEAN_NAME, HeaderChannelRegistry.class))
                .andThrow(new NoSuchBeanDefinitionException(IntegrationContextUtils.INTEGRATION_HEADER_CHANNEL_REGISTRY_BEAN_NAME)).once();
        
        replay(messagingTemplate, channel, replyChannel, factory);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive(context);
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.ID), message.getHeaders().getId());
        Assert.assertEquals(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL), "replyChannel");
        
        MessageChannel savedReplyChannel = channelSyncConsumer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(receivedMessage),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);
        
        verify(messagingTemplate, channel, replyChannel, factory);
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
        
        messagingTemplate.setReceiveTimeout(10000L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(message).once();
        
        replay(messagingTemplate, channel, replyChannel);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive(context);
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.ID), message.getHeaders().getId());
        
        MessageChannel savedReplyChannel = channelSyncConsumer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(receivedMessage),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);
        
        verify(messagingTemplate, channel, replyChannel);
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
        
        messagingTemplate.setReceiveTimeout(500L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(message).once();
        
        expect(messageCorrelator.getCorrelationKey(anyObject(Message.class))).andReturn(MessageHeaders.ID + " = '123456789'").once();
        expect(messageCorrelator.getCorrelationKeyName(anyObject(String.class))).andReturn("correlationKeyName").once();

        replay(messagingTemplate, channel, replyChannel, messageCorrelator);

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
        
        verify(messagingTemplate, channel, replyChannel, messageCorrelator);
    }
    
    @Test
    public void testReceiveNoMessage() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);
        
        reset(messagingTemplate, channel, replyChannel);
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(null).once();
        
        replay(messagingTemplate, channel, replyChannel);
        
        try {
            ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
            channelSyncConsumer.receive(context);
        } catch(ActionTimeoutException e) {
            Assert.assertTrue(e.getLocalizedMessage().startsWith("Action timeout while receiving message from channel"));
            verify(messagingTemplate, channel, replyChannel);
            return;
        }
        
        Assert.fail("Missing " + ActionTimeoutException.class + " because no message was received");
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
        
        messagingTemplate.setReceiveTimeout(500L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(message).once();
        
        replay(messagingTemplate, channel, replyChannel);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive(context);
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.ID), message.getHeaders().getId());
        
        MessageChannel savedReplyChannel = channelSyncConsumer.getCorrelationManager().find("", endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertNull(savedReplyChannel);
        
        verify(messagingTemplate, channel, replyChannel);
    }

    @Test
    public void testSendReplyMessage() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(messagingTemplate, replyChannel);

        messagingTemplate.send(eq(replyChannel), anyObject(org.springframework.messaging.Message.class));
        expectLastCall().once();

        replay(messagingTemplate, replyChannel);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        channelSyncConsumer.saveReplyMessageChannel(new DefaultMessage("").setHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL, replyChannel), context);
        channelSyncConsumer.send(message, context);

        verify(messagingTemplate, replyChannel);
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

        messagingTemplate.send(eq(replyChannel), anyObject(org.springframework.messaging.Message.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Assert.assertEquals(((GenericMessage)getCurrentArguments()[1]).getPayload(), message.getPayload());
                return null;
            }
        }).once();

        replay(messagingTemplate, replyChannel);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        channelSyncConsumer.saveReplyMessageChannel(request, context);
        channelSyncConsumer.send(message, context);

        verify(messagingTemplate, replyChannel);
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

    @Test
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

        try {
            ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
            channelSyncConsumer.send(message, context);
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Failed to find reply channel"));
            return;
        }

        Assert.fail("Missing " + IllegalArgumentException.class + " because no reply destination found");
    }

    @Test
    public void testSendEmptyMessage() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        try {
            ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
            channelSyncConsumer.send(null, context);
        } catch(IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Can not send empty message");
            return;
        }

        Assert.fail("Missing " + IllegalArgumentException.class + " because of sending empty message");
    }

    @Test
    public void testSendReplyMessageFail() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(messagingTemplate, replyChannel);

        messagingTemplate.send(eq(replyChannel), anyObject(org.springframework.messaging.Message.class));
        expectLastCall().andThrow(new MessageDeliveryException("Internal error!")).once();

        replay(messagingTemplate, replyChannel);

        try {
            ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
            channelSyncConsumer.saveReplyMessageChannel(new DefaultMessage("").setHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL, replyChannel), context);
            channelSyncConsumer.send(message, context);
        } catch(CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Failed to send message to channel: "));
            Assert.assertNotNull(e.getCause());
            Assert.assertEquals(e.getCause().getClass(), MessageDeliveryException.class);
            Assert.assertEquals(e.getCause().getLocalizedMessage(), "Internal error!");
            verify(messagingTemplate, replyChannel);

            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " because of message channel template returned false");
    }
}
