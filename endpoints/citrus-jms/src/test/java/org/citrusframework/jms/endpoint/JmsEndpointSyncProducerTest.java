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

import java.util.HashMap;
import java.util.Map;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TextMessage;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.message.correlation.ObjectStore;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class JmsEndpointSyncProducerTest extends AbstractTestNGUnitTest {

    private final ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
    private final Connection connection = Mockito.mock(Connection.class);
    private final Session session = Mockito.mock(Session.class);
    private final Destination destination = Mockito.mock(Destination.class);
    private final Queue destinationQueue = Mockito.mock(Queue.class);
    private final MessageConsumer messageConsumer = Mockito.mock(MessageConsumer.class);
    private final MessageProducer messageProducer = Mockito.mock(MessageProducer.class);
    private final Queue replyDestinationQueue = Mockito.mock(Queue.class);
    private final TemporaryQueue tempReplyQueue = Mockito.mock(TemporaryQueue.class);

    private int retryCount = 0;

    @Test
    public void testSendMessageWithReplyDestination() throws JMSException {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);
        endpoint.getEndpointConfiguration().setReplyDestination(replyDestinationQueue);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);

        reset(connectionFactory, destination, connection, session, messageConsumer, messageProducer);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createConsumer(replyDestinationQueue, "JMSCorrelationID = '123456789'")).thenReturn(messageConsumer);
        when(messageConsumer.receive(anyLong())).thenReturn(jmsResponse);

        when(session.createProducer(destination)).thenReturn(messageProducer);

        when(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).thenReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, Object>()));

        endpoint.createProducer().send(message, context);

        verify(messageProducer).send((TextMessage)any());
        verify(connection).start();
    }

    @Test
    public void testSendMessageWithReplyDestinationName() throws JMSException {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestinationName("myDestination");
        endpoint.getEndpointConfiguration().setReplyDestinationName("replyDestination");

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);

        reset(connectionFactory, destination, connection, session, messageConsumer, messageProducer);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createQueue("replyDestination")).thenReturn(replyDestinationQueue);

        when(session.createConsumer(replyDestinationQueue, "JMSCorrelationID = '123456789'")).thenReturn(messageConsumer);
        when(messageConsumer.receive(anyLong())).thenReturn(jmsResponse);

        when(session.createProducer(destinationQueue)).thenReturn(messageProducer);

        when(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).thenReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, Object>()));

        when(session.createQueue("myDestination")).thenReturn(destinationQueue);

        endpoint.createProducer().send(message, context);

        verify(messageProducer).send((TextMessage)any());
        verify(connection).start();
    }

    @Test
    public void testSendMessageWithTemporaryReplyDestination() throws JMSException {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);

        reset(connectionFactory, destination, connection, session, messageConsumer, messageProducer, tempReplyQueue);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createTemporaryQueue()).thenReturn(tempReplyQueue);

        when(session.createConsumer(tempReplyQueue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(anyLong())).thenReturn(jmsResponse);

        when(session.createProducer(destination)).thenReturn(messageProducer);

        when(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).thenReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, Object>()));

        endpoint.createProducer().send(message, context);

        verify(connection).start();
        verify(messageProducer).send((TextMessage)any());
        verify(tempReplyQueue).delete();
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithReplyHandler() throws JMSException {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);
        endpoint.getEndpointConfiguration().setReplyDestination(replyDestinationQueue);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);

        reset(connectionFactory, destination, connection, session, messageConsumer, messageProducer);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createConsumer(replyDestinationQueue, "JMSCorrelationID = '123456789'")).thenReturn(messageConsumer);
        when(messageConsumer.receive(anyLong())).thenReturn(jmsResponse);

        when(session.createProducer(destination)).thenReturn(messageProducer);

        when(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).thenReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, Object>()));

        endpoint.createProducer().send(message, context);

        verify(messageProducer).send((TextMessage)any());
        verify(connection).start();
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithReplyMessageCorrelator() throws JMSException {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);
        endpoint.getEndpointConfiguration().setReplyDestination(replyDestinationQueue);

        MessageCorrelator correlator = new DefaultMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);

        reset(connectionFactory, destination, connection, session, messageConsumer, messageProducer);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createConsumer(replyDestinationQueue, "JMSCorrelationID = '123456789'")).thenReturn(messageConsumer);
        when(messageConsumer.receive(anyLong())).thenReturn(jmsResponse);

        when(session.createProducer(destination)).thenReturn(messageProducer);

        when(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).thenReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, Object>()));

        endpoint.createProducer().send(message, context);

        verify(connection).start();
        verify(messageProducer).send((TextMessage)any());
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Message is empty - unable to send empty message")
    public void testSendEmptyMessage() throws JMSException {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);
        endpoint.createProducer().send(null, context);
    }

    @Test
    public void testOnReplyMessage() {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        JmsSyncProducer jmsSyncProducer = (JmsSyncProducer)endpoint.createProducer();
        jmsSyncProducer.getCorrelationManager().saveCorrelationKey(
                endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(jmsSyncProducer.getName()),
                jmsSyncProducer.toString(), context);
        jmsSyncProducer.getCorrelationManager().store(jmsSyncProducer.toString(), message);

        Assert.assertEquals(jmsSyncProducer.receive(context), message);
    }

    @Test
    public void testOnReplyMessageWithCorrelatorKey() {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        JmsSyncProducer jmsSyncProducer = (JmsSyncProducer)endpoint.createProducer();
        jmsSyncProducer.getCorrelationManager().store(new DefaultMessageCorrelator().getCorrelationKey(message), message);

        Assert.assertEquals(jmsSyncProducer.receive(new DefaultMessageCorrelator().getCorrelationKey(message), context), message);
    }

    @Test
    public void testReplyMessageRetries() {
        retryCount = 0;

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();

        ((JmsSyncProducer)endpoint.createProducer()).getCorrelationManager().setObjectStore(new ObjectStore<Message>() {
            @Override
            public void add(String correlationKey, Message object) {
            }

            @Override
            public Message remove(String correlationKey) {
                retryCount++;
                if (retryCount == 5) {
                    return message;
                } else {
                    return null;
                }
            }
        });

        JmsSyncProducer jmsSyncProducer = (JmsSyncProducer)endpoint.createConsumer();
        jmsSyncProducer.getCorrelationManager().saveCorrelationKey(
                endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(jmsSyncProducer.getName()),
                jmsSyncProducer.toString(), context);

        Assert.assertEquals(retryCount, 0);
        Assert.assertEquals(jmsSyncProducer.receive(context, 2500), message);
        Assert.assertEquals(retryCount, 5);
    }

    @Test
    public void testReplyMessageRetriesExceeded() {
        retryCount = 0;

        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setPollingInterval(300L);

        ((JmsSyncProducer)endpoint.createProducer()).getCorrelationManager().setObjectStore(new ObjectStore<Message>() {
            @Override
            public void add(String correlationKey, Message object) {
            }

            @Override
            public Message remove(String correlationKey) {
                retryCount++;
                return null;
            }
        });


        JmsSyncProducer jmsSyncProducer = (JmsSyncProducer)endpoint.createConsumer();
        jmsSyncProducer.getCorrelationManager().saveCorrelationKey(
                endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(jmsSyncProducer.getName()),
                jmsSyncProducer.toString(), context);

        Assert.assertEquals(retryCount, 0);
        try {
            jmsSyncProducer.receive(context, 800);
            Assert.fail("Missing action timeout exception");
        } catch (ActionTimeoutException e) {
            Assert.assertEquals(retryCount, 4);
        }
    }

    @Test
    public void testIntervalGreaterThanTimeout() {
        retryCount = 0;

        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setPollingInterval(1000L);

        ((JmsSyncProducer)endpoint.createProducer()).getCorrelationManager().setObjectStore(new ObjectStore<Message>() {
            @Override
            public void add(String correlationKey, Message object) {
            }

            @Override
            public Message remove(String correlationKey) {
                retryCount++;
                return null;
            }
        });

        JmsSyncProducer jmsSyncProducer = (JmsSyncProducer)endpoint.createConsumer();
        jmsSyncProducer.getCorrelationManager().saveCorrelationKey(
                endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(jmsSyncProducer.getName()),
                jmsSyncProducer.toString(), context);

        Assert.assertEquals(retryCount, 0);
        try {
            jmsSyncProducer.receive(context, 250);
            Assert.fail("Missing action timeout exception");
        } catch (ActionTimeoutException e) {
            Assert.assertEquals(retryCount, 2);
        }
    }

    @Test
    public void testZeroTimeout() {
        retryCount = 0;

        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setPollingInterval(1000L);

        ((JmsSyncProducer)endpoint.createProducer()).getCorrelationManager().setObjectStore(new ObjectStore<Message>() {
            @Override
            public void add(String correlationKey, Message object) {
            }

            @Override
            public Message remove(String correlationKey) {
                retryCount++;
                return null;
            }
        });


        JmsSyncProducer jmsSyncProducer = (JmsSyncProducer)endpoint.createConsumer();
        jmsSyncProducer.getCorrelationManager().saveCorrelationKey(
                endpoint.getEndpointConfiguration().getCorrelator().getCorrelationKeyName(jmsSyncProducer.getName()),
                jmsSyncProducer.toString(), context);

        Assert.assertEquals(retryCount, 0);
        try {
            jmsSyncProducer.receive(context, 0);
            Assert.fail("Missing action timeout exception");
        } catch (ActionTimeoutException e) {
            Assert.assertEquals(retryCount, 1);
        }
    }
}
