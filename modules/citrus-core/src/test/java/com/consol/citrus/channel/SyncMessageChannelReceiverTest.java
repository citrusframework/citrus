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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import org.easymock.classextension.EasyMock;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.integration.channel.MessageChannelTemplate;
import org.springframework.integration.channel.PollableChannel;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.message.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.message.ReplyMessageCorrelator;

/**
 * @author Christoph Deppisch
 */
public class SyncMessageChannelReceiverTest {

    private MessageChannelTemplate messageChannelTemplate = EasyMock.createMock(MessageChannelTemplate.class);
    
    private PollableChannel channel = org.easymock.EasyMock.createMock(PollableChannel.class);
    private MessageChannel replyChannel = org.easymock.EasyMock.createMock(MessageChannel.class);

    private ReplyMessageCorrelator replyMessageCorrelator = org.easymock.EasyMock.createMock(ReplyMessageCorrelator.class);
    
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithReplyChannel() throws JMSException {
        SyncMessageChannelReceiver receiver = new SyncMessageChannelReceiver();
        receiver.setMessageChannelTemplate(messageChannelTemplate);
        receiver.setChannel(channel);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .setReplyChannel(replyChannel)
                                .build();

        reset(messageChannelTemplate, channel, replyChannel);
        
        expect(channel.getName()).andReturn("testChannel").atLeastOnce();
        
        messageChannelTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messageChannelTemplate.receive(channel)).andReturn(message).once();
        
        replay(messageChannelTemplate, channel, replyChannel);
        
        Message receivedMessage = receiver.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        
        MessageChannel savedReplyChannel = receiver.getReplyMessageChannel();
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);
        
        verify(messageChannelTemplate, channel, replyChannel);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithReplyChannelName() throws JMSException {
        SyncMessageChannelReceiver receiver = new SyncMessageChannelReceiver();
        receiver.setMessageChannelTemplate(messageChannelTemplate);
        receiver.setChannel(channel);
        
        BeanFactory factory = org.easymock.EasyMock.createMock(BeanFactory.class);
        receiver.setBeanFactory(factory);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .setReplyChannelName("replyChannel")
                                .build();

        reset(messageChannelTemplate, channel, replyChannel, factory);
        
        expect(channel.getName()).andReturn("testChannel").atLeastOnce();
        
        messageChannelTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messageChannelTemplate.receive(channel)).andReturn(message).once();
        
        expect(factory.getBean("replyChannel", MessageChannel.class)).andReturn(replyChannel).once();
        
        replay(messageChannelTemplate, channel, replyChannel, factory);
        
        Message receivedMessage = receiver.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        
        MessageChannel savedReplyChannel = receiver.getReplyMessageChannel();
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);
        
        verify(messageChannelTemplate, channel, replyChannel, factory);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithCustomTimeout() throws JMSException {
        SyncMessageChannelReceiver receiver = new SyncMessageChannelReceiver();
        receiver.setMessageChannelTemplate(messageChannelTemplate);
        receiver.setChannel(channel);
        
        receiver.setReceiveTimeout(10000L);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .setReplyChannel(replyChannel)
                                .build();

        reset(messageChannelTemplate, channel, replyChannel);
        
        expect(channel.getName()).andReturn("testChannel").atLeastOnce();
        
        messageChannelTemplate.setReceiveTimeout(10000L);
        expectLastCall().once();
        
        expect(messageChannelTemplate.receive(channel)).andReturn(message).once();
        
        replay(messageChannelTemplate, channel, replyChannel);
        
        Message receivedMessage = receiver.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        
        MessageChannel savedReplyChannel = receiver.getReplyMessageChannel();
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);
        
        verify(messageChannelTemplate, channel, replyChannel);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithReplyMessageCorrelator() throws JMSException {
        SyncMessageChannelReceiver receiver = new SyncMessageChannelReceiver();
        receiver.setMessageChannelTemplate(messageChannelTemplate);
        receiver.setChannel(channel);
        
        receiver.setCorrelator(replyMessageCorrelator);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .setReplyChannel(replyChannel)
                                .build();

        reset(messageChannelTemplate, channel, replyChannel, replyMessageCorrelator);
        
        expect(channel.getName()).andReturn("testChannel").atLeastOnce();
        
        messageChannelTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messageChannelTemplate.receive(channel)).andReturn(message).once();
        
        expect(replyMessageCorrelator.getCorrelationKey(message)).andReturn("springintegration_id = '123456789'").once();
        
        replay(messageChannelTemplate, channel, replyChannel, replyMessageCorrelator);
        
        Message receivedMessage = receiver.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        
        Assert.assertNull(receiver.getReplyMessageChannel());
        Assert.assertNull(receiver.getReplyMessageChannel("springintegration_id = 'totally_wrong'"));
        
        MessageChannel savedReplyChannel = receiver.getReplyMessageChannel("springintegration_id = '123456789'");
        Assert.assertNotNull(savedReplyChannel);
        Assert.assertEquals(savedReplyChannel, replyChannel);
        
        verify(messageChannelTemplate, channel, replyChannel, replyMessageCorrelator);
    }
    
    @Test
    public void testReceiveNoMessage() throws JMSException {
        SyncMessageChannelReceiver receiver = new SyncMessageChannelReceiver();
        receiver.setMessageChannelTemplate(messageChannelTemplate);
        receiver.setChannel(channel);
        
        reset(messageChannelTemplate, channel, replyChannel);
        
        expect(channel.getName()).andReturn("testChannel").atLeastOnce();
        
        messageChannelTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messageChannelTemplate.receive(channel)).andReturn(null).once();
        
        replay(messageChannelTemplate, channel, replyChannel);
        
        try {
            receiver.receive();
        } catch(ActionTimeoutException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Action timeout while receiving message from channel 'testChannel'");
            verify(messageChannelTemplate, channel, replyChannel);
            return;
        }
        
        Assert.fail("Missing " + ActionTimeoutException.class + " because no message was received");
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageNoReplyChannel() throws JMSException {
        SyncMessageChannelReceiver receiver = new SyncMessageChannelReceiver();
        receiver.setMessageChannelTemplate(messageChannelTemplate);
        receiver.setChannel(channel);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(headers)
                                .build();

        reset(messageChannelTemplate, channel, replyChannel);
        
        expect(channel.getName()).andReturn("testChannel").atLeastOnce();
        
        messageChannelTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messageChannelTemplate.receive(channel)).andReturn(message).once();
        
        replay(messageChannelTemplate, channel, replyChannel);
        
        Message receivedMessage = receiver.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        
        MessageChannel savedReplyChannel = receiver.getReplyMessageChannel();
        Assert.assertNull(savedReplyChannel);
        
        verify(messageChannelTemplate, channel, replyChannel);
    }
}
