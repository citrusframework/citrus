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
import static org.easymock.classextension.EasyMock.*;

import java.util.HashMap;
import java.util.Map;

import javax.jms.*;

import org.easymock.classextension.EasyMock;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.jms.core.JmsTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 */
public class JmsMessageSenderTest {

    private ConnectionFactory connectionFactory = org.easymock.EasyMock.createMock(ConnectionFactory.class);
    private Connection connection = EasyMock.createMock(Connection.class);
    private Session session = EasyMock.createMock(Session.class);
    private Destination destination = EasyMock.createMock(Destination.class);
    private Queue destinationQueue = EasyMock.createMock(Queue.class);
    private MessageProducer messageProducer = EasyMock.createMock(MessageProducer.class);
    
    private JmsTemplate jmsTemplate = EasyMock.createMock(JmsTemplate.class);
    
    @Test
    public void testSendMessageWithJmsTemplate() {
        JmsMessageSender sender = new JmsMessageSender();
        sender.setJmsTemplate(jmsTemplate);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(jmsTemplate, connectionFactory, destination, messageProducer);

        expect(jmsTemplate.getDefaultDestination()).andReturn(destination).atLeastOnce();
        
        jmsTemplate.convertAndSend(message);
        expectLastCall().once();
        
        replay(jmsTemplate, connectionFactory, destination, messageProducer);
        
        sender.send(message);
        
        verify(jmsTemplate, connectionFactory, destination, messageProducer);
    }
    
    @Test
    public void testSendMessageWithDestination() throws JMSException {
        JmsMessageSender sender = new JmsMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        sender.setDestination(destination);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(jmsTemplate, connectionFactory, destination, messageProducer, connection, session);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();

        expect(session.createProducer(destination)).andReturn(messageProducer).once();
        messageProducer.send((TextMessage)anyObject());
        expectLastCall().once();
        
        expect(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).andReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, String>()));
        
        expect(session.getTransacted()).andReturn(false).once();
        
        replay(jmsTemplate, connectionFactory, destination, messageProducer, connection, session);
        
        sender.send(message);
        
        verify(jmsTemplate, connectionFactory, destination, messageProducer, connection, session);
    }
    
    @Test
    public void testSendMessageWithDestinationName() throws JMSException {
        JmsMessageSender sender = new JmsMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        sender.setDestinationName("myDestination");
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(jmsTemplate, connectionFactory, destination, messageProducer, connection, session);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();

        expect(session.createProducer(destinationQueue)).andReturn(messageProducer).once();
        messageProducer.send((TextMessage)anyObject());
        expectLastCall().once();

        expect(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).andReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, String>()));
        
        expect(session.getTransacted()).andReturn(false).once();
        
        expect(session.createQueue("myDestination")).andReturn(destinationQueue).once();
        
        replay(jmsTemplate, connectionFactory, destination, messageProducer, connection, session);
        
        sender.send(message);
        
        verify(jmsTemplate, connectionFactory, destination, messageProducer, connection, session);
    }
    
    @Test
    public void testSendEmptyMessage() throws JMSException {
        JmsMessageSender sender = new JmsMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        sender.setDestination(destination);
        
        try {
            sender.send(null);
        } catch(IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Can not send empty message");
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because of sending empty message");
    }
}
