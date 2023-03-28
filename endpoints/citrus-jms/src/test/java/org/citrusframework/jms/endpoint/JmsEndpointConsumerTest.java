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

package org.citrusframework.jms.endpoint;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class JmsEndpointConsumerTest extends AbstractTestNGUnitTest {

    private ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
    private Connection connection = Mockito.mock(Connection.class);
    private Session session = Mockito.mock(Session.class);
    private Destination destination = Mockito.mock(Destination.class);
    private Queue destinationQueue = Mockito.mock(Queue.class);
    private MessageConsumer messageConsumer = Mockito.mock(MessageConsumer.class);

    private JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);

    @Test
    public void testReceiveMessageWithJmsTemplate() {
        JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.getEndpointConfiguration().setJmsTemplate(jmsTemplate);

        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(jmsTemplate, connectionFactory, destination);

        when(jmsTemplate.getDefaultDestination()).thenReturn(destination);
        when(jmsTemplate.receive(destination)).thenReturn(new TextMessageImpl(controlMessage.getPayload(String.class), controlHeaders));

        Message receivedMessage = endpoint.createConsumer().receive(context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());

        verify(jmsTemplate).setReceiveTimeout(5000L);
    }

    @Test
    public void testWithDestination() throws JMSException {
        JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> headers = new HashMap<String, Object>();

        reset(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);
        when(session.getTransacted()).thenReturn(false);
        when(session.getAcknowledgeMode()).thenReturn(Session.AUTO_ACKNOWLEDGE);

        when(session.createConsumer(destination, null)).thenReturn(messageConsumer);
        when(messageConsumer.receive(5000L)).thenReturn(new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", headers));

        Message receivedMessage = endpoint.createConsumer().receive(context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());

        verify(connection).start();
    }

    @Test
    public void testReceiveMessageWithDestinationName() throws JMSException {
        JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestinationName("myDestination");

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> headers = new HashMap<String, Object>();

        reset(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);
        when(session.getTransacted()).thenReturn(false);
        when(session.getAcknowledgeMode()).thenReturn(Session.AUTO_ACKNOWLEDGE);

        when(session.createQueue("myDestination")).thenReturn(destinationQueue);
        when(session.createConsumer(destinationQueue, null)).thenReturn(messageConsumer);
        when(messageConsumer.receive(5000L)).thenReturn(new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", headers));

        Message receivedMessage = endpoint.createConsumer().receive(context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());

        verify(connection).start();
    }

    @Test
    public void testReceiveMessageTimeout() throws JMSException {
        JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);

        reset(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);
        when(session.getTransacted()).thenReturn(false);
        when(session.getAcknowledgeMode()).thenReturn(Session.AUTO_ACKNOWLEDGE);

        when(session.createConsumer(destination, null)).thenReturn(messageConsumer);

        when(messageConsumer.receive(5000L)).thenReturn(null);

        try {
            endpoint.createConsumer().receive(context);
            Assert.fail("Missing " + CitrusRuntimeException.class + " because of receiving message timeout");
        } catch(ActionTimeoutException e) {
            Assert.assertTrue(e.getMessage().startsWith("Action timeout after 5000 milliseconds. Failed to receive message on endpoint"));
            verify(connection).start();
        }
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

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);
        when(session.getTransacted()).thenReturn(false);
        when(session.getAcknowledgeMode()).thenReturn(Session.AUTO_ACKNOWLEDGE);

        when(session.createConsumer(destination, null)).thenReturn(messageConsumer);
        when(messageConsumer.receive(10000L)).thenReturn(new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", headers));

        Message receivedMessage = endpoint.createConsumer().receive(context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());

        verify(connection).start();
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

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);
        when(session.getTransacted()).thenReturn(false);
        when(session.getAcknowledgeMode()).thenReturn(Session.AUTO_ACKNOWLEDGE);

        when(session.createConsumer(destination, null)).thenReturn(messageConsumer);
        when(messageConsumer.receive(5000L)).thenReturn(new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", headers));

        Message receivedMessage = endpoint.createConsumer().receive(context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        Assert.assertNotNull(receivedMessage.getHeader("Operation"));
        Assert.assertTrue(receivedMessage.getHeader("Operation").equals("sayHello"));

        verify(connection).start();
    }

    @Test
    public void testWithMessageSelector() throws JMSException {
        JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> headers = new HashMap<String, Object>();

        reset(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);
        when(session.getTransacted()).thenReturn(false);
        when(session.getAcknowledgeMode()).thenReturn(Session.AUTO_ACKNOWLEDGE);

        when(session.createConsumer(destination, "Operation = 'sayHello'")).thenReturn(messageConsumer);
        when(messageConsumer.receive(5000L)).thenReturn(new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", headers));

        Message receivedMessage = endpoint.createConsumer().receive("Operation = 'sayHello'", context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());

        verify(connection).start();
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

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);
        when(session.getTransacted()).thenReturn(false);
        when(session.getAcknowledgeMode()).thenReturn(Session.AUTO_ACKNOWLEDGE);

        when(session.createConsumer(destination, "Operation = 'sayHello'")).thenReturn(messageConsumer);
        when(messageConsumer.receive(10000L)).thenReturn(new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", headers));

        Message receivedMessage = endpoint.createConsumer().receive("Operation = 'sayHello'", context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());

        verify(connection).start();
    }
}
