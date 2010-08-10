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

import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 */
public class JmsMessageReceiverTest {

    private ConnectionFactory connectionFactory = org.easymock.EasyMock.createMock(ConnectionFactory.class);
    private Connection connection = EasyMock.createMock(Connection.class);
    private Session session = EasyMock.createMock(Session.class);
    private Destination destination = EasyMock.createMock(Destination.class);
    private Queue destinationQueue = EasyMock.createMock(Queue.class);
    private MessageConsumer messageConsumer = EasyMock.createMock(MessageConsumer.class);
    
    private JmsTemplate jmsTemplate = EasyMock.createMock(JmsTemplate.class);
    
    @Test
    public void testReceiveMessageWithJmsTemplate() {
        JmsMessageReceiver receiver = new JmsMessageReceiver();
        receiver.setJmsTemplate(jmsTemplate);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        final Message<String> controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(controlHeaders)
                                .build();
        
        reset(jmsTemplate, connectionFactory, destination);

        jmsTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(jmsTemplate.getDefaultDestination()).andReturn(destination).atLeastOnce();
        
        expect(jmsTemplate.receiveAndConvert()).andReturn(controlMessage);

        replay(jmsTemplate, connectionFactory, destination);
        
        Message<?> receivedMessage = receiver.receive();
        Assert.assertTrue(receivedMessage.equals(controlMessage));
        
        verify(jmsTemplate, connectionFactory, destination);
    }
    
