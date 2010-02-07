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

import java.util.HashMap;
import java.util.Map;

import javax.jms.*;

import org.easymock.classextension.EasyMock;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.jms.core.JmsTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.message.DefaultReplyMessageCorrelator;
import com.consol.citrus.message.ReplyMessageCorrelator;

/**
 * @author Christoph Deppisch
 */
public class JmsSyncMessageReceiverTest {

    private ConnectionFactory connectionFactory = org.easymock.EasyMock.createMock(ConnectionFactory.class);
    private Connection connection = EasyMock.createMock(Connection.class);
    private Session session = EasyMock.createMock(Session.class);
    private Destination destination = EasyMock.createMock(Destination.class);
    private Destination replyDestination = EasyMock.createMock(Destination.class);
    private MessageConsumer messageConsumer = EasyMock.createMock(MessageConsumer.class);
    
    private JmsTemplate jmsTemplate = EasyMock.createMock(JmsTemplate.class);
    
    @Test
    public void testWithReplyDestination() throws JMSException {
        JmsSyncMessageReceiver receiver = new JmsSyncMessageReceiver();
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
        
        TextMessageImpl jmsTestMessage = new TextMessageImpl(
                "<TestRequest><Message>Hello World!</Message></TestRequest>", headers);
        jmsTestMessage.setJMSReplyTo(replyDestination);
        
        expect(messageConsumer.receive(5000L)).andReturn(jmsTestMessage).once();
        
        replay(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
        
        Message<?> receivedMessage = receiver.receive();
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        
        Assert.assertEquals(receiver.getReplyDestination(), replyDestination);
        
        verify(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
    }
    
    @Test
    public void testWithMessageCorrelator() throws JMSException {
        JmsSyncMessageReceiver receiver = new JmsSyncMessageReceiver();
        receiver.setConnectionFactory(connectionFactory);
        
        receiver.setDestination(destination);
        
        ReplyMessageCorrelator correlator = new DefaultReplyMessageCorrelator();
        receiver.setCorrelator(correlator);
        
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
        
        TextMessageImpl jmsTestMessage = new TextMessageImpl(
                "<TestRequest><Message>Hello World!</Message></TestRequest>", headers);
        jmsTestMessage.setJMSReplyTo(replyDestination);
        
        expect(messageConsumer.receive(5000L)).andReturn(jmsTestMessage).once();
        
        replay(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
        
        Message<?> receivedMessage = receiver.receive();
        Assert.assertEquals(receivedMessage.getPayload(), controlMessage.getPayload());
        
        Assert.assertNull(receiver.getReplyDestination(
                correlator.getCorrelationKey("wrongIdKey")));
        Assert.assertEquals(receiver.getReplyDestination(
                correlator.getCorrelationKey(receivedMessage)), replyDestination);
        
        verify(jmsTemplate, connectionFactory, destination, connection, session, messageConsumer);
    }
}
