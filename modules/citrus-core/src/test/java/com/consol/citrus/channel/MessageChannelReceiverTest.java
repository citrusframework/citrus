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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

import java.util.HashMap;
import java.util.Map;

import org.easymock.classextension.EasyMock;
import org.springframework.integration.channel.MessageChannelTemplate;
import org.springframework.integration.channel.PollableChannel;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.ActionTimeoutException;

/**
 * @author Christoph Deppisch
 */
public class MessageChannelReceiverTest {

    private MessageChannelTemplate messageChannelTemplate = EasyMock.createMock(MessageChannelTemplate.class);
    
    private PollableChannel channel = org.easymock.EasyMock.createMock(PollableChannel.class);
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessage() {
        MessageChannelReceiver messageChannelReceiver = new MessageChannelReceiver();
        messageChannelReceiver.setMessageChannelTemplate(messageChannelTemplate);
        
        messageChannelReceiver.setChannel(channel);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageChannelTemplate, channel);
        
        expect(channel.getName()).andReturn("testChannel").anyTimes();
        
        messageChannelTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messageChannelTemplate.receive(channel)).andReturn(message).once();
        
        replay(messageChannelTemplate, channel);
        
        Message receivedMessage = messageChannelReceiver.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        verify(messageChannelTemplate, channel);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithCustomTimeout() {
        MessageChannelReceiver messageChannelReceiver = new MessageChannelReceiver();
        messageChannelReceiver.setMessageChannelTemplate(messageChannelTemplate);
        
        messageChannelReceiver.setChannel(channel);
        messageChannelReceiver.setReceiveTimeout(10000L);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageChannelTemplate, channel);
        
        expect(channel.getName()).andReturn("testChannel").anyTimes();
        
        messageChannelTemplate.setReceiveTimeout(10000L);
        expectLastCall().once();
        
        expect(messageChannelTemplate.receive(channel)).andReturn(message).once();
        
        replay(messageChannelTemplate, channel);
        
        Message receivedMessage = messageChannelReceiver.receive();
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        verify(messageChannelTemplate, channel);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageTimeoutOverride() {
        MessageChannelReceiver messageChannelReceiver = new MessageChannelReceiver();
        messageChannelReceiver.setMessageChannelTemplate(messageChannelTemplate);
        
        messageChannelReceiver.setChannel(channel);
        messageChannelReceiver.setReceiveTimeout(10000L);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageChannelTemplate, channel);
        
        expect(channel.getName()).andReturn("testChannel").anyTimes();
        
        messageChannelTemplate.setReceiveTimeout(25000L);
        expectLastCall().once();
        
        expect(messageChannelTemplate.receive(channel)).andReturn(message).once();
        
        replay(messageChannelTemplate, channel);
        
        Message receivedMessage = messageChannelReceiver.receive(25000L);
        
        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeaders(), message.getHeaders());
        verify(messageChannelTemplate, channel);
    }
    
    @Test
    public void testReceiveTimeout() {
        MessageChannelReceiver messageChannelReceiver = new MessageChannelReceiver();
        messageChannelReceiver.setMessageChannelTemplate(messageChannelTemplate);
        
        messageChannelReceiver.setChannel(channel);
        
        reset(messageChannelTemplate, channel);
        
        expect(channel.getName()).andReturn("testChannel").anyTimes();
        
        messageChannelTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(messageChannelTemplate.receive(channel)).andReturn(null).once();
        
        replay(messageChannelTemplate, channel);
        
        try {
            messageChannelReceiver.receive();
        } catch(ActionTimeoutException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Action timeout while receiving message from channel 'testChannel'");
            verify(messageChannelTemplate, channel);
            return;
        }
        
        Assert.fail("Missing " + ActionTimeoutException.class + " because no message was received");
    }
    
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testReceiveSelected() {
        MessageChannelReceiver messageChannelReceiver = new MessageChannelReceiver();
        messageChannelReceiver.setMessageChannelTemplate(messageChannelTemplate);
        
        messageChannelReceiver.setChannel(channel);
        
        messageChannelReceiver.receiveSelected("Operation = 'sayHello'");
    }
    
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testReceiveSelectedWithTimeout() {
        MessageChannelReceiver messageChannelReceiver = new MessageChannelReceiver();
        messageChannelReceiver.setMessageChannelTemplate(messageChannelTemplate);
        
        messageChannelReceiver.setChannel(channel);
        
        messageChannelReceiver.receiveSelected("Operation = 'sayHello'", 10000L);
    }
}