    @Test
    public void testWithDestination() throws JMSException {
        JmsMessageReceiver receiver = new JmsMessageReceiver();
        receiver.setConnectionFactory(connectionFactory);
        
        receiver.setDestination(destination);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        final Message<String> controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(controlHeaders)
                                .build();
        
        Map<String, String> headers = new HashMap<String, String>();
        
        reset(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        expect(session.getTransacted()).andReturn(false).once();
        expect(session.getAcknowledgeMode()).andReturn(Session.AUTO_ACKNOWLEDGE).once();
        
        expect(session.createConsumer(destination, null)).andReturn(messageConsumer).once();
        
        connection.start();
        expectLastCall().once();
        
        expect(messageConsumer.receive(5000L)).andReturn(new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", headers)).once();
        
        replay(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
        
        Message<?> receivedMessage = receiver.receive();
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        
        verify(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
    }
    
    @Test
    public void testReceiveMessageWithDestinationName() throws JMSException {
        JmsMessageReceiver receiver = new JmsMessageReceiver();
        receiver.setConnectionFactory(connectionFactory);
        
        receiver.setDestinationName("myDestination");
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        final Message<String> controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(controlHeaders)
                                .build();
        
        Map<String, String> headers = new HashMap<String, String>();
        
        reset(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        expect(session.getTransacted()).andReturn(false).once();
        expect(session.getAcknowledgeMode()).andReturn(Session.AUTO_ACKNOWLEDGE).once();
        
        expect(session.createQueue("myDestination")).andReturn(destinationQueue).once();
        
        expect(session.createConsumer(destinationQueue, null)).andReturn(messageConsumer).once();
        
        expect(messageConsumer.receive(5000L)).andReturn(new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", headers)).once();
        
        connection.start();
        expectLastCall().once();
        
        replay(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
        
        Message<?> receivedMessage = receiver.receive();
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        
        verify(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
    }
    
    @Test
    public void testReceiveMessageTimeout() throws JMSException {
        JmsMessageReceiver receiver = new JmsMessageReceiver();
        receiver.setConnectionFactory(connectionFactory);
        
        receiver.setDestination(destination);
        
        reset(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        expect(session.getTransacted()).andReturn(false).once();
        expect(session.getAcknowledgeMode()).andReturn(Session.AUTO_ACKNOWLEDGE).once();
        
        expect(session.createConsumer(destination, null)).andReturn(messageConsumer).once();
        
        expect(messageConsumer.receive(5000L)).andReturn(null).once();
        
        connection.start();
        expectLastCall().once();
        
        replay(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
        
        try {
            receiver.receive();
        } catch(ActionTimeoutException e) {
            Assert.assertTrue(e.getMessage().startsWith("Action timed out while receiving message on"));
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because of receiveing message timeout");
    }
    
    @Test
    public void testWithCustomTimeout() throws JMSException {
        JmsMessageReceiver receiver = new JmsMessageReceiver();
        receiver.setConnectionFactory(connectionFactory);
        
        receiver.setDestination(destination);
        
        receiver.setReceiveTimeout(10000L);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        final Message<String> controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(controlHeaders)
                                .build();
        
        Map<String, String> headers = new HashMap<String, String>();
        
        reset(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        expect(session.getTransacted()).andReturn(false).once();
        expect(session.getAcknowledgeMode()).andReturn(Session.AUTO_ACKNOWLEDGE).once();
        
        expect(session.createConsumer(destination, null)).andReturn(messageConsumer).once();
        
        connection.start();
        expectLastCall().once();
        
        expect(messageConsumer.receive(10000L)).andReturn(new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", headers)).once();
        
        replay(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
        
        Message<?> receivedMessage = receiver.receive();
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        
        verify(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
    }
    
    public void testWithMessageHeaders() throws JMSException {
        JmsMessageReceiver receiver = new JmsMessageReceiver();
        receiver.setConnectionFactory(connectionFactory);
        
        receiver.setDestination(destination);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        final Message<String> controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(controlHeaders)
                                .build();
        
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Operation", "sayHello");
        
        reset(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        expect(session.getTransacted()).andReturn(false).once();
        expect(session.getAcknowledgeMode()).andReturn(Session.AUTO_ACKNOWLEDGE).once();
        
        expect(session.createConsumer(destination, null)).andReturn(messageConsumer).once();
        
        connection.start();
        expectLastCall().once();
        
        expect(messageConsumer.receive(5000L)).andReturn(new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", headers)).once();
        
        replay(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
        
        Message<?> receivedMessage = receiver.receive();
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        Assert.assertTrue(receivedMessage.getHeaders().containsKey("Operation"));
        Assert.assertTrue(receivedMessage.getHeaders().get("Operation").equals("sayHello"));
        
        verify(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
    }
    
    @Test
    public void testWithMessageSelector() throws JMSException {
        JmsMessageReceiver receiver = new JmsMessageReceiver();
        receiver.setConnectionFactory(connectionFactory);
        
        receiver.setDestination(destination);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        final Message<String> controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(controlHeaders)
                                .build();
        
        Map<String, String> headers = new HashMap<String, String>();
        
        reset(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        expect(session.getTransacted()).andReturn(false).once();
        expect(session.getAcknowledgeMode()).andReturn(Session.AUTO_ACKNOWLEDGE).once();
        
        expect(session.createConsumer(destination, "Operation = 'sayHello'")).andReturn(messageConsumer).once();
        
        connection.start();
        expectLastCall().once();
        
        expect(messageConsumer.receive(5000L)).andReturn(new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", headers)).once();
        
        replay(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
        
        Message<?> receivedMessage = receiver.receiveSelected("Operation = 'sayHello'");
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        
        verify(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
    }
    
    @Test
    public void testWithMessageSelectorAndCustomTimeout() throws JMSException {
        JmsMessageReceiver receiver = new JmsMessageReceiver();
        receiver.setConnectionFactory(connectionFactory);
        
        receiver.setDestination(destination);
        receiver.setReceiveTimeout(10000L);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        final Message<String> controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(controlHeaders)
                                .build();
        
        Map<String, String> headers = new HashMap<String, String>();
        
        reset(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        expect(session.getTransacted()).andReturn(false).once();
        expect(session.getAcknowledgeMode()).andReturn(Session.AUTO_ACKNOWLEDGE).once();
        
        expect(session.createConsumer(destination, "Operation = 'sayHello'")).andReturn(messageConsumer).once();
        
        connection.start();
        expectLastCall().once();
        
        expect(messageConsumer.receive(10000L)).andReturn(new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", headers)).once();
        
        replay(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
        
        Message<?> receivedMessage = receiver.receiveSelected("Operation = 'sayHello'");
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        
        verify(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
    }
}
