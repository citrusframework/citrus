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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class ChannelEndpointSyncConsumerTest {

    private MessagingTemplate messagingTemplate = EasyMock.createMock(MessagingTemplate.class);
    
    private PollableChannel channel = EasyMock.createMock(PollableChannel.class);
    private MessageChannel replyChannel = EasyMock.createMock(MessageChannel.class);

    private ReplyMessageCorrelator replyMessageCorrelator = EasyMock.createMock(ReplyMessageCorrelator.class);
    
    private DestinationResolver channelResolver = EasyMock.createMock(DestinationResolver.class);
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithReplyChannel() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .setReplyChannel(replyChannel)
                                .build();

        reset(messagingTemplate, channel, replyChannel);
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(message).once();
        
        replay(messagingTemplate, channel, replyChannel);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        
        MessageChannel savedReplyChannel = channelSyncConsumer.findReplyChannel("");
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
        final Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
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
        Message receivedMessage = channelSyncConsumer.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        
        MessageChannel savedReplyChannel = channelSyncConsumer.findReplyChannel("");
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
        final Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
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
        Message receivedMessage = channelSyncConsumer.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        
        MessageChannel savedReplyChannel = channelSyncConsumer.findReplyChannel("");
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
        final Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .setReplyChannel(replyChannel)
                                .build();

        reset(messagingTemplate, channel, replyChannel);
        
        messagingTemplate.setReceiveTimeout(10000L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(message).once();
        
        replay(messagingTemplate, channel, replyChannel);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        
        MessageChannel savedReplyChannel = channelSyncConsumer.findReplyChannel("");
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

        endpoint.getEndpointConfiguration().setCorrelator(replyMessageCorrelator);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .setReplyChannel(replyChannel)
                                .build();

        reset(messagingTemplate, channel, replyChannel, replyMessageCorrelator);
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(message).once();
        
        expect(replyMessageCorrelator.getCorrelationKey(message)).andReturn(MessageHeaders.ID + " = '123456789'").once();
        
        replay(messagingTemplate, channel, replyChannel, replyMessageCorrelator);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        
        Assert.assertNull(channelSyncConsumer.findReplyChannel(""));
        Assert.assertNull(channelSyncConsumer.findReplyChannel(MessageHeaders.ID + " = 'totally_wrong'"));
        
        MessageChannel savedReplyChannel = channelSyncConsumer.findReplyChannel(MessageHeaders.ID + " = '123456789'");
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);
        
        verify(messagingTemplate, channel, replyChannel, replyMessageCorrelator);
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
            channelSyncConsumer.receive();
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
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .build();

        reset(messagingTemplate, channel, replyChannel);
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(message).once();
        
        replay(messagingTemplate, channel, replyChannel);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = channelSyncConsumer.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        
        MessageChannel savedReplyChannel = channelSyncConsumer.findReplyChannel("");
        Assert.assertNull(savedReplyChannel);
        
        verify(messagingTemplate, channel, replyChannel);
    }

    @Test
    public void testSendReplyMessage() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .copyHeaders(headers)
                .build();

        reset(messagingTemplate, replyChannel);

        messagingTemplate.send(replyChannel, message);
        expectLastCall().once();

        replay(messagingTemplate, replyChannel);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        channelSyncConsumer.saveReplyMessageChannel(MessageBuilder.withPayload("").setReplyChannel(replyChannel).build());
        channelSyncConsumer.send(message);

        verify(messagingTemplate, replyChannel);
    }

    @Test
    public void testSendReplyMessageWithReplyMessageCorrelator() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        ReplyMessageCorrelator correlator = new DefaultReplyMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        Message<String> request = MessageBuilder.withPayload("").setReplyChannel(replyChannel).build();

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR, request.getHeaders().getId());
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .copyHeaders(headers)
                .build();

        final Message<String> sentMessage = MessageBuilder.fromMessage(message)
                .removeHeader(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR)
                .build();

        reset(messagingTemplate, replyChannel);

        messagingTemplate.send(eq(replyChannel), (Message<?>)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Assert.assertEquals(((Message<?>)getCurrentArguments()[1]).getPayload(), sentMessage.getPayload());
                return null;
            }
        }).once();

        replay(messagingTemplate, replyChannel);

        ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
        channelSyncConsumer.saveReplyMessageChannel(request);
        channelSyncConsumer.send(message);

        verify(messagingTemplate, replyChannel);
    }

    @Test
    public void testSendReplyMessageWithMissingCorrelatorKey() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        ReplyMessageCorrelator correlator = new DefaultReplyMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .copyHeaders(headers)
                .build();

        try {
            ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
            channelSyncConsumer.send(message);
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Can not correlate reply destination"));
            return;
        }

        Assert.fail("Missing " + IllegalArgumentException.class + " because of missing correlation key");
    }

    @Test
    public void testNoReplyDestinationFound() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        ReplyMessageCorrelator correlator = new DefaultReplyMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR, "123456789");
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .copyHeaders(headers)
                .build();

        try {
            ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
            channelSyncConsumer.send(message);
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unable to locate reply channel"));
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
            channelSyncConsumer.send(null);
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

        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .copyHeaders(headers)
                .build();

        reset(messagingTemplate, replyChannel);

        messagingTemplate.send(replyChannel, message);
        expectLastCall().andThrow(new MessageDeliveryException("Internal error!")).once();

        replay(messagingTemplate, replyChannel);

        try {
            ChannelSyncConsumer channelSyncConsumer = (ChannelSyncConsumer) endpoint.createConsumer();
            channelSyncConsumer.saveReplyMessageChannel(MessageBuilder.withPayload("").setReplyChannel(replyChannel).build());
            channelSyncConsumer.send(message);
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
