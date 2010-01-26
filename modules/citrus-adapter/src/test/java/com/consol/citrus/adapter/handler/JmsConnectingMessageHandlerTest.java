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

package com.consol.citrus.adapter.handler;

import static org.easymock.EasyMock.*;

import java.util.*;

import javax.jms.*;
import javax.jms.Queue;

import org.easymock.EasyMock;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.adapter.handler.JmsConnectingMessageHandler.JmsMessageCallback;

/**
 * @author Christoph Deppisch
 */
public class JmsConnectingMessageHandlerTest {

    private ConnectionFactory connectionFactory = EasyMock.createMock(ConnectionFactory.class);
    private Connection connection = EasyMock.createMock(Connection.class);
    private Session session = EasyMock.createMock(Session.class);
    private MessageConsumer messageConsumer = EasyMock.createMock(MessageConsumer.class);
    private MessageProducer messageProducer = EasyMock.createMock(MessageProducer.class);
    private Destination sendDestination = EasyMock.createMock(Destination.class);
    private Queue sendDestinationQueue = EasyMock.createMock(Queue.class);
    private Destination replyDestination = EasyMock.createMock(Destination.class);
    private Queue replyDestinationQueue = EasyMock.createMock(Queue.class);
    private TemporaryQueue tempReplyQueue = EasyMock.createMock(TemporaryQueue.class);
    
    @Test
    public void testMessageHandler() throws JMSException {
        JmsConnectingMessageHandler messageHandler = new JmsConnectingMessageHandler();
        messageHandler.setConnectionFactory(connectionFactory);
        messageHandler.setDestination(sendDestination);
        messageHandler.setReplyDestination(replyDestination);

        Map<String, String> requestHeaders = new HashMap<String, String>();
        Map<String, String> responseHeaders = new HashMap<String, String>();

        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);
        TextMessage jmsRequest = new TextMessageImpl("<TestRequest>Hello World!</TestRequest>", requestHeaders);
        
        reset(connectionFactory, connection, session, messageConsumer, messageProducer);
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        expect(session.createConsumer(replyDestination, "JMSCorrelationID = '123456789'")).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(jmsResponse).once();
        
        expect(session.createProducer(sendDestination)).andReturn(messageProducer).once();
        messageProducer.send(jmsRequest);
        expectLastCall().once();
        expect(session.createTextMessage("<TestRequest>Hello World!</TestRequest>")).andReturn(jmsRequest).once();
        
        replay(connectionFactory, connection, session, messageConsumer, messageProducer);
        
        Message<?> responseMessage = messageHandler.handleMessage(
                MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>").build());
        
        Assert.assertEquals(responseMessage.getPayload(), "<TestResponse>Hello World!</TestResponse>");
        
        verify(connectionFactory, connection, session, messageConsumer, messageProducer);
    }
    
    @Test
    public void testMessageHandlerDestinationName() throws JMSException {
        JmsConnectingMessageHandler messageHandler = new JmsConnectingMessageHandler();
        messageHandler.setConnectionFactory(connectionFactory);
        messageHandler.setDestinationName("sendDestination");
        messageHandler.setReplyDestinationName("replyDestination");

        Map<String, String> requestHeaders = new HashMap<String, String>();
        Map<String, String> responseHeaders = new HashMap<String, String>();

        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);
        TextMessage jmsRequest = new TextMessageImpl("<TestRequest>Hello World!</TestRequest>", requestHeaders);
        
        reset(connectionFactory, connection, session, messageConsumer, messageProducer);
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        
        expect(session.createQueue("sendDestination")).andReturn(sendDestinationQueue).once();
        expect(session.createQueue("replyDestination")).andReturn(replyDestinationQueue).once();
        
        expect(session.createConsumer(replyDestinationQueue, "JMSCorrelationID = '123456789'")).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(jmsResponse).once();
        
        expect(session.createProducer(sendDestinationQueue)).andReturn(messageProducer).once();
        messageProducer.send(jmsRequest);
        expectLastCall().once();
        expect(session.createTextMessage("<TestRequest>Hello World!</TestRequest>")).andReturn(jmsRequest).once();
        
        replay(connectionFactory, connection, session, messageConsumer, messageProducer);
        
        Message<?> responseMessage = messageHandler.handleMessage(
                MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>").build());
        
        Assert.assertEquals(responseMessage.getPayload(), "<TestResponse>Hello World!</TestResponse>");
        
        verify(connectionFactory, connection, session, messageConsumer, messageProducer);
    }
    
    @Test
    public void testMessageHandlerTemporaryReplyDestination() throws JMSException {
        JmsConnectingMessageHandler messageHandler = new JmsConnectingMessageHandler();
        messageHandler.setConnectionFactory(connectionFactory);
        messageHandler.setDestination(sendDestination);

        Map<String, String> requestHeaders = new HashMap<String, String>();
        Map<String, String> responseHeaders = new HashMap<String, String>();

        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);
        TextMessage jmsRequest = new TextMessageImpl("<TestRequest>Hello World!</TestRequest>", requestHeaders);
        
        reset(connectionFactory, connection, session, messageConsumer, messageProducer, tempReplyQueue);
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        
        expect(session.createTemporaryQueue()).andReturn(tempReplyQueue).once();
        expect(session.createConsumer(tempReplyQueue)).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(jmsResponse).once();
        
        expect(session.createProducer(sendDestination)).andReturn(messageProducer).once();
        messageProducer.send(jmsRequest);
        expectLastCall().once();
        expect(session.createTextMessage("<TestRequest>Hello World!</TestRequest>")).andReturn(jmsRequest).once();
        
