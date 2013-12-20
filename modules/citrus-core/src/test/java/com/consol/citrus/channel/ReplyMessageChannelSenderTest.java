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
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.integration.*;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.jms.JMSException;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class ReplyMessageChannelSenderTest {

    private MessagingTemplate messagingTemplate = EasyMock.createMock(MessagingTemplate.class);
    
    private MessageChannel replyChannel = org.easymock.EasyMock.createMock(MessageChannel.class);
    
    @Test
    public void testSendReplyMessage() {
        ReplyMessageChannelSender sender = new ReplyMessageChannelSender();
        sender.setMessagingTemplate(messagingTemplate);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messagingTemplate, replyChannel);

        messagingTemplate.send(replyChannel, message);
        expectLastCall().once();
        
        replay(messagingTemplate, replyChannel);

        ((ChannelSyncConsumer)sender.getMessageChannelEndpoint().createConsumer()).saveReplyMessageChannel(MessageBuilder.withPayload("").setReplyChannel(replyChannel).build());
        sender.send(message);
        
        verify(messagingTemplate, replyChannel);
    }
    
    @Test
    public void testSendReplyMessageWithReplyMessageCorrelator() throws JMSException {
        ReplyMessageChannelSender sender = new ReplyMessageChannelSender();
        sender.setMessagingTemplate(messagingTemplate);
        
        ReplyMessageCorrelator correlator = new DefaultReplyMessageCorrelator();
        sender.setCorrelator(correlator);

        Message<String> request = MessageBuilder.withPayload("").setReplyChannel(replyChannel).build();

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR, request.getHeaders().getId());
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        final Message<String> sentMessage = MessageBuilder.fromMessage(message)
                                .removeHeader(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR)
                                .build();
        
        reset(messagingTemplate, replyChannel);

        messagingTemplate.send(eq(replyChannel), (Message<?>)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Assert.assertEquals(((Message<?>)getCurrentArguments()[1]).getPayload(), sentMessage.getPayload());
                return null;
            }
        }).once();
        
        replay(messagingTemplate, replyChannel);

        ((ChannelSyncConsumer)sender.getMessageChannelEndpoint().createConsumer()).saveReplyMessageChannel(request);
        sender.send(message);
        
        verify(messagingTemplate, replyChannel);
    }
    
    @Test
    public void testSendReplyMessageWithMissingCorrelatorKey() throws JMSException {
        ReplyMessageChannelSender sender = new ReplyMessageChannelSender();
        sender.setMessagingTemplate(messagingTemplate);
        
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
        sender.setMessagingTemplate(messagingTemplate);
        
        ReplyMessageCorrelator correlator = new DefaultReplyMessageCorrelator();
        sender.setCorrelator(correlator);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR, "123456789");
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        try {
            sender.send(message);
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unable to locate reply channel"));
            return;
        }
        
        Assert.fail("Missing " + IllegalArgumentException.class + " because no reply destination found");
    }
    
    @Test
    public void testSendEmptyMessage() throws JMSException {
        ReplyMessageChannelSender sender = new ReplyMessageChannelSender();
        sender.setMessagingTemplate(messagingTemplate);
        
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
        sender.setMessagingTemplate(messagingTemplate);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messagingTemplate, replyChannel);

        messagingTemplate.send(replyChannel, message);
        expectLastCall().andThrow(new MessageDeliveryException("Internal error!")).once();
        
        replay(messagingTemplate, replyChannel);

        try {
            ((ChannelSyncConsumer)sender.getMessageChannelEndpoint().createConsumer()).saveReplyMessageChannel(MessageBuilder.withPayload("").setReplyChannel(replyChannel).build());
            sender.send(message);
        } catch(CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Failed to send message to channel: "));
            Assert.assertNotNull(e.getCause());
            Assert.assertEquals(e.getCause().getClass(), MessageDeliveryException.class);
            Assert.assertEquals(e.getCause().getLocalizedMessage(), "Internal error!");
            verify(messagingTemplate, replyChannel);
            
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because of message channel template returned false");
    }
    
}
