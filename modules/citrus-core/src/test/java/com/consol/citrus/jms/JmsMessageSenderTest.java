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

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

import java.util.*;

import javax.jms.*;
import javax.jms.Queue;

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
        
        reset(jmsTemplate, connectionFactory, destination);

        expect(jmsTemplate.getDefaultDestination()).andReturn(destination).atLeastOnce();
        
        jmsTemplate.convertAndSend(message);
        expectLastCall().once();
        
        replay(jmsTemplate, connectionFactory, destination);
        
        sender.send(message);
        
        verify(jmsTemplate, connectionFactory, destination);
    }
    
    @Test
    public void testSendMessageWithDestiantion() throws JMSException {
        JmsMessageSender sender = new JmsMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        sender.setDestination(destination);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(jmsTemplate, connectionFactory, destination, connection, session);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();

        expect(session.createProducer(destination)).andReturn(messageProducer).once();

        expect(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).andReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", null));
        
        expect(session.getTransacted()).andReturn(false).once();
        
        replay(jmsTemplate, connectionFactory, destination, connection, session);
        
        sender.send(message);
        
        verify(jmsTemplate, connectionFactory, destination, connection, session);
    }
    
    @Test
    public void testSendMessageWithDestiantionName() throws JMSException {
        JmsMessageSender sender = new JmsMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        sender.setDestinationName("myDestination");
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(jmsTemplate, connectionFactory, destination, connection, session);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();

        expect(session.createProducer(destinationQueue)).andReturn(messageProducer).once();

        expect(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).andReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", null));
        
        expect(session.getTransacted()).andReturn(false).once();
        
        expect(session.createQueue("myDestination")).andReturn(destinationQueue).once();
        
        replay(jmsTemplate, connectionFactory, destination, connection, session);
        
        sender.send(message);
        
        verify(jmsTemplate, connectionFactory, destination, connection, session);
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
    
    private static class TextMessageImpl implements TextMessage {
        private String payload = "";
        
        private Map<String, String> headers = new HashMap<String, String>();
        
        public TextMessageImpl(String payload, Map<String, String> headers) {
            this.payload = payload;
            this.headers = headers;
        }
        
        public void setStringProperty(String name, String value) throws JMSException {headers.put(name, value);}
        public void setShortProperty(String name, short value) throws JMSException {}
        public void setObjectProperty(String name, Object value) throws JMSException {}
        public void setLongProperty(String name, long value) throws JMSException {}
        public void setJMSType(String type) throws JMSException {}
        public void setJMSTimestamp(long timestamp) throws JMSException {}
        public void setJMSReplyTo(Destination replyTo) throws JMSException {}
        public void setJMSRedelivered(boolean redelivered) throws JMSException {}
        public void setJMSPriority(int priority) throws JMSException {}
        public void setJMSMessageID(String id) throws JMSException {}
        public void setJMSExpiration(long expiration) throws JMSException {}
        public void setJMSDestination(Destination destination) throws JMSException {}
        public void setJMSDeliveryMode(int deliveryMode) throws JMSException {}
        public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {}
        public void setJMSCorrelationID(String correlationID) throws JMSException {}
        public void setIntProperty(String name, int value) throws JMSException {}
        public void setFloatProperty(String name, float value) throws JMSException {}
        public void setDoubleProperty(String name, double value) throws JMSException {}
        public void setByteProperty(String name, byte value) throws JMSException {}
        public void setBooleanProperty(String name, boolean value) throws JMSException {}
        public boolean propertyExists(String name) throws JMSException {return false;}
        public String getStringProperty(String name) throws JMSException {return headers.get(name);}
        public short getShortProperty(String name) throws JMSException {return 0;}
        @SuppressWarnings("unchecked")
        public Enumeration getPropertyNames() throws JMSException {return null;}
        public Object getObjectProperty(String name) throws JMSException {return null;}
        public long getLongProperty(String name) throws JMSException {return 0;}
        public String getJMSType() throws JMSException {return null;}
        public long getJMSTimestamp() throws JMSException {return 0;}
        public Destination getJMSReplyTo() throws JMSException {return null;}
        public boolean getJMSRedelivered() throws JMSException {return false;}
        public int getJMSPriority() throws JMSException {return 0;}
        public String getJMSMessageID() throws JMSException {return "123456789";}
        public long getJMSExpiration() throws JMSException {return 0;}
        public Destination getJMSDestination() throws JMSException {return null;}
        public int getJMSDeliveryMode() throws JMSException {return 0;}
        public byte[] getJMSCorrelationIDAsBytes() throws JMSException {return null;}
        public String getJMSCorrelationID() throws JMSException {return null;}
        public int getIntProperty(String name) throws JMSException {return 0;}
        public float getFloatProperty(String name) throws JMSException {return 0;}
        public double getDoubleProperty(String name) throws JMSException {return 0;}
        public byte getByteProperty(String name) throws JMSException {return 0;}
        public boolean getBooleanProperty(String name) throws JMSException {return false;}
        public void clearProperties() throws JMSException {}
        public void clearBody() throws JMSException {}
        public void acknowledge() throws JMSException {}
        public void setText(String string) throws JMSException {this.payload = string;}
        public String getText() throws JMSException {return payload;}
    }
}
