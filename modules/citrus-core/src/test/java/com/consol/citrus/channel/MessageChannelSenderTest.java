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
import org.springframework.integration.*;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.support.channel.ChannelResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 */
public class MessageChannelSenderTest {

    private MessagingTemplate messagingTemplate = EasyMock.createMock(MessagingTemplate.class);
    
    private MessageChannel channel = EasyMock.createMock(MessageChannel.class);
    
    private ChannelResolver channelResolver = EasyMock.createMock(ChannelResolver.class);
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSendMessage() {
        MessageChannelSender messageChannelSender = new MessageChannelSender();
        messageChannelSender.setMessagingTemplate(messagingTemplate);
        
        messageChannelSender.setChannel(channel);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messagingTemplate, channel);
        
        messagingTemplate.send(channel, message);
        expectLastCall().once();
        
        replay(messagingTemplate, channel);
        
        messageChannelSender.send(message);
        
        verify(messagingTemplate, channel);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSendMessageChannelNameResolver() {
        MessageChannelSender messageChannelSender = new MessageChannelSender();
        messageChannelSender.setMessagingTemplate(messagingTemplate);
        
        messageChannelSender.setChannelName("testChannel");
        
        messageChannelSender.setChannelResolver(channelResolver);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messagingTemplate, channel, channelResolver);
        
        expect(channelResolver.resolveChannelName("testChannel")).andReturn(channel).once();
        
        messagingTemplate.send(channel, message);
        expectLastCall().once();
        
        replay(messagingTemplate, channel, channelResolver);
        
        messageChannelSender.send(message);
        
        verify(messagingTemplate, channel, channelResolver);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSendMessageFailed() {
        MessageChannelSender messageChannelSender = new MessageChannelSender();
        messageChannelSender.setMessagingTemplate(messagingTemplate);
        
        messageChannelSender.setChannel(channel);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messagingTemplate, channel);
        
        messagingTemplate.send(channel, message);
        expectLastCall().andThrow(new MessageDeliveryException("Internal error!")).once();
        
        replay(messagingTemplate, channel);

        try {
            messageChannelSender.send(message);
        } catch(CitrusRuntimeException e) {
            Assert.assertTrue(e.getLocalizedMessage().startsWith("Failed to send message to channel: "));
            Assert.assertNotNull(e.getCause());
            Assert.assertEquals(e.getCause().getClass(), MessageDeliveryException.class);
            Assert.assertEquals(e.getCause().getLocalizedMessage(), "Internal error!");
            verify(messagingTemplate, channel);
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because no message was received");    
    }
    
}
