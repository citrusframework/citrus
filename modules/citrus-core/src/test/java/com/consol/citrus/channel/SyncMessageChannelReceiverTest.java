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

import static org.easymock.EasyMock.*;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import org.easymock.EasyMock;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.integration.*;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.support.channel.ChannelResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.message.ReplyMessageCorrelator;

/**
 * @author Christoph Deppisch
 */
public class SyncMessageChannelReceiverTest {

    private MessagingTemplate messagingTemplate = EasyMock.createMock(MessagingTemplate.class);
    
    private PollableChannel channel = EasyMock.createMock(PollableChannel.class);
    private MessageChannel replyChannel = EasyMock.createMock(MessageChannel.class);

    private ReplyMessageCorrelator replyMessageCorrelator = EasyMock.createMock(ReplyMessageCorrelator.class);
    
    private ChannelResolver channelResolver = EasyMock.createMock(ChannelResolver.class);
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithReplyChannel() throws JMSException {
        SyncMessageChannelReceiver receiver = new SyncMessageChannelReceiver();
        receiver.setMessagingTemplate(messagingTemplate);
        receiver.setChannel(channel);
        
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
        
        Message receivedMessage = receiver.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        
        MessageChannel savedReplyChannel = receiver.getReplyMessageChannel();
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);
        
        verify(messagingTemplate, channel, replyChannel);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageChannelNameResolver() throws JMSException {
        SyncMessageChannelReceiver receiver = new SyncMessageChannelReceiver();
        receiver.setMessagingTemplate(messagingTemplate);
        receiver.setChannelName("testChannel");
        
        receiver.setChannelResolver(channelResolver);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .setReplyChannel(replyChannel)
                                .build();

        reset(messagingTemplate, channel, replyChannel, channelResolver);
        
        expect(channelResolver.resolveChannelName("testChannel")).andReturn(channel).once();
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(message).once();
        
        replay(messagingTemplate, channel, replyChannel, channelResolver);
        
        Message receivedMessage = receiver.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        
        MessageChannel savedReplyChannel = receiver.getReplyMessageChannel();
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);
        
        verify(messagingTemplate, channel, replyChannel, channelResolver);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithReplyChannelName() throws JMSException {
        SyncMessageChannelReceiver receiver = new SyncMessageChannelReceiver();
        receiver.setMessagingTemplate(messagingTemplate);
        receiver.setChannel(channel);
        
        BeanFactory factory = EasyMock.createMock(BeanFactory.class);
        receiver.setBeanFactory(factory);
        
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
        
        replay(messagingTemplate, channel, replyChannel, factory);
        
        Message receivedMessage = receiver.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        
        MessageChannel savedReplyChannel = receiver.getReplyMessageChannel();
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);
        
        verify(messagingTemplate, channel, replyChannel, factory);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithCustomTimeout() throws JMSException {
        SyncMessageChannelReceiver receiver = new SyncMessageChannelReceiver();
        receiver.setMessagingTemplate(messagingTemplate);
        receiver.setChannel(channel);
        
        receiver.setReceiveTimeout(10000L);
        
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
        
        Message receivedMessage = receiver.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        
        MessageChannel savedReplyChannel = receiver.getReplyMessageChannel();
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);
        
        verify(messagingTemplate, channel, replyChannel);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithReplyMessageCorrelator() throws JMSException {
        SyncMessageChannelReceiver receiver = new SyncMessageChannelReceiver();
        receiver.setMessagingTemplate(messagingTemplate);
        receiver.setChannel(channel);
        
        receiver.setCorrelator(replyMessageCorrelator);
        
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
        
        Message receivedMessage = receiver.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        
        Assert.assertNull(receiver.getReplyMessageChannel());
        Assert.assertNull(receiver.getReplyMessageChannel(MessageHeaders.ID + " = 'totally_wrong'"));
        
        MessageChannel savedReplyChannel = receiver.getReplyMessageChannel(MessageHeaders.ID + " = '123456789'");
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);
        
        verify(messagingTemplate, channel, replyChannel, replyMessageCorrelator);
    }
    
    @Test
    public void testReceiveNoMessage() throws JMSException {
        SyncMessageChannelReceiver receiver = new SyncMessageChannelReceiver();
        receiver.setMessagingTemplate(messagingTemplate);
        receiver.setChannel(channel);
        
        reset(messagingTemplate, channel, replyChannel);
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(null).once();
        
        replay(messagingTemplate, channel, replyChannel);
        
        try {
            receiver.receive();
        } catch(ActionTimeoutException e) {
            Assert.assertTrue(e.getLocalizedMessage().startsWith("Action timeout while receiving message from channel"));
            verify(messagingTemplate, channel, replyChannel);
            return;
        }
        
        Assert.fail("Missing " + ActionTimeoutException.class + " because no message was received");
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageNoReplyChannel() throws JMSException {
        SyncMessageChannelReceiver receiver = new SyncMessageChannelReceiver();
        receiver.setMessagingTemplate(messagingTemplate);
        receiver.setChannel(channel);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .build();

        reset(messagingTemplate, channel, replyChannel);
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(message).once();
        
        replay(messagingTemplate, channel, replyChannel);
        
        Message receivedMessage = receiver.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        
        MessageChannel savedReplyChannel = receiver.getReplyMessageChannel();
        Assert.assertNull(savedReplyChannel);
        
        verify(messagingTemplate, channel, replyChannel);
    }
}
