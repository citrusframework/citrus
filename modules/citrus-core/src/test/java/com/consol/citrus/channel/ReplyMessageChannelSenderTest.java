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
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import org.easymock.classextension.EasyMock;
import org.springframework.integration.channel.MessageChannelTemplate;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.message.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;

/**
 * @author Christoph Deppisch
 */
public class ReplyMessageChannelSenderTest {

    private MessageChannelTemplate messageChannelTemplate = EasyMock.createMock(MessageChannelTemplate.class);
    
    private MessageChannel replyChannel = org.easymock.EasyMock.createMock(MessageChannel.class);
    
    @Test
    public void testSendReplyMessage() {
        ReplyMessageChannelSender sender = new ReplyMessageChannelSender();
        sender.setMessageChannelTemplate(messageChannelTemplate);
        
        ReplyMessageChannelHolder replyChannelHolder = org.easymock.EasyMock.createMock(ReplyMessageChannelHolder.class);
        sender.setReplyMessageChannelHolder(replyChannelHolder);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageChannelTemplate, replyChannel, replyChannelHolder);

        expect(replyChannel.getName()).andReturn("replyChannel").anyTimes();
        
        expect(replyChannelHolder.getReplyMessageChannel()).andReturn(replyChannel).once();
        
        expect(messageChannelTemplate.send(message, replyChannel)).andReturn(true).once();
        
        replay(messageChannelTemplate, replyChannel, replyChannelHolder);
        
        sender.send(message);
        
        verify(messageChannelTemplate, replyChannel, replyChannelHolder);
    }
    
    @Test
    public void testSendReplyMessageWithReplyMessageCorrelator() throws JMSException {
        ReplyMessageChannelSender sender = new ReplyMessageChannelSender();
        sender.setMessageChannelTemplate(messageChannelTemplate);
        
        ReplyMessageChannelHolder replyChannelHolder = org.easymock.EasyMock.createMock(ReplyMessageChannelHolder.class);
        sender.setReplyMessageChannelHolder(replyChannelHolder);
        
        ReplyMessageCorrelator correlator = new DefaultReplyMessageCorrelator();
        sender.setCorrelator(correlator);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR, "123456789");
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        final Message<String> sentMessage = MessageBuilder.fromMessage(message)
                                .removeHeader(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR)
                                .build();
        
        reset(messageChannelTemplate, replyChannel, replyChannelHolder);

        expect(replyChannel.getName()).andReturn("replyChannel").anyTimes();
        
        expect(replyChannelHolder.getReplyMessageChannel("springintegration_id = '123456789'")).andReturn(replyChannel).once();
        
        expect(messageChannelTemplate.send(sentMessage, replyChannel)).andReturn(true).once();
        
        replay(messageChannelTemplate, replyChannel, replyChannelHolder);
        
        sender.send(message);
        
        verify(messageChannelTemplate, replyChannel, replyChannelHolder);
    }
    
    @Test
    public void testSendReplyMessageWithMissingCorrelatorKey() throws JMSException {
        ReplyMessageChannelSender sender = new ReplyMessageChannelSender();
        sender.setMessageChannelTemplate(messageChannelTemplate);
        
        ReplyMessageChannelHolder replyChannelHolder = org.easymock.EasyMock.createMock(ReplyMessageChannelHolder.class);
        sender.setReplyMessageChannelHolder(replyChannelHolder);
        
        ReplyMessageCorrelator correlator = new DefaultReplyMessageCorrelator();
        sender.setCorrelator(correlator);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        try {
            sender.send(message);
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Can not correlate reply destination"));
            return;
        }
        
        Assert.fail("Missing " + IllegalArgumentException.class + " because of missing correlation key");
    }
    
    @Test
    public void testNoReplyDestinationFound() throws JMSException {
        ReplyMessageChannelSender sender = new ReplyMessageChannelSender();
        sender.setMessageChannelTemplate(messageChannelTemplate);
        
        ReplyMessageChannelHolder replyChannelHolder = org.easymock.EasyMock.createMock(ReplyMessageChannelHolder.class);
        sender.setReplyMessageChannelHolder(replyChannelHolder);
        
        ReplyMessageCorrelator correlator = new DefaultReplyMessageCorrelator();
        sender.setCorrelator(correlator);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR, "123456789");
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(replyChannelHolder);

        expect(replyChannelHolder.getReplyMessageChannel("springintegration_id = '123456789'")).andReturn(null).once();

        replay(replyChannelHolder);
        
        try {
            sender.send(message);
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Not able to find temporary reply channel"));
            verify(replyChannelHolder);
            return;
        }
        
        Assert.fail("Missing " + IllegalArgumentException.class + " because no reply destiantion found");
    }
    
    @Test
    public void testSendEmptyMessage() throws JMSException {
        ReplyMessageChannelSender sender = new ReplyMessageChannelSender();
        sender.setMessageChannelTemplate(messageChannelTemplate);
        
        try {
            sender.send(null);
        } catch(IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Can not send empty message");
            return;
        }
        
        Assert.fail("Missing " + IllegalArgumentException.class + " because of sending empty message");
    }
    
    @Test
    public void testSendReplyMessageFail() {
        ReplyMessageChannelSender sender = new ReplyMessageChannelSender();
        sender.setMessageChannelTemplate(messageChannelTemplate);
        
        ReplyMessageChannelHolder replyChannelHolder = org.easymock.EasyMock.createMock(ReplyMessageChannelHolder.class);
        sender.setReplyMessageChannelHolder(replyChannelHolder);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageChannelTemplate, replyChannel, replyChannelHolder);

        expect(replyChannel.getName()).andReturn("replyChannel").anyTimes();
        
        expect(replyChannelHolder.getReplyMessageChannel()).andReturn(replyChannel).once();
        
        expect(messageChannelTemplate.send(message, replyChannel)).andReturn(false).once();
        
        replay(messageChannelTemplate, replyChannel, replyChannelHolder);
       
        try {
            sender.send(message);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Failed to send message to channel 'replyChannel'");
            verify(messageChannelTemplate, replyChannel, replyChannelHolder);
            
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because of message channel template returned false");
    }
}
