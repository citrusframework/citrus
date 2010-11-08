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
import org.easymock.IAnswer;
import org.springframework.integration.channel.MessageChannelTemplate;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.message.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.ReplyMessageCorrelator;
import com.consol.citrus.message.ReplyMessageHandler;

/**
 * @author Christoph Deppisch
 */
public class SyncMessageChannelSenderTest {

    private MessageChannelTemplate messageChannelTemplate = EasyMock.createMock(MessageChannelTemplate.class);
    
    private MessageChannel channel = org.easymock.EasyMock.createMock(MessageChannel.class);

    private ReplyMessageHandler replyMessageHandler = org.easymock.EasyMock.createMock(ReplyMessageHandler.class);
    
    private ReplyMessageCorrelator replyMessageCorrelator = org.easymock.EasyMock.createMock(ReplyMessageCorrelator.class);
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessage() throws JMSException {
        SyncMessageChannelSender sender = new SyncMessageChannelSender();
        sender.setMessageChannelTemplate(messageChannelTemplate);
        sender.setChannel(channel);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final Message response = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();

        reset(messageChannelTemplate, channel, replyMessageHandler);
        
        expect(channel.getName()).andReturn("testChannel").atLeastOnce();
        
        messageChannelTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messageChannelTemplate.sendAndReceive(message, channel)).andReturn(response).once();
        
        replay(messageChannelTemplate, channel, replyMessageHandler);
        
        sender.send(message);
        
        verify(messageChannelTemplate, channel, replyMessageHandler);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithReplyHandler() throws JMSException {
        SyncMessageChannelSender sender = new SyncMessageChannelSender();
        sender.setMessageChannelTemplate(messageChannelTemplate);
        sender.setChannel(channel);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final Message response = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();

        sender.setReplyMessageHandler(replyMessageHandler);
        
        reset(messageChannelTemplate, channel, replyMessageHandler);
        
        expect(channel.getName()).andReturn("testChannel").atLeastOnce();
        
        messageChannelTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messageChannelTemplate.sendAndReceive(message, channel)).andReturn(response).once();
        
        replyMessageHandler.onReplyMessage((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message replyMessage = (Message) org.easymock.EasyMock.getCurrentArguments()[0];
                Assert.assertEquals(replyMessage.getPayload(), response.getPayload());
                Assert.assertEquals(replyMessage.getHeaders(), response.getHeaders());
                return null;
            }
        }).once();
        
        replay(messageChannelTemplate, channel, replyMessageHandler);
        
        sender.send(message);
        
        verify(messageChannelTemplate, channel, replyMessageHandler);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithReplyTimeout() throws JMSException {
        SyncMessageChannelSender sender = new SyncMessageChannelSender();
        sender.setMessageChannelTemplate(messageChannelTemplate);
        sender.setChannel(channel);
        
        sender.setReplyTimeout(10000L);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final Message response = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();

        sender.setReplyMessageHandler(replyMessageHandler);
        
        reset(messageChannelTemplate, channel, replyMessageHandler);
        
        expect(channel.getName()).andReturn("testChannel").atLeastOnce();
        
        messageChannelTemplate.setReceiveTimeout(10000L);
        expectLastCall().once();
        
        expect(messageChannelTemplate.sendAndReceive(message, channel)).andReturn(response).once();
        
        replyMessageHandler.onReplyMessage((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message replyMessage = (Message) org.easymock.EasyMock.getCurrentArguments()[0];
                Assert.assertEquals(replyMessage.getPayload(), response.getPayload());
                Assert.assertEquals(replyMessage.getHeaders(), response.getHeaders());
                return null;
            }
        }).once();
        
        replay(messageChannelTemplate, channel, replyMessageHandler);
        
        sender.send(message);
        
        verify(messageChannelTemplate, channel, replyMessageHandler);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithReplyMessageCorrelator() throws JMSException {
        SyncMessageChannelSender sender = new SyncMessageChannelSender();
        sender.setMessageChannelTemplate(messageChannelTemplate);
        sender.setChannel(channel);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final Message response = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();

        sender.setReplyMessageHandler(replyMessageHandler);

        sender.setCorrelator(replyMessageCorrelator);
        
        reset(messageChannelTemplate, channel, replyMessageHandler, replyMessageCorrelator);
        
        expect(channel.getName()).andReturn("testChannel").atLeastOnce();
        
        messageChannelTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messageChannelTemplate.sendAndReceive(message, channel)).andReturn(response).once();
        
        expect(replyMessageCorrelator.getCorrelationKey(message)).andReturn("springintegration_id = '123456789'").once();
        
        replyMessageHandler.onReplyMessage((Message)anyObject(), (String)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message replyMessage = (Message) org.easymock.EasyMock.getCurrentArguments()[0];
                Assert.assertEquals(replyMessage.getPayload(), response.getPayload());
                Assert.assertEquals(replyMessage.getHeaders(), response.getHeaders());
                
                Assert.assertEquals(org.easymock.EasyMock.getCurrentArguments()[1].toString(), 
                        "springintegration_id = '123456789'");
                return null;
            }
        }).once();
        
        replay(messageChannelTemplate, channel, replyMessageHandler, replyMessageCorrelator);
        
        sender.send(message);
        
        verify(messageChannelTemplate, channel, replyMessageHandler, replyMessageCorrelator);
    }
    
    @Test
    public void testSendMessageNoResponse() throws JMSException {
        SyncMessageChannelSender sender = new SyncMessageChannelSender();
        sender.setMessageChannelTemplate(messageChannelTemplate);
        sender.setChannel(channel);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        sender.setReplyMessageHandler(replyMessageHandler);
        
        reset(messageChannelTemplate, channel, replyMessageHandler);
        
        expect(channel.getName()).andReturn("testChannel").atLeastOnce();
        
        messageChannelTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messageChannelTemplate.sendAndReceive(message, channel)).andReturn(null).once();
        
        replay(messageChannelTemplate, channel, replyMessageHandler);

        try {
            sender.send(message);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Reply timed out after 5000ms. Did not receive reply message on reply channel");
            verify(messageChannelTemplate, channel, replyMessageHandler);
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because of reply timeout");
    }
    
}
