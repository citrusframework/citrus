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

package com.consol.citrus.jms;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

import java.util.HashMap;
import java.util.Map;

import javax.jms.*;

import org.easymock.classextension.EasyMock;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.jms.core.JmsTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.message.*;

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
        
        JmsReplyDestinationHolder replyDestinationHolder = org.easymock.EasyMock.createMock(JmsReplyDestinationHolder.class);
        sender.setReplyDestinationHolder(replyDestinationHolder);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(jmsTemplate, connectionFactory, messageProducer, replyDestinationHolder);

        expect(replyDestinationHolder.getReplyDestination()).andReturn(replyDestination).once();
        
        jmsTemplate.convertAndSend(replyDestination, message);
        expectLastCall().once();
        
        replay(jmsTemplate, connectionFactory, messageProducer, replyDestinationHolder);
        
        sender.send(message);
        
        verify(jmsTemplate, connectionFactory, messageProducer, replyDestinationHolder);
    }
    
    @Test
    public void testSendMessageWithConnectionFactory() throws JMSException {
        JmsReplyMessageSender sender = new JmsReplyMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        JmsReplyDestinationHolder replyDestinationHolder = org.easymock.EasyMock.createMock(JmsReplyDestinationHolder.class);
        sender.setReplyDestinationHolder(replyDestinationHolder);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(jmsTemplate, connectionFactory, replyDestinationHolder, messageProducer, connection, session);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();

        expect(replyDestinationHolder.getReplyDestination()).andReturn(replyDestination).once();
        
        expect(session.createProducer(replyDestination)).andReturn(messageProducer).once();
        messageProducer.send((TextMessage)anyObject());
        expectLastCall().once();
        
        expect(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).andReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, String>()));
        
        expect(session.getTransacted()).andReturn(false).once();
        
        replay(jmsTemplate, connectionFactory, replyDestinationHolder, messageProducer, connection, session);
        
        sender.send(message);
        
        verify(jmsTemplate, connectionFactory, replyDestinationHolder, messageProducer, connection, session);
    }
    
    @Test
    public void testSendMessageWithReplyMessageCorrelator() throws JMSException {
        JmsReplyMessageSender sender = new JmsReplyMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        JmsReplyDestinationHolder replyDestinationHolder = org.easymock.EasyMock.createMock(JmsReplyDestinationHolder.class);
        sender.setReplyDestinationHolder(replyDestinationHolder);
        
        ReplyMessageCorrelator correlator = new DefaultReplyMessageCorrelator();
        sender.setCorrelator(correlator);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR, "123456789");
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(jmsTemplate, connectionFactory, replyDestinationHolder, messageProducer, connection, session);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();

        expect(replyDestinationHolder.getReplyDestination("springintegration_id = '123456789'")).andReturn(replyDestination).once();
        
        expect(session.createProducer(replyDestination)).andReturn(messageProducer).once();
        messageProducer.send((TextMessage)anyObject());
        expectLastCall().once();
        
        expect(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).andReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, String>()));
        
        expect(session.getTransacted()).andReturn(false).once();
        
        replay(jmsTemplate, connectionFactory, replyDestinationHolder, messageProducer, connection, session);
        
        sender.send(message);
        
        verify(jmsTemplate, connectionFactory, replyDestinationHolder, messageProducer, connection, session);
    }
    
    @Test
    public void testSendMessageWithMissingCorrelatorKey() throws JMSException {
        JmsReplyMessageSender sender = new JmsReplyMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        JmsReplyDestinationHolder replyDestinationHolder = org.easymock.EasyMock.createMock(JmsReplyDestinationHolder.class);
        sender.setReplyDestinationHolder(replyDestinationHolder);
        
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
        JmsReplyMessageSender sender = new JmsReplyMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        JmsReplyDestinationHolder replyDestinationHolder = org.easymock.EasyMock.createMock(JmsReplyDestinationHolder.class);
        sender.setReplyDestinationHolder(replyDestinationHolder);
        
        ReplyMessageCorrelator correlator = new DefaultReplyMessageCorrelator();
        sender.setCorrelator(correlator);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR, "123456789");
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(replyDestinationHolder);

        expect(replyDestinationHolder.getReplyDestination("springintegration_id = '123456789'")).andReturn(null).once();

        replay(replyDestinationHolder);
        
        try {
            sender.send(message);
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Not able to find temporary reply destination"));
            verify(replyDestinationHolder);
            return;
        }
        
        Assert.fail("Missing " + IllegalArgumentException.class + " because no reply destiantion found");
    }
    
    @Test
    public void testSendEmptyMessage() throws JMSException {
        JmsReplyMessageSender sender = new JmsReplyMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        try {
            sender.send(null);
        } catch(IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Can not send empty message");
            return;
        }
        
        Assert.fail("Missing " + IllegalArgumentException.class + " because of sending empty message");
    }
}
