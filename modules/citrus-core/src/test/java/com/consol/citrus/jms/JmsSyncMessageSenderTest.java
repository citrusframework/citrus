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
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

import java.util.HashMap;
import java.util.Map;

import javax.jms.*;

import org.easymock.classextension.EasyMock;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;

/**
 * @author Christoph Deppisch
 */
public class JmsSyncMessageSenderTest {

    private ConnectionFactory connectionFactory = org.easymock.EasyMock.createMock(ConnectionFactory.class);
    private Connection connection = EasyMock.createMock(Connection.class);
    private Session session = EasyMock.createMock(Session.class);
    private Destination destination = EasyMock.createMock(Destination.class);
    private Queue destinationQueue = EasyMock.createMock(Queue.class);
    private MessageConsumer messageConsumer = EasyMock.createMock(MessageConsumer.class);
    private MessageProducer messageProducer = EasyMock.createMock(MessageProducer.class);
    private Queue replyDestinationQueue = EasyMock.createMock(Queue.class);
    private TemporaryQueue tempReplyQueue = EasyMock.createMock(TemporaryQueue.class);    
    
    @Test
    public void testSendMessageWithReplyDestination() throws JMSException {
        JmsSyncMessageSender sender = new JmsSyncMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        sender.setDestination(destination);
        sender.setReplyDestination(replyDestinationQueue);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        Map<String, String> responseHeaders = new HashMap<String, String>();
        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);
        
        reset(connectionFactory, destination, connection, session, messageConsumer, messageProducer);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();

        expect(session.createConsumer(replyDestinationQueue, "JMSCorrelationID = '123456789'")).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(jmsResponse).once();
        
        expect(session.createProducer(destination)).andReturn(messageProducer).once();
        messageProducer.send((TextMessage)anyObject());
        expectLastCall().once();
        
        expect(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).andReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, String>()));
        
        replay(connectionFactory, destination, connection, session, messageConsumer, messageProducer);
        
        sender.send(message);
        
        verify(connectionFactory, destination, connection, session, messageConsumer, messageProducer);
    }
    
    @Test
    public void testSendMessageWithReplyDestinationName() throws JMSException {
        JmsSyncMessageSender sender = new JmsSyncMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        sender.setDestinationName("myDestination");
        sender.setReplyDestinationName("replyDestination");
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();

        Map<String, String> responseHeaders = new HashMap<String, String>();
        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);
        
        reset(connectionFactory, destination, connection, session, messageConsumer, messageProducer);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();

        expect(session.createQueue("replyDestination")).andReturn(replyDestinationQueue).once();
        
        expect(session.createConsumer(replyDestinationQueue, "JMSCorrelationID = '123456789'")).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(jmsResponse).once();
        
        expect(session.createProducer(destinationQueue)).andReturn(messageProducer).once();
        messageProducer.send((TextMessage)anyObject());
        expectLastCall().once();
        
        expect(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).andReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, String>()));
        
        expect(session.createQueue("myDestination")).andReturn(destinationQueue).once();
        
        replay(connectionFactory, destination, connection, session, messageConsumer, messageProducer);
        
        sender.send(message);
        
        verify(connectionFactory, destination, connection, session, messageConsumer, messageProducer);
    }
    
    @Test
    public void testSendMessageWithTemporaryReplyDestination() throws JMSException {
        JmsSyncMessageSender sender = new JmsSyncMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        sender.setDestination(destination);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        Map<String, String> responseHeaders = new HashMap<String, String>();
        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);
        
        reset(connectionFactory, destination, connection, session, messageConsumer, messageProducer, tempReplyQueue);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();

        expect(session.createTemporaryQueue()).andReturn(tempReplyQueue).once();
        
        expect(session.createConsumer(tempReplyQueue)).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(jmsResponse).once();
        
        expect(session.createProducer(destination)).andReturn(messageProducer).once();
        messageProducer.send((TextMessage)anyObject());
        expectLastCall().once();
        
        expect(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).andReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, String>()));
        
        tempReplyQueue.delete();
        expectLastCall().once();
        
        replay(connectionFactory, destination, connection, session, messageConsumer, messageProducer, tempReplyQueue);
        
        sender.send(message);
        
        verify(connectionFactory, destination, connection, session, messageConsumer, messageProducer, tempReplyQueue);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithReplyHandler() throws JMSException {
        JmsSyncMessageSender sender = new JmsSyncMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        sender.setDestination(destination);
        sender.setReplyDestination(replyDestinationQueue);
        
        ReplyMessageHandler replyMessageHandler = org.easymock.EasyMock.createMock(ReplyMessageHandler.class);
        sender.setReplyMessageHandler(replyMessageHandler);
        
        reset(replyMessageHandler);
        
        replyMessageHandler.onReplyMessage((Message)anyObject());
        expectLastCall().once();
        
        replay(replyMessageHandler);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        Map<String, String> responseHeaders = new HashMap<String, String>();
        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);
        
        reset(connectionFactory, destination, connection, session, messageConsumer, messageProducer);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();

        expect(session.createConsumer(replyDestinationQueue, "JMSCorrelationID = '123456789'")).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(jmsResponse).once();
        
        expect(session.createProducer(destination)).andReturn(messageProducer).once();
        messageProducer.send((TextMessage)anyObject());
        expectLastCall().once();
        
        expect(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).andReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, String>()));
        
        replay(connectionFactory, destination, connection, session, messageConsumer, messageProducer);
        
        sender.send(message);
        
        verify(connectionFactory, destination, connection, session, messageConsumer, messageProducer);
        verify(replyMessageHandler);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithReplyMessageCorrelator() throws JMSException {
        JmsSyncMessageSender sender = new JmsSyncMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        sender.setDestination(destination);
        sender.setReplyDestination(replyDestinationQueue);

        ReplyMessageCorrelator correlator = new DefaultReplyMessageCorrelator();
        sender.setCorrelator(correlator);

        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        ReplyMessageHandler replyMessageHandler = org.easymock.EasyMock.createMock(ReplyMessageHandler.class);
        sender.setReplyMessageHandler(replyMessageHandler);
        
        Map<String, String> responseHeaders = new HashMap<String, String>();
        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);
        
        reset(connectionFactory, destination, connection, session, messageConsumer, messageProducer, replyMessageHandler);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();

        expect(session.createConsumer(replyDestinationQueue, "JMSCorrelationID = '123456789'")).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(jmsResponse).once();
        
        expect(session.createProducer(destination)).andReturn(messageProducer).once();
        messageProducer.send((TextMessage)anyObject());
        expectLastCall().once();
        
        expect(session.createTextMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")).andReturn(
                new TextMessageImpl("<TestRequest><Message>Hello World!</Message></TestRequest>", new HashMap<String, String>()));
        
        replyMessageHandler.onReplyMessage((Message)anyObject(), (String)anyObject());
        expectLastCall().once();
        
        replay(connectionFactory, destination, connection, session, messageConsumer, messageProducer, replyMessageHandler);
        
        sender.send(message);
        
        verify(connectionFactory, destination, connection, session, messageConsumer, messageProducer, replyMessageHandler);
    }
    
    @Test
    public void testSendEmptyMessage() throws JMSException {
        JmsSyncMessageSender sender = new JmsSyncMessageSender();
        sender.setConnectionFactory(connectionFactory);
        
        sender.setDestination(destination);
        
        try {
            sender.send(null);
        } catch(IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Can not send empty message");
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because of sending empty message");
    }
}
