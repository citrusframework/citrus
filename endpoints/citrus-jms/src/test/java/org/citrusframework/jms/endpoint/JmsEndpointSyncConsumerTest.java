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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jms.message.JmsMessage;
import org.citrusframework.message.*;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.testng.Assert;
import org.testng.annotations.Test;

import jakarta.jms.*;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class JmsEndpointSyncConsumerTest extends AbstractTestNGUnitTest {

    private ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
    private Connection connection = Mockito.mock(Connection.class);
    private Session session = Mockito.mock(Session.class);
    private Destination destination = Mockito.mock(Destination.class);
    private Destination replyDestination = Mockito.mock(Destination.class);
    private MessageConsumer messageConsumer = Mockito.mock(MessageConsumer.class);
    private MessageProducer messageProducer = Mockito.mock(MessageProducer.class);

    private JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);

    @Test
    public void testWithReplyDestination() throws JMSException {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);
        
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> headers = new HashMap<String, Object>();
        
        reset(connectionFactory, destination, connection, session, messageConsumer);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);
        when(session.getTransacted()).thenReturn(false);
        when(session.getAcknowledgeMode()).thenReturn(Session.AUTO_ACKNOWLEDGE);
        
        when(session.createConsumer(destination, null)).thenReturn(messageConsumer);

        TextMessageImpl jmsTestMessage = new TextMessageImpl(
                "<TestRequest><Message>Hello World!</Message></TestRequest>", headers);
        jmsTestMessage.setJMSReplyTo(replyDestination);

        when(messageConsumer.receive(5000L)).thenReturn(jmsTestMessage);

        JmsSyncConsumer jmsSyncConsumer = (JmsSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = jmsSyncConsumer.receive(context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());

        Assert.assertEquals(jmsSyncConsumer.getCorrelationManager().find(endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKey(receivedMessage),
                endpoint.getEndpointConfiguration().getTimeout()), replyDestination);

        verify(connection).start();
    }

    @Test
    public void testWithMessageCorrelator() throws JMSException {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);
        
        MessageCorrelator correlator = new DefaultMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> headers = new HashMap<String, Object>();
        
        reset(connectionFactory, destination, connection, session, messageConsumer);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);
        when(session.getTransacted()).thenReturn(false);
        when(session.getAcknowledgeMode()).thenReturn(Session.AUTO_ACKNOWLEDGE);
        
        when(session.createConsumer(destination, null)).thenReturn(messageConsumer);

        TextMessageImpl jmsTestMessage = new TextMessageImpl(
                "<TestRequest><Message>Hello World!</Message></TestRequest>", headers);
        jmsTestMessage.setJMSReplyTo(replyDestination);

        when(messageConsumer.receive(5000L)).thenReturn(jmsTestMessage);

        JmsSyncConsumer jmsSyncConsumer = (JmsSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = jmsSyncConsumer.receive(context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());

        Assert.assertNull(jmsSyncConsumer.getCorrelationManager().find(
                correlator.getCorrelationKey("wrongIdKey"), endpoint.getEndpointConfiguration().getTimeout()));
        Assert.assertEquals(jmsSyncConsumer.getCorrelationManager().find(
                correlator.getCorrelationKey(receivedMessage), endpoint.getEndpointConfiguration().getTimeout()), replyDestination);

        verify(connection).start();
    }

    @Test
    public void testSendMessageWithJmsTemplate() {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setJmsTemplate(jmsTemplate);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(jmsTemplate, connectionFactory, messageProducer);

        JmsSyncConsumer jmsSyncConsumer = (JmsSyncConsumer)endpoint.createConsumer();
        jmsSyncConsumer.saveReplyDestination(new JmsMessage().replyTo(replyDestination), context);
        jmsSyncConsumer.send(message, context);

        verify(jmsTemplate).send(eq(replyDestination), any(MessageCreator.class));
    }

    @Test
    public void testSendMessageWithConnectionFactory() throws JMSException {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(jmsTemplate, connectionFactory, messageProducer, connection, session);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createProducer(replyDestination)).thenReturn(messageProducer);

        when(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).thenReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, Object>()));

        when(session.getTransacted()).thenReturn(false);

        JmsSyncConsumer jmsSyncConsumer = (JmsSyncConsumer)endpoint.createConsumer();
        jmsSyncConsumer.saveReplyDestination(new JmsMessage().replyTo(replyDestination), context);
        jmsSyncConsumer.send(message, context);

        verify(messageProducer).send((TextMessage)any());
    }

    @Test
    public void testSendMessageWithReplyMessageCorrelator() throws JMSException {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        MessageCorrelator correlator = new DefaultMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        JmsMessage requestMessage = new JmsMessage()
                .replyTo(replyDestination);

        ((JmsSyncConsumer)endpoint.createConsumer()).getCorrelationManager().saveCorrelationKey(
                endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(endpoint.createConsumer().getName()),
                requestMessage.getId(), context);

        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(jmsTemplate, connectionFactory, messageProducer, connection, session);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createProducer(replyDestination)).thenReturn(messageProducer);

        when(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).thenReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, Object>()));

        when(session.getTransacted()).thenReturn(false);

        JmsSyncConsumer jmsSyncConsumer = (JmsSyncConsumer)endpoint.createConsumer();
        jmsSyncConsumer.saveReplyDestination(requestMessage, context);
        jmsSyncConsumer.send(message, context);

        verify(messageProducer).send((TextMessage)any());
    }

    @Test
    public void testNoCorrelationKeyFound() {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        JmsMessage requestMessage = new JmsMessage();

        MessageCorrelator correlator = new DefaultMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        JmsSyncEndpoint dummyEndpoint = new JmsSyncEndpoint();
        dummyEndpoint.setName("dummyEndpoint");
        ((JmsSyncConsumer)dummyEndpoint.createConsumer()).getCorrelationManager().saveCorrelationKey(
                dummyEndpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(dummyEndpoint.createConsumer().getName()),
                "123456789", context);

        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        try {
            JmsSyncConsumer jmsSyncConsumer = (JmsSyncConsumer) endpoint.createConsumer();
            jmsSyncConsumer.saveReplyDestination(requestMessage, context);
            jmsSyncConsumer.send(message, context);
        } catch(CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Failed to get correlation key"));
            return;
        }

        Assert.fail("Missing " + IllegalArgumentException.class + " because no reply destination found");
    }

    @Test
    public void testSendMessageWithMissingReplyTo() throws JMSException {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        JmsMessage requestMessage = new JmsMessage();

        MessageCorrelator correlator = new DefaultMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        ((JmsSyncConsumer)endpoint.createConsumer()).getCorrelationManager().saveCorrelationKey(
                endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(endpoint.createConsumer().getName()),
                "123456789", context);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        try {
            JmsSyncConsumer jmsSyncConsumer = (JmsSyncConsumer)endpoint.createConsumer();
            jmsSyncConsumer.saveReplyDestination(requestMessage, context);
            jmsSyncConsumer.send(message, context);
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Failed to find JMS reply destination"), e.getMessage());
            return;
        }

        Assert.fail("Missing " + IllegalArgumentException.class + " because of missing correlation key");
    }

    @Test
    public void testNoReplyDestinationFound() throws JMSException {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        MessageCorrelator correlator = new DefaultMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        ((JmsSyncConsumer)endpoint.createConsumer()).getCorrelationManager().saveCorrelationKey(
                endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(endpoint.createConsumer().getName()),
                "123456789", context);

        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        try {
            JmsSyncConsumer jmsSyncConsumer = (JmsSyncConsumer)endpoint.createConsumer();
            jmsSyncConsumer.send(message, context);
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Failed to find JMS reply destination for message correlation key"));
            return;
        }

        Assert.fail("Missing " + IllegalArgumentException.class + " because no reply destination found");
    }

    @Test
    public void testSendEmptyMessage() throws JMSException {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        try {
            JmsSyncConsumer jmsSyncConsumer = (JmsSyncConsumer)endpoint.createConsumer();
            jmsSyncConsumer.send(null, context);
        } catch(IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Message is empty - unable to send empty message");
            return;
        }

        Assert.fail("Missing " + IllegalArgumentException.class + " because of sending empty message");
    }
}
