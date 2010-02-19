/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.channel;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
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
            Assert.assertEquals(e.getLocalizedMessage(), "Reply timed out after 5000ms. Did not receive reply message on channel");
            verify(messageChannelTemplate, channel, replyMessageHandler);
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because of reply timeout");
    }
}
