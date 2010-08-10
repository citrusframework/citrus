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
