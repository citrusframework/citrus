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

package com.consol.citrus.jms;

import com.consol.citrus.message.*;
import org.easymock.EasyMock;
import org.springframework.integration.Message;
import org.springframework.integration.jms.JmsHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jms.core.JmsTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class JmsReplyMessageSenderTest {

    private ConnectionFactory connectionFactory = org.easymock.EasyMock.createMock(ConnectionFactory.class);
    private Connection connection = EasyMock.createMock(Connection.class);
    private Session session = EasyMock.createMock(Session.class);
    private Destination replyDestination = EasyMock.createMock(Destination.class);
    private MessageProducer messageProducer = EasyMock.createMock(MessageProducer.class);
    
    private JmsTemplate jmsTemplate = EasyMock.createMock(JmsTemplate.class);
    
    @Test
    public void testSendMessageWithJmsTemplate() {
        JmsReplyMessageSender sender = new JmsReplyMessageSender();
        sender.setJmsTemplate(jmsTemplate);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(jmsTemplate, connectionFactory, messageProducer);

        jmsTemplate.convertAndSend(replyDestination, message);
        expectLastCall().once();
        
        replay(jmsTemplate, connectionFactory, messageProducer);

        sender.getJmsEndpoint().saveReplyDestination(MessageBuilder.withPayload("").setHeader(JmsHeaders.REPLY_TO, replyDestination).build());
        sender.send(message);
        
        verify(jmsTemplate, connectionFactory, messageProducer);
    }
    
    @Test
    public void testSendMessageWithConnectionFactory() throws JMSException {
        JmsReplyMessageSender sender = new JmsReplyMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(jmsTemplate, connectionFactory, messageProducer, connection, session);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();

        expect(session.createProducer(replyDestination)).andReturn(messageProducer).once();
        messageProducer.send((TextMessage)anyObject());
        expectLastCall().once();
        
        expect(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).andReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, String>()));
        
        expect(session.getTransacted()).andReturn(false).once();
        
        replay(jmsTemplate, connectionFactory, messageProducer, connection, session);

        sender.getJmsEndpoint().saveReplyDestination(MessageBuilder.withPayload("").setHeader(JmsHeaders.REPLY_TO, replyDestination).build());
        sender.send(message);
        
        verify(jmsTemplate, connectionFactory, messageProducer, connection, session);
    }
    
    @Test
    public void testSendMessageWithReplyMessageCorrelator() throws JMSException {
        JmsReplyMessageSender sender = new JmsReplyMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        ReplyMessageCorrelator correlator = new DefaultReplyMessageCorrelator();
        sender.setCorrelator(correlator);

        Message<String> requestMessage = MessageBuilder.withPayload("")
                                .setHeader(JmsHeaders.REPLY_TO, replyDestination)
                                .build();

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR, requestMessage.getHeaders().getId());
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(jmsTemplate, connectionFactory, messageProducer, connection, session);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();

        expect(session.createProducer(replyDestination)).andReturn(messageProducer).once();
        messageProducer.send((TextMessage)anyObject());
        expectLastCall().once();
        
        expect(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).andReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, String>()));
        
        expect(session.getTransacted()).andReturn(false).once();
        
        replay(jmsTemplate, connectionFactory, messageProducer, connection, session);

        sender.getJmsEndpoint().saveReplyDestination(requestMessage);
        sender.send(message);
        
        verify(jmsTemplate, connectionFactory, messageProducer, connection, session);
    }
    
    @Test
    public void testSendMessageWithMissingCorrelatorKey() throws JMSException {
        JmsReplyMessageSender sender = new JmsReplyMessageSender();
        sender.setConnectionFactory(connectionFactory);

        Message<String> requestMessage = MessageBuilder.withPayload("")
                .setHeader(JmsHeaders.REPLY_TO, replyDestination)
                .build();

        ReplyMessageCorrelator correlator = new DefaultReplyMessageCorrelator();
        sender.setCorrelator(correlator);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        try {
            sender.getJmsEndpoint().saveReplyDestination(requestMessage);
            sender.send(message);
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Can not correlate reply destination"));
            return;
        }
        
        Assert.fail("Missing " + IllegalArgumentException.class + " because of missing correlation key");
    }
    
    @Test
    public void testNoReplyDestinationFound() throws JMSException {
        JmsReplyMessageSender sender = new JmsReplyMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
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
            Assert.assertTrue(e.getMessage().startsWith("Unable to locate JMS reply destination with correlation key"));
            return;
        }
        
        Assert.fail("Missing " + IllegalArgumentException.class + " because no reply destination found");
    }
    
    @Test
    public void testSendEmptyMessage() throws JMSException {
        JmsReplyMessageSender sender = new JmsReplyMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        try {
            sender.send(null);
        } catch(IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Message is empty - unable to send empty message");
            return;
        }
        
        Assert.fail("Missing " + IllegalArgumentException.class + " because of sending empty message");
    }
    
}
