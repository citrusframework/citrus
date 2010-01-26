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

package com.consol.citrus.adapter.handler;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import org.easymock.EasyMock;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.integration.channel.MessageChannelTemplate;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.message.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class MessageChannelConnectingMessageHandlerTest {

    private MessageChannelTemplate messageChannelTemplate = org.easymock.classextension.EasyMock.createMock(MessageChannelTemplate.class);
    private MessageChannel messageChannel = EasyMock.createMock(MessageChannel.class);
    
    @Test
    @SuppressWarnings("unchecked")
    public void testMessageHandler() throws JMSException {
        MessageChannelConnectingMessageHandler messageHandler = new MessageChannelConnectingMessageHandler();
        messageHandler.setMessageChannelTemplate(messageChannelTemplate);
        messageHandler.setChannel(messageChannel);
        
        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        Map<String, Object> responseHeaders = new HashMap<String, Object>();

        Message response = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();
        
        Message request = MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();
        
        reset(messageChannel);
        org.easymock.classextension.EasyMock.reset(messageChannelTemplate);
        
        expect(messageChannelTemplate.sendAndReceive(request, messageChannel)).andReturn(response).once();
        messageChannelTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messageChannel.getName()).andReturn("sendChannel").once();
        
        replay(messageChannel);
        org.easymock.classextension.EasyMock.replay(messageChannelTemplate);
        
        Message<?> responseMessage = messageHandler.handleMessage(request);
        
        Assert.assertEquals(responseMessage.getPayload(), response.getPayload());
        
        verify(messageChannel);
        org.easymock.classextension.EasyMock.verify(messageChannelTemplate);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testMessageHandlerWithChannelName() throws JMSException {
        MessageChannelConnectingMessageHandler messageHandler = new MessageChannelConnectingMessageHandler();
        messageHandler.setMessageChannelTemplate(messageChannelTemplate);
        messageHandler.setChannelName("sendMessageChannel");
        
        BeanFactory beanFactory = EasyMock.createMock(BeanFactory.class);
        
        messageHandler.setBeanFactory(beanFactory);
        
        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        Map<String, Object> responseHeaders = new HashMap<String, Object>();

        Message response = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();
        
        Message request = MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();
        
        reset(messageChannel, beanFactory);
        org.easymock.classextension.EasyMock.reset(messageChannelTemplate);

        expect(beanFactory.getBean("sendMessageChannel", MessageChannel.class)).andReturn(messageChannel).once();
        
        expect(messageChannelTemplate.sendAndReceive(request, messageChannel)).andReturn(response).once();
        messageChannelTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        replay(messageChannel, beanFactory);
        org.easymock.classextension.EasyMock.replay(messageChannelTemplate);
        
        Message<?> responseMessage = messageHandler.handleMessage(request);
        
        Assert.assertEquals(responseMessage.getPayload(), response.getPayload());
        
        verify(messageChannel, beanFactory);
        org.easymock.classextension.EasyMock.verify(messageChannelTemplate);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testMessageHandlerCustomReplyTimeout() throws JMSException {
        MessageChannelConnectingMessageHandler messageHandler = new MessageChannelConnectingMessageHandler();
        messageHandler.setMessageChannelTemplate(messageChannelTemplate);
        messageHandler.setChannel(messageChannel);
        messageHandler.setReplyTimeout(10000L);
        
        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        Map<String, Object> responseHeaders = new HashMap<String, Object>();

        Message response = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();
        
        Message request = MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();
        
        reset(messageChannel);
        org.easymock.classextension.EasyMock.reset(messageChannelTemplate);
        
        expect(messageChannelTemplate.sendAndReceive(request, messageChannel)).andReturn(response).once();
        messageChannelTemplate.setReceiveTimeout(10000L);
        expectLastCall().once();
        
        expect(messageChannel.getName()).andReturn("sendChannel").once();
        
        replay(messageChannel);
        org.easymock.classextension.EasyMock.replay(messageChannelTemplate);
        
        Message<?> responseMessage = messageHandler.handleMessage(request);
        
        Assert.assertEquals(responseMessage.getPayload(), response.getPayload());
        
        verify(messageChannel);
        org.easymock.classextension.EasyMock.verify(messageChannelTemplate);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testMessageHandlerNoReplyMessage() throws JMSException {
        MessageChannelConnectingMessageHandler messageHandler = new MessageChannelConnectingMessageHandler();
        messageHandler.setMessageChannelTemplate(messageChannelTemplate);
        messageHandler.setChannel(messageChannel);
        
        Map<String, Object> requestHeaders = new HashMap<String, Object>();

        Message request = MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();

        reset(messageChannel);
        org.easymock.classextension.EasyMock.reset(messageChannelTemplate);
        
        expect(messageChannelTemplate.sendAndReceive(request, messageChannel)).andReturn(null).once();
        messageChannelTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messageChannel.getName()).andReturn("sendChannel").once();
        
        replay(messageChannel);
        org.easymock.classextension.EasyMock.replay(messageChannelTemplate);
        
        Message<?> responseMessage = messageHandler.handleMessage(request);
        
        Assert.assertNull(responseMessage);
        
        verify(messageChannel);
        org.easymock.classextension.EasyMock.verify(messageChannelTemplate);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testMessageHandlerWithFallbackMessageHandler() throws JMSException {
        MessageChannelConnectingMessageHandler messageHandler = new MessageChannelConnectingMessageHandler();
        messageHandler.setMessageChannelTemplate(messageChannelTemplate);
        messageHandler.setChannel(messageChannel);
        
        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        Map<String, Object> responseHeaders = new HashMap<String, Object>();

        Message request = MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();

        StaticResponseProducingMessageHandler fallbackMessageHandler = new StaticResponseProducingMessageHandler();
        fallbackMessageHandler.setMessageHeader(responseHeaders);
        fallbackMessageHandler.setMessagePayload("<StaticTestResponse>Hello World!</StaticTestResponse>");
        messageHandler.setFallbackMessageHandlerDelegate(fallbackMessageHandler);
        
        reset(messageChannel);
        org.easymock.classextension.EasyMock.reset(messageChannelTemplate);
        
        expect(messageChannelTemplate.sendAndReceive(request, messageChannel)).andReturn(null).once();
        messageChannelTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messageChannel.getName()).andReturn("sendChannel").once();
        
        replay(messageChannel);
        org.easymock.classextension.EasyMock.replay(messageChannelTemplate);
        
        Message<?> responseMessage = messageHandler.handleMessage(request);
        
        Assert.assertEquals(responseMessage.getPayload(), "<StaticTestResponse>Hello World!</StaticTestResponse>");
        
        verify(messageChannel);
        org.easymock.classextension.EasyMock.verify(messageChannelTemplate);
    }
}
