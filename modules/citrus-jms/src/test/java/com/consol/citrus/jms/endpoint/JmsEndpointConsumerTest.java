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

package com.consol.citrus.jms.endpoint;

import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
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
public class JmsEndpointConsumerTest extends AbstractTestNGUnitTest {

    private ConnectionFactory connectionFactory = org.easymock.EasyMock.createMock(ConnectionFactory.class);
    private Connection connection = EasyMock.createMock(Connection.class);
    private Session session = EasyMock.createMock(Session.class);
    private Destination destination = EasyMock.createMock(Destination.class);
    private Queue destinationQueue = EasyMock.createMock(Queue.class);
    private MessageConsumer messageConsumer = EasyMock.createMock(MessageConsumer.class);
    
    private JmsTemplate jmsTemplate = EasyMock.createMock(JmsTemplate.class);

    @Test
    public void testReceiveMessageWithJmsTemplate() {
        JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.getEndpointConfiguration().setJmsTemplate(jmsTemplate);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(jmsTemplate, connectionFactory, destination);

        jmsTemplate.setReceiveTimeout(5000L);
        expectLastCall().once();
        
        expect(jmsTemplate.getDefaultDestination()).andReturn(destination).atLeastOnce();
        
        expect(jmsTemplate.receive()).andReturn(new TextMessageImpl(controlMessage.getPayload(String.class), controlHeaders));

        replay(jmsTemplate, connectionFactory, destination);
        
        Message receivedMessage = endpoint.createConsumer().receive(context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());

        verify(jmsTemplate, connectionFactory, destination);
    }
    
    @Test
    public void testWithDestination() throws JMSException {
        JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);
        
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> headers = new HashMap<String, Object>();
        
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
        
        Message receivedMessage = endpoint.createConsumer().receive(context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        
        verify(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
    }
    
    @Test
    public void testReceiveMessageWithDestinationName() throws JMSException {
        JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestinationName("myDestination");
        
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> headers = new HashMap<String, Object>();
        
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
        
        Message receivedMessage = endpoint.createConsumer().receive(context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        
        verify(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
    }
    
    @Test
    public void testReceiveMessageTimeout() throws JMSException {
        JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);
        
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
            endpoint.createConsumer().receive(context);
        } catch(ActionTimeoutException e) {
            Assert.assertTrue(e.getMessage().startsWith("Action timed out while receiving JMS message on"));
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because of receiveing message timeout");
    }
    
    @Test
    public void testWithCustomTimeout() throws JMSException {
        JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);

        endpoint.getEndpointConfiguration().setTimeout(10000L);
        
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> headers = new HashMap<String, Object>();
        
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
        
        Message receivedMessage = endpoint.createConsumer().receive(context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        
        verify(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
    }

    @Test
    public void testWithMessageHeaders() throws JMSException {
        JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        Map<String, Object> headers = new HashMap<String, Object>();
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
        
        Message receivedMessage = endpoint.createConsumer().receive(context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        Assert.assertNotNull(receivedMessage.getHeader("Operation"));
        Assert.assertTrue(receivedMessage.getHeader("Operation").equals("sayHello"));
        
        verify(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
    }
    
    @Test
    public void testWithMessageSelector() throws JMSException {
        JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);
        
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> headers = new HashMap<String, Object>();
        
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
        
        Message receivedMessage = endpoint.createConsumer().receive("Operation = 'sayHello'", context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        
        verify(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
    }
    
    @Test
    public void testWithMessageSelectorAndCustomTimeout() throws JMSException {
        JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);
        endpoint.getEndpointConfiguration().setTimeout(10000L);
        
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> headers = new HashMap<String, Object>();
        
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
        
        Message receivedMessage = endpoint.createConsumer().receive("Operation = 'sayHello'", context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        
        verify(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
    }
}
