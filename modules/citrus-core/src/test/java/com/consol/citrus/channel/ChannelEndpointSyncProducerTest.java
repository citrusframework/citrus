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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class ChannelEndpointSyncProducerTest extends AbstractTestNGUnitTest {

    private MessagingTemplate messagingTemplate = EasyMock.createMock(MessagingTemplate.class);
    private MessageChannel channel = org.easymock.EasyMock.createMock(MessageChannel.class);
    private MessageCorrelator messageCorrelator = org.easymock.EasyMock.createMock(MessageCorrelator.class);
    private DestinationResolver channelResolver = EasyMock.createMock(DestinationResolver.class);
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSendMessage() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);
        
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final org.springframework.messaging.Message response = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();

        reset(messagingTemplate, channel);
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.sendAndReceive(eq(channel), anyObject(org.springframework.messaging.Message.class))).andReturn(response).once();
        
        replay(messagingTemplate, channel);

        endpoint.createProducer().send(message, context);
        
        verify(messagingTemplate, channel);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSendMessageChannelNameResolver() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannelName("testChannel");

        endpoint.getEndpointConfiguration().setChannelResolver(channelResolver);
        
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final org.springframework.messaging.Message response = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();

        reset(messagingTemplate, channel, channelResolver);
        
        expect(channelResolver.resolveDestination("testChannel")).andReturn(channel).once();
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.sendAndReceive(eq(channel), anyObject(org.springframework.messaging.Message.class))).andReturn(response).once();
        
        replay(messagingTemplate, channel, channelResolver);

        endpoint.createProducer().send(message, context);
        
        verify(messagingTemplate, channel, channelResolver);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSendMessageWithReplyHandler() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);
        
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final org.springframework.messaging.Message response = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();

        reset(messagingTemplate, channel);
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.sendAndReceive(eq(channel), anyObject(org.springframework.messaging.Message.class))).andReturn(response).once();
        
        replay(messagingTemplate, channel);

        ChannelSyncProducer channelSyncProducer = (ChannelSyncProducer) endpoint.createProducer();
        channelSyncProducer.send(message, context);

        Message replyMessage = channelSyncProducer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(message),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertEquals(replyMessage.getPayload(), response.getPayload());
        Assert.assertEquals(replyMessage.getHeader(org.springframework.messaging.MessageHeaders.ID), response.getHeaders().getId());
        
        verify(messagingTemplate, channel);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSendMessageWithCustomReplyTimeout() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);

        endpoint.getEndpointConfiguration().setTimeout(10000L);
        
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final org.springframework.messaging.Message response = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();

        reset(messagingTemplate, channel);
        
        messagingTemplate.setReceiveTimeout(10000L);
        expectLastCall().once();
        
        expect(messagingTemplate.sendAndReceive(eq(channel), anyObject(org.springframework.messaging.Message.class))).andReturn(response).once();

        replay(messagingTemplate, channel);

        ChannelSyncProducer channelSyncProducer = (ChannelSyncProducer) endpoint.createProducer();
        channelSyncProducer.send(message, context);

        Message replyMessage = channelSyncProducer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(message),
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertEquals(replyMessage.getPayload(), response.getPayload());
        Assert.assertEquals(replyMessage.getHeader(org.springframework.messaging.MessageHeaders.ID), response.getHeaders().getId());

        verify(messagingTemplate, channel);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSendMessageWithReplyMessageCorrelator() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);
        
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final org.springframework.messaging.Message response = MessageBuilder.withPayload("<TestResponse>Hello World!</TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();

        endpoint.getEndpointConfiguration().setCorrelator(messageCorrelator);
        
        reset(messagingTemplate, channel, messageCorrelator);
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.sendAndReceive(eq(channel), anyObject(org.springframework.messaging.Message.class))).andReturn(response).once();
        
        expect(messageCorrelator.getCorrelationKey(message)).andReturn(MessageHeaders.ID + " = '123456789'").once();
        expect(messageCorrelator.getCorrelationKeyName(anyObject(String.class))).andReturn("correlationKeyName").once();

        replay(messagingTemplate, channel, messageCorrelator);

        ChannelSyncProducer channelSyncProducer = (ChannelSyncProducer) endpoint.createProducer();
        channelSyncProducer.send(message, context);

        Message replyMessage = channelSyncProducer.getCorrelationManager().find(MessageHeaders.ID + " = '123456789'",
                endpoint.getEndpointConfiguration().getTimeout());
        Assert.assertEquals(replyMessage.getPayload(), response.getPayload());
        Assert.assertEquals(replyMessage.getHeader(org.springframework.messaging.MessageHeaders.ID), response.getHeaders().getId());

        verify(messagingTemplate, channel, messageCorrelator);
    }
    
    @Test
    public void testSendMessageNoResponse() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        endpoint.getEndpointConfiguration().setChannel(channel);
        
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(messagingTemplate, channel);
        
        messagingTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messagingTemplate.sendAndReceive(eq(channel), anyObject(org.springframework.messaging.Message.class))).andReturn(null).once();
        
        replay(messagingTemplate, channel);

        try {
            endpoint.createProducer().send(message, context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Reply timed out after 5000ms. Did not receive reply message on reply channel");
            verify(messagingTemplate, channel);
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because of reply timeout");
    }

    @Test
    public void testOnReplyMessage() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        ChannelSyncProducer channelSyncProducer = (ChannelSyncProducer) endpoint.createProducer();
        channelSyncProducer.getCorrelationManager().saveCorrelationKey(
                endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(channelSyncProducer.getName()),
                channelSyncProducer.toString(), context);
        channelSyncProducer.getCorrelationManager().store(channelSyncProducer.toString(), message);

        Assert.assertEquals(channelSyncProducer.receive(context), message);
    }

    @Test
    public void testOnReplyMessageWithCorrelatorKey() {
        ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        ChannelSyncProducer channelSyncProducer = (ChannelSyncProducer) endpoint.createProducer();
        channelSyncProducer.getCorrelationManager().store(new DefaultMessageCorrelator().getCorrelationKey(message), message);

        Assert.assertEquals(channelSyncProducer.receive(new DefaultMessageCorrelator().getCorrelationKey(message), context), message);
    }
    
}
