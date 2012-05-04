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

import org.easymock.EasyMock;
import org.springframework.integration.Message;
import org.springframework.integration.core.*;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.support.channel.ChannelResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.channel.selector.HeaderMatchingMessageSelector;
import com.consol.citrus.exceptions.ActionTimeoutException;

/**
 * @author Christoph Deppisch
 */
public class MessageChannelReceiverTest {

    private MessagingTemplate messagingTemplate = EasyMock.createMock(MessagingTemplate.class);
    
    private PollableChannel channel = EasyMock.createMock(PollableChannel.class);
    
    private ChannelResolver channelResolver = EasyMock.createMock(ChannelResolver.class);
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessage() {
        MessageChannelReceiver messageChannelReceiver = new MessageChannelReceiver();
        messageChannelReceiver.setMessagingTemplate(messagingTemplate);
        
        messageChannelReceiver.setChannel(channel);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messagingTemplate, channel);
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(message).once();
        
        replay(messagingTemplate, channel);
        
        Message receivedMessage = messageChannelReceiver.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        verify(messagingTemplate, channel);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageChannelNameResolver() {
        MessageChannelReceiver messageChannelReceiver = new MessageChannelReceiver();
        messageChannelReceiver.setMessagingTemplate(messagingTemplate);
        
        messageChannelReceiver.setChannelName("testChannel");
        
        messageChannelReceiver.setChannelResolver(channelResolver);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messagingTemplate, channel, channelResolver);
        
        expect(channelResolver.resolveChannelName("testChannel")).andReturn(channel).once();
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(message).once();
        
        replay(messagingTemplate, channel, channelResolver);
        
        Message receivedMessage = messageChannelReceiver.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        verify(messagingTemplate, channel, channelResolver);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithCustomTimeout() {
        MessageChannelReceiver messageChannelReceiver = new MessageChannelReceiver();
        messageChannelReceiver.setMessagingTemplate(messagingTemplate);
        
        messageChannelReceiver.setChannel(channel);
        messageChannelReceiver.setReceiveTimeout(10000L);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messagingTemplate, channel);
        
        messagingTemplate.setReceiveTimeout(10000L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(message).once();
        
        replay(messagingTemplate, channel);
        
        Message receivedMessage = messageChannelReceiver.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        verify(messagingTemplate, channel);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageTimeoutOverride() {
        MessageChannelReceiver messageChannelReceiver = new MessageChannelReceiver();
        messageChannelReceiver.setMessagingTemplate(messagingTemplate);
        
        messageChannelReceiver.setChannel(channel);
        messageChannelReceiver.setReceiveTimeout(10000L);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messagingTemplate, channel);
        
        messagingTemplate.setReceiveTimeout(25000L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(message).once();
        
        replay(messagingTemplate, channel);
        
        Message receivedMessage = messageChannelReceiver.receive(25000L);
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        verify(messagingTemplate, channel);
    }
    
    @Test
    public void testReceiveTimeout() {
        MessageChannelReceiver messageChannelReceiver = new MessageChannelReceiver();
        messageChannelReceiver.setMessagingTemplate(messagingTemplate);
        
        messageChannelReceiver.setChannel(channel);
        
        reset(messagingTemplate, channel);
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.receive(channel)).andReturn(null).once();
        
        replay(messagingTemplate, channel);
        
        try {
            messageChannelReceiver.receive();
            Assert.fail("Missing " + ActionTimeoutException.class + " because no message was received");
        } catch(ActionTimeoutException e) {
            Assert.assertTrue(e.getLocalizedMessage().startsWith("Action timeout while receiving message from channel"));
        }
        
        verify(messagingTemplate, channel);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveSelected() {
        MessageChannelReceiver messageChannelReceiver = new MessageChannelReceiver();
        messageChannelReceiver.setMessagingTemplate(messagingTemplate);
        
        messageChannelReceiver.setChannel(channel);
        messageChannelReceiver.setReceiveTimeout(0L);

        try {
            messageChannelReceiver.receiveSelected("Operation = 'sayHello'");
            Assert.fail("Missing exception due to unsupported operation");
        } catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e.getMessage());
        }
        
        QueueChannel queueChannel = EasyMock.createMock(QueueChannel.class);
        Message message = MessageBuilder.withPayload("Hello").setHeader("Operation", "sayHello").build();
        reset(queueChannel);
        
        expect(queueChannel.receiveSelected(anyObject(HeaderMatchingMessageSelector.class)))
                            .andReturn(message).once();
        
        replay(queueChannel);
        
        messageChannelReceiver.setChannel(queueChannel);
        Message receivedMessage = messageChannelReceiver.receiveSelected("Operation = 'sayHello'");
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        verify(queueChannel);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveSelectedWithTimeout() {
        MessageChannelReceiver messageChannelReceiver = new MessageChannelReceiver();
        messageChannelReceiver.setMessagingTemplate(messagingTemplate);
        
        messageChannelReceiver.setChannel(channel);
        
        try {
            messageChannelReceiver.receiveSelected("Operation = 'sayHello'", 2500L);
            Assert.fail("Missing exception due to unsupported operation");
        } catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e.getMessage());
        }
        
        QueueChannel queueChannel = EasyMock.createMock(QueueChannel.class);
        Message message = MessageBuilder.withPayload("Hello").setHeader("Operation", "sayHello").build();
        reset(queueChannel);
        
        expect(queueChannel.receiveSelected(anyObject(HeaderMatchingMessageSelector.class)))
                            .andReturn(null).times(2) // force retry
                            .andReturn(message).once();
        
        replay(queueChannel);
        
        messageChannelReceiver.setChannel(queueChannel);
        Message receivedMessage = messageChannelReceiver.receiveSelected("Operation = 'sayHello'", 2500L);
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        verify(queueChannel);
    }
    
    @Test
    public void testReceiveSelectedNoMessageWithTimeout() {
        MessageChannelReceiver messageChannelReceiver = new MessageChannelReceiver();
        messageChannelReceiver.setMessagingTemplate(messagingTemplate);
        
        QueueChannel queueChannel = EasyMock.createMock(QueueChannel.class);
        
        reset(queueChannel);
        
        expect(queueChannel.receiveSelected(anyObject(HeaderMatchingMessageSelector.class)))
                            .andReturn(null).times(5); // force retries
        
        replay(queueChannel);
        
        messageChannelReceiver.setChannel(queueChannel);
        
        try {
            messageChannelReceiver.receiveSelected("Operation = 'sayHello'", 1500L);
            Assert.fail("Missing " + ActionTimeoutException.class + " because no message was received");
        } catch(ActionTimeoutException e) {
            Assert.assertTrue(e.getLocalizedMessage().startsWith("Action timeout while receiving message from channel"));
        }
        
        verify(queueChannel);
    }
}
