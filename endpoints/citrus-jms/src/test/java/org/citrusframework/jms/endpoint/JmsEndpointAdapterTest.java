/*
 * Copyright 2006-2014 the original author or authors.
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
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TextMessage;
import java.util.HashMap;

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class JmsEndpointAdapterTest extends AbstractTestNGUnitTest {

    private ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
    private Connection connection = Mockito.mock(Connection.class);
    private Session session = Mockito.mock(Session.class);
    private Destination destination = Mockito.mock(Destination.class);
    private MessageConsumer messageConsumer = Mockito.mock(MessageConsumer.class);
    private MessageProducer messageProducer = Mockito.mock(MessageProducer.class);
    private TemporaryQueue tempReplyQueue = Mockito.mock(TemporaryQueue.class);

    private JmsEndpointAdapter endpointAdapter;
    private JmsSyncEndpointConfiguration endpointConfiguration;

    @BeforeMethod
    public void setup() {
        endpointConfiguration = new JmsSyncEndpointConfiguration();
        endpointConfiguration.setConnectionFactory(connectionFactory);
        endpointConfiguration.setDestination(destination);
        endpointConfiguration.setTimeout(250L);

        endpointAdapter = new JmsEndpointAdapter(endpointConfiguration);
        endpointAdapter.setTestContextFactory(testContextFactory);
    }

    @Test
    public void testEndpointAdapter() throws JMSException {
        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", new HashMap<String, Object>());

        reset(connectionFactory, connection, session, messageConsumer, messageProducer, tempReplyQueue);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createTemporaryQueue()).thenReturn(tempReplyQueue);

        when(session.createConsumer(tempReplyQueue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(anyLong())).thenReturn(jmsResponse);

        when(session.createProducer(destination)).thenReturn(messageProducer);

        when(session.createTextMessage("<TestMessage><text>Hi!</text></TestMessage>")).thenReturn(
                new TextMessageImpl("<TestMessage><text>Hi!</text></TestMessage>", new HashMap<String, Object>()));


        Message response = endpointAdapter.handleMessage(new DefaultMessage("<TestMessage><text>Hi!</text></TestMessage>"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getPayload(String.class), "<TestResponse>Hello World!</TestResponse>");

        verify(messageProducer).send((TextMessage)any());
        verify(connection).start();
        verify(tempReplyQueue).delete();

    }

    @Test
    public void testNoResponse() throws JMSException {
        reset(connectionFactory, connection, session, messageConsumer, messageProducer, tempReplyQueue);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createTemporaryQueue()).thenReturn(tempReplyQueue);

        when(session.createConsumer(tempReplyQueue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(anyLong())).thenReturn(null);

        when(session.createProducer(destination)).thenReturn(messageProducer);

        when(session.createTextMessage("<TestMessage><text>Hi!</text></TestMessage>")).thenReturn(
                new TextMessageImpl("<TestMessage><text>Hi!</text></TestMessage>", new HashMap<String, Object>()));


        Assert.assertNull(endpointAdapter.handleMessage(new DefaultMessage("<TestMessage><text>Hi!</text></TestMessage>")));

        verify(messageProducer).send((TextMessage)any());
        verify(connection).start();
        verify(tempReplyQueue).delete();
    }

}
