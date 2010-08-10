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