        tempReplyQueue.delete();
        expectLastCall().once();
        
        replay(connectionFactory, connection, session, messageConsumer, messageProducer, tempReplyQueue);
        
        Message<?> responseMessage = messageHandler.handleMessage(
                MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>").build());
        
        Assert.assertEquals(responseMessage.getPayload(), "<TestResponse>Hello World!</TestResponse>");
        
        verify(connectionFactory, connection, session, messageConsumer, messageProducer, tempReplyQueue);
    }
    
    @Test
    public void testMessageHandlerJmsMessageCallback() throws JMSException {
        JmsConnectingMessageHandler messageHandler = new JmsConnectingMessageHandler();
        messageHandler.setConnectionFactory(connectionFactory);
        messageHandler.setDestination(sendDestination);
        messageHandler.setReplyDestination(replyDestination);
        
        JmsMessageCallback jmsMessageCallback = EasyMock.createMock(JmsMessageCallback.class);
        messageHandler.setMessageCallback(jmsMessageCallback);

        Map<String, String> requestHeaders = new HashMap<String, String>();
        Map<String, String> responseHeaders = new HashMap<String, String>();

        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);
        TextMessage jmsRequest = new TextMessageImpl("<TestRequest>Hello World!</TestRequest>", requestHeaders);
        
        reset(connectionFactory, connection, session, messageConsumer, messageProducer, jmsMessageCallback);
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        expect(session.createConsumer(replyDestination, "JMSCorrelationID = '123456789'")).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(jmsResponse).once();
        
        expect(session.createProducer(sendDestination)).andReturn(messageProducer).once();
        
        jmsMessageCallback.doWithMessage((javax.jms.Message)anyObject(), (Message<?>)anyObject());
        expectLastCall().once();
        
        messageProducer.send(jmsRequest);
        expectLastCall().once();
        expect(session.createTextMessage("<TestRequest>Hello World!</TestRequest>")).andReturn(jmsRequest).once();
        
        replay(connectionFactory, connection, session, messageConsumer, messageProducer, jmsMessageCallback);
        
        Message<?> responseMessage = messageHandler.handleMessage(
                MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>").build());
        
        Assert.assertEquals(responseMessage.getPayload(), "<TestResponse>Hello World!</TestResponse>");
        
        verify(connectionFactory, connection, session, messageConsumer, messageProducer, jmsMessageCallback);
    }
    
    @Test
    public void testMessageHandlerNoReplyMessage() throws JMSException {
        JmsConnectingMessageHandler messageHandler = new JmsConnectingMessageHandler();
        messageHandler.setConnectionFactory(connectionFactory);
        messageHandler.setDestination(sendDestination);
        messageHandler.setReplyDestination(replyDestination);

        Map<String, String> requestHeaders = new HashMap<String, String>();

        TextMessage jmsRequest = new TextMessageImpl("<TestRequest>Hello World!</TestRequest>", requestHeaders);
        
        reset(connectionFactory, connection, session, messageConsumer, messageProducer);
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        expect(session.createConsumer(replyDestination, "JMSCorrelationID = '123456789'")).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(null).once();
        
        expect(session.createProducer(sendDestination)).andReturn(messageProducer).once();
        messageProducer.send(jmsRequest);
        expectLastCall().once();
        expect(session.createTextMessage("<TestRequest>Hello World!</TestRequest>")).andReturn(jmsRequest).once();
        
        replay(connectionFactory, connection, session, messageConsumer, messageProducer);
        
        Message<?> responseMessage = messageHandler.handleMessage(
                MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>").build());
        
        Assert.assertNull(responseMessage);
        
        verify(connectionFactory, connection, session, messageConsumer, messageProducer);
    }
    
    @Test
    public void testMessageHandlerWithFallbackMessageHandler() throws JMSException {
        JmsConnectingMessageHandler messageHandler = new JmsConnectingMessageHandler();
        messageHandler.setConnectionFactory(connectionFactory);
        messageHandler.setDestination(sendDestination);
        messageHandler.setReplyDestination(replyDestination);
        
        Map<String, String> requestHeaders = new HashMap<String, String>();

        TextMessage jmsRequest = new TextMessageImpl("<TestRequest>Hello World!</TestRequest>", requestHeaders);
        
        StaticResponseProducingMessageHandler fallbackMessageHandler = new StaticResponseProducingMessageHandler();
        fallbackMessageHandler.setMessageHeader(new HashMap<String, Object>());
        fallbackMessageHandler.setMessagePayload("<StaticTestResponse>Hello World!</StaticTestResponse>");
        messageHandler.setFallbackMessageHandlerDelegate(fallbackMessageHandler);
        
        reset(connectionFactory, connection, session, messageConsumer, messageProducer);
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        expect(session.createConsumer(replyDestination, "JMSCorrelationID = '123456789'")).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(null).once();
        
        expect(session.createProducer(sendDestination)).andReturn(messageProducer).once();
        messageProducer.send(jmsRequest);
        expectLastCall().once();
        expect(session.createTextMessage("<TestRequest>Hello World!</TestRequest>")).andReturn(jmsRequest).once();
        
        replay(connectionFactory, connection, session, messageConsumer, messageProducer);
        
        Message<?> responseMessage = messageHandler.handleMessage(
                MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>").build());
        
        Assert.assertEquals(responseMessage.getPayload(), "<StaticTestResponse>Hello World!</StaticTestResponse>");
        
        verify(connectionFactory, connection, session, messageConsumer, messageProducer);
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
