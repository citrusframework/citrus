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
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.testng.Assert;
import org.testng.annotations.Test;

import jakarta.jms.*;
import java.util.HashMap;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class JmsEndpointProducerTest extends AbstractTestNGUnitTest {

    private ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
    private Connection connection = Mockito.mock(Connection.class);
    private Session session = Mockito.mock(Session.class);
    private Destination destination = Mockito.mock(Destination.class);
    private Queue destinationQueue = Mockito.mock(Queue.class);
    private MessageProducer messageProducer = Mockito.mock(MessageProducer.class);
    
    private JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
    
    @Test
    public void testSendMessageWithJmsTemplate() {
        JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.getEndpointConfiguration().setJmsTemplate(jmsTemplate);
        
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(jmsTemplate, connectionFactory, destination, messageProducer);

        when(jmsTemplate.getDefaultDestination()).thenReturn(destination);

        endpoint.createProducer().send(message, context);

        verify(jmsTemplate).send(eq(destination), any(MessageCreator.class));
    }

    @Test
    public void testSendMessageWithDestination() throws JMSException {
        JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);
        
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(jmsTemplate, connectionFactory, destination, messageProducer, connection, session);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createProducer(destination)).thenReturn(messageProducer);

        when(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).thenReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, Object>()));

        when(session.getTransacted()).thenReturn(false);

        endpoint.createProducer().send(message, context);

        verify(messageProducer).send((TextMessage)any());
    }
    
    @Test
    public void testSendMessageWithDestinationName() throws JMSException {
        JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestinationName("myDestination");
        
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(jmsTemplate, connectionFactory, destination, messageProducer, connection, session);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createProducer(destinationQueue)).thenReturn(messageProducer);

        when(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).thenReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, Object>()));

        when(session.getTransacted()).thenReturn(false);

        when(session.createQueue("myDestination")).thenReturn(destinationQueue);

        endpoint.createProducer().send(message, context);

        verify(messageProducer).send((TextMessage)any());
    }
    
    @Test
    public void testSendEmptyMessage() throws JMSException {
        JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);
        
        try {
            endpoint.createProducer().send(null, context);
        } catch(IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Message is empty - unable to send empty message");
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because of sending empty message");
    }
    
}
