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

import com.consol.citrus.jms.message.JmsMessage;
import com.consol.citrus.message.*;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class JmsEndpointSyncConsumerTest extends AbstractTestNGUnitTest {

    private ConnectionFactory connectionFactory = org.easymock.EasyMock.createMock(ConnectionFactory.class);
    private Connection connection = EasyMock.createMock(Connection.class);
    private Session session = EasyMock.createMock(Session.class);
    private Destination destination = EasyMock.createMock(Destination.class);
    private Destination replyDestination = EasyMock.createMock(Destination.class);
    private MessageConsumer messageConsumer = EasyMock.createMock(MessageConsumer.class);
    private MessageProducer messageProducer = EasyMock.createMock(MessageProducer.class);

    private JmsTemplate jmsTemplate = EasyMock.createMock(JmsTemplate.class);

    @Test
    public void testWithReplyDestination() throws JMSException {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        endpoint.getEndpointConfiguration().setDestination(destination);
        
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> headers = new HashMap<String, Object>();
        
        reset(connectionFactory, destination, connection, session, messageConsumer);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        expect(session.getTransacted()).andReturn(false).once();
        expect(session.getAcknowledgeMode()).andReturn(Session.AUTO_ACKNOWLEDGE).once();
        
        expect(session.createConsumer(destination, null)).andReturn(messageConsumer).once();
        
        connection.start();
        expectLastCall().once();
        
        TextMessageImpl jmsTestMessage = new TextMessageImpl(
                "<TestRequest><Message>Hello World!</Message></TestRequest>", headers);
        jmsTestMessage.setJMSReplyTo(replyDestination);
        
        expect(messageConsumer.receive(5000L)).andReturn(jmsTestMessage).once();
        
        replay(connectionFactory, destination, connection, session, messageConsumer);

        JmsSyncConsumer jmsSyncConsumer = (JmsSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = jmsSyncConsumer.receive(context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        
        Assert.assertEquals(jmsSyncConsumer.findReplyDestination(), replyDestination);
        
        verify(connectionFactory, destination, connection, session, messageConsumer);
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

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        expect(session.getTransacted()).andReturn(false).once();
        expect(session.getAcknowledgeMode()).andReturn(Session.AUTO_ACKNOWLEDGE).once();
        
        expect(session.createConsumer(destination, null)).andReturn(messageConsumer).once();
        
        connection.start();
        expectLastCall().once();
        
        TextMessageImpl jmsTestMessage = new TextMessageImpl(
                "<TestRequest><Message>Hello World!</Message></TestRequest>", headers);
        jmsTestMessage.setJMSReplyTo(replyDestination);
        
        expect(messageConsumer.receive(5000L)).andReturn(jmsTestMessage).once();
        
        replay(connectionFactory, destination, connection, session, messageConsumer);

        JmsSyncConsumer jmsSyncConsumer = (JmsSyncConsumer) endpoint.createConsumer();
        Message receivedMessage = jmsSyncConsumer.receive(context);
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        
        Assert.assertNull(jmsSyncConsumer.findReplyDestination(
                correlator.getCorrelationKey("wrongIdKey")));
        Assert.assertEquals(jmsSyncConsumer.findReplyDestination(
                correlator.getCorrelationKey(receivedMessage)), replyDestination);
        
        verify(connectionFactory, destination, connection, session, messageConsumer);
    }

    @Test
    public void testSendMessageWithJmsTemplate() {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setJmsTemplate(jmsTemplate);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(jmsTemplate, connectionFactory, messageProducer);

        jmsTemplate.send(eq(replyDestination), anyObject(MessageCreator.class));
        expectLastCall().once();

        replay(jmsTemplate, connectionFactory, messageProducer);

        JmsSyncConsumer jmsSyncConsumer = (JmsSyncConsumer)endpoint.createConsumer();
        jmsSyncConsumer.saveReplyDestination(new JmsMessage("").setReplyTo(replyDestination));
        jmsSyncConsumer.send(message, context);

        verify(jmsTemplate, connectionFactory, messageProducer);
    }

    @Test
    public void testSendMessageWithConnectionFactory() throws JMSException {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(jmsTemplate, connectionFactory, messageProducer, connection, session);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();

        expect(session.createProducer(replyDestination)).andReturn(messageProducer).once();
        messageProducer.send((TextMessage)anyObject());
        expectLastCall().once();

        expect(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).andReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, Object>()));

        expect(session.getTransacted()).andReturn(false).once();

        replay(jmsTemplate, connectionFactory, messageProducer, connection, session);

        JmsSyncConsumer jmsSyncConsumer = (JmsSyncConsumer)endpoint.createConsumer();
        jmsSyncConsumer.saveReplyDestination(new JmsMessage("").setReplyTo(replyDestination));
        jmsSyncConsumer.send(message, context);

        verify(jmsTemplate, connectionFactory, messageProducer, connection, session);
    }

    @Test
    public void testSendMessageWithReplyMessageCorrelator() throws JMSException {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        MessageCorrelator correlator = new DefaultMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        JmsMessage requestMessage = new JmsMessage("")
                .setReplyTo(replyDestination);

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(MessageHeaders.SYNC_MESSAGE_CORRELATOR, requestMessage.getHeader(MessageHeaders.ID));
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(jmsTemplate, connectionFactory, messageProducer, connection, session);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();

        expect(session.createProducer(replyDestination)).andReturn(messageProducer).once();
        messageProducer.send((TextMessage)anyObject());
        expectLastCall().once();

        expect(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).andReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, Object>()));

        expect(session.getTransacted()).andReturn(false).once();

        replay(jmsTemplate, connectionFactory, messageProducer, connection, session);

        JmsSyncConsumer jmsSyncConsumer = (JmsSyncConsumer)endpoint.createConsumer();
        jmsSyncConsumer.saveReplyDestination(requestMessage);
        jmsSyncConsumer.send(message, context);

        verify(jmsTemplate, connectionFactory, messageProducer, connection, session);
    }

    @Test
    public void testSendMessageWithMissingCorrelatorKey() throws JMSException {
        JmsSyncEndpoint endpoint = new JmsSyncEndpoint();
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);

        JmsMessage requestMessage = new JmsMessage("")
                .setReplyTo(replyDestination);

        MessageCorrelator correlator = new DefaultMessageCorrelator();
        endpoint.getEndpointConfiguration().setCorrelator(correlator);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        try {
            JmsSyncConsumer jmsSyncConsumer = (JmsSyncConsumer)endpoint.createConsumer();
            jmsSyncConsumer.saveReplyDestination(requestMessage);
            jmsSyncConsumer.send(message, context);
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Can not correlate reply destination"));
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

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(MessageHeaders.SYNC_MESSAGE_CORRELATOR, "123456789");
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        try {
            JmsSyncConsumer jmsSyncConsumer = (JmsSyncConsumer)endpoint.createConsumer();
            jmsSyncConsumer.send(message, context);
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unable to locate JMS reply destination with correlation key"));
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
