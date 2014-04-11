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

package com.consol.citrus.adapter.handler;

import com.consol.citrus.adapter.handler.JmsConnectingMessageHandler.JmsMessageCallback;
import org.easymock.EasyMock;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @deprecated since Citrus 1.4
 */
@Deprecated
public class JmsConnectingMessageHandlerTest {

    private ConnectionFactory connectionFactory = EasyMock.createMock(ConnectionFactory.class);
    private Connection connection = EasyMock.createMock(Connection.class);
    private Session session = EasyMock.createMock(Session.class);
    private MessageConsumer messageConsumer = EasyMock.createMock(MessageConsumer.class);
    private MessageProducer messageProducer = EasyMock.createMock(MessageProducer.class);
    private Destination sendDestination = EasyMock.createMock(Destination.class);
    private Queue sendDestinationQueue = EasyMock.createMock(Queue.class);
    private Destination replyDestination = EasyMock.createMock(Destination.class);
    private Queue replyDestinationQueue = EasyMock.createMock(Queue.class);
    private TemporaryQueue tempReplyQueue = EasyMock.createMock(TemporaryQueue.class);
    
    @Test
    public void testMessageHandler() throws JMSException {
        JmsConnectingMessageHandler messageHandler = new JmsConnectingMessageHandler();
        messageHandler.setConnectionFactory(connectionFactory);
        messageHandler.setDestination(sendDestination);
        messageHandler.setReplyDestination(replyDestination);

        Map<String, String> requestHeaders = new HashMap<String, String>();
        Map<String, String> responseHeaders = new HashMap<String, String>();

        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);
        TextMessage jmsRequest = new TextMessageImpl("<TestRequest>Hello World!</TestRequest>", requestHeaders);
        
        reset(connectionFactory, connection, session, messageConsumer, messageProducer);
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        expect(session.createConsumer(replyDestination, "JMSCorrelationID = '123456789'")).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(jmsResponse).once();
        
        expect(session.createProducer(sendDestination)).andReturn(messageProducer).once();
        messageProducer.send(jmsRequest);
        expectLastCall().once();
        expect(session.createTextMessage("<TestRequest>Hello World!</TestRequest>")).andReturn(jmsRequest).once();
        
        replay(connectionFactory, connection, session, messageConsumer, messageProducer);
        
        Message<?> responseMessage = messageHandler.handleMessage(
                MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>").build());
        
        Assert.assertEquals(responseMessage.getPayload(), "<TestResponse>Hello World!</TestResponse>");
        
        verify(connectionFactory, connection, session, messageConsumer, messageProducer);
    }
    
    @Test
    public void testMessageHandlerDestinationName() throws JMSException {
        JmsConnectingMessageHandler messageHandler = new JmsConnectingMessageHandler();
        messageHandler.setConnectionFactory(connectionFactory);
        messageHandler.setDestinationName("sendDestination");
        messageHandler.setReplyDestinationName("replyDestination");

        Map<String, String> requestHeaders = new HashMap<String, String>();
        Map<String, String> responseHeaders = new HashMap<String, String>();

        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);
        TextMessage jmsRequest = new TextMessageImpl("<TestRequest>Hello World!</TestRequest>", requestHeaders);
        
        reset(connectionFactory, connection, session, messageConsumer, messageProducer);
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        
        expect(session.createQueue("sendDestination")).andReturn(sendDestinationQueue).once();
        expect(session.createQueue("replyDestination")).andReturn(replyDestinationQueue).once();
        
        expect(session.createConsumer(replyDestinationQueue, "JMSCorrelationID = '123456789'")).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(jmsResponse).once();
        
        expect(session.createProducer(sendDestinationQueue)).andReturn(messageProducer).once();
        messageProducer.send(jmsRequest);
        expectLastCall().once();
        expect(session.createTextMessage("<TestRequest>Hello World!</TestRequest>")).andReturn(jmsRequest).once();
        
        replay(connectionFactory, connection, session, messageConsumer, messageProducer);
        
        Message<?> responseMessage = messageHandler.handleMessage(
                MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>").build());
        
        Assert.assertEquals(responseMessage.getPayload(), "<TestResponse>Hello World!</TestResponse>");
        
        verify(connectionFactory, connection, session, messageConsumer, messageProducer);
    }
    
    @Test
    public void testMessageHandlerTemporaryReplyDestination() throws JMSException {
        JmsConnectingMessageHandler messageHandler = new JmsConnectingMessageHandler();
        messageHandler.setConnectionFactory(connectionFactory);
        messageHandler.setDestination(sendDestination);

        Map<String, String> requestHeaders = new HashMap<String, String>();
        Map<String, String> responseHeaders = new HashMap<String, String>();

        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);
        TextMessage jmsRequest = new TextMessageImpl("<TestRequest>Hello World!</TestRequest>", requestHeaders);
        
        reset(connectionFactory, connection, session, messageConsumer, messageProducer, tempReplyQueue);
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        
        expect(session.createTemporaryQueue()).andReturn(tempReplyQueue).once();
        expect(session.createConsumer(tempReplyQueue)).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(jmsResponse).once();
        
        expect(session.createProducer(sendDestination)).andReturn(messageProducer).once();
        messageProducer.send(jmsRequest);
        expectLastCall().once();
        expect(session.createTextMessage("<TestRequest>Hello World!</TestRequest>")).andReturn(jmsRequest).once();
        
        tempReplyQueue.delete();
        expectLastCall().once();
        
        replay(connectionFactory, connection, session, messageConsumer, messageProducer, tempReplyQueue);
        
        Message<?> responseMessage = messageHandler.handleMessage(
                MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>").build());
        
        Assert.assertEquals(responseMessage.getPayload(), "<TestResponse>Hello World!</TestResponse>");
        
        verify(connectionFactory, connection, session, messageConsumer, messageProducer, tempReplyQueue);
    }
    
    @Test
    public void testMessageHandlerJmsMessageCallback() throws JMSException {
        JmsConnectingMessageHandler messageHandler = new JmsConnectingMessageHandler();
        messageHandler.setConnectionFactory(connectionFactory);
        messageHandler.setDestination(sendDestination);
        messageHandler.setReplyDestination(replyDestination);
        
        JmsMessageCallback jmsMessageCallback = EasyMock.createMock(JmsMessageCallback.class);
        messageHandler.setMessageCallback(jmsMessageCallback);

        Map<String, String> requestHeaders = new HashMap<String, String>();
        Map<String, String> responseHeaders = new HashMap<String, String>();

        TextMessage jmsResponse = new TextMessageImpl("<TestResponse>Hello World!</TestResponse>", responseHeaders);
        TextMessage jmsRequest = new TextMessageImpl("<TestRequest>Hello World!</TestRequest>", requestHeaders);
        
        reset(connectionFactory, connection, session, messageConsumer, messageProducer, jmsMessageCallback);
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        expect(session.createConsumer(replyDestination, "JMSCorrelationID = '123456789'")).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(jmsResponse).once();
        
        expect(session.createProducer(sendDestination)).andReturn(messageProducer).once();
        
        jmsMessageCallback.doWithMessage((javax.jms.Message)anyObject(), (Message<?>)anyObject());
        expectLastCall().once();
        
        messageProducer.send(jmsRequest);
        expectLastCall().once();
        expect(session.createTextMessage("<TestRequest>Hello World!</TestRequest>")).andReturn(jmsRequest).once();
        
        replay(connectionFactory, connection, session, messageConsumer, messageProducer, jmsMessageCallback);
        
        Message<?> responseMessage = messageHandler.handleMessage(
                MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>").build());
        
        Assert.assertEquals(responseMessage.getPayload(), "<TestResponse>Hello World!</TestResponse>");
        
        verify(connectionFactory, connection, session, messageConsumer, messageProducer, jmsMessageCallback);
    }
    
    @Test
    public void testMessageHandlerNoReplyMessage() throws JMSException {
        JmsConnectingMessageHandler messageHandler = new JmsConnectingMessageHandler();
        messageHandler.setConnectionFactory(connectionFactory);
        messageHandler.setDestination(sendDestination);
        messageHandler.setReplyDestination(replyDestination);
        messageHandler.setFallbackMessageHandlerDelegate(null);

        Map<String, String> requestHeaders = new HashMap<String, String>();

        TextMessage jmsRequest = new TextMessageImpl("<TestRequest>Hello World!</TestRequest>", requestHeaders);
        
        reset(connectionFactory, connection, session, messageConsumer, messageProducer);
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        expect(session.createConsumer(replyDestination, "JMSCorrelationID = '123456789'")).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(null).once();
        
        expect(session.createProducer(sendDestination)).andReturn(messageProducer).once();
        messageProducer.send(jmsRequest);
        expectLastCall().once();
        expect(session.createTextMessage("<TestRequest>Hello World!</TestRequest>")).andReturn(jmsRequest).once();
        
        replay(connectionFactory, connection, session, messageConsumer, messageProducer);
        
        Message<?> responseMessage = messageHandler.handleMessage(
                MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>").build());
        
        Assert.assertNull(responseMessage);
        
        verify(connectionFactory, connection, session, messageConsumer, messageProducer);
    }
    
    @Test
    public void testMessageHandlerWithFallbackMessageHandler() throws JMSException {
        JmsConnectingMessageHandler messageHandler = new JmsConnectingMessageHandler();
        messageHandler.setConnectionFactory(connectionFactory);
        messageHandler.setDestination(sendDestination);
        messageHandler.setReplyDestination(replyDestination);
        
        Map<String, String> requestHeaders = new HashMap<String, String>();

        TextMessage jmsRequest = new TextMessageImpl("<TestRequest>Hello World!</TestRequest>", requestHeaders);
        
        StaticResponseProducingMessageHandler fallbackMessageHandler = new StaticResponseProducingMessageHandler();
        fallbackMessageHandler.setMessageHeader(new HashMap<String, Object>());
        fallbackMessageHandler.setMessagePayload("<StaticTestResponse>Hello World!</StaticTestResponse>");
        messageHandler.setFallbackMessageHandlerDelegate(fallbackMessageHandler);
        
        reset(connectionFactory, connection, session, messageConsumer, messageProducer);
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        expect(session.createConsumer(replyDestination, "JMSCorrelationID = '123456789'")).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(null).once();
        
        expect(session.createProducer(sendDestination)).andReturn(messageProducer).once();
        messageProducer.send(jmsRequest);
        expectLastCall().once();
        expect(session.createTextMessage("<TestRequest>Hello World!</TestRequest>")).andReturn(jmsRequest).once();
        
        replay(connectionFactory, connection, session, messageConsumer, messageProducer);
        
        Message<?> responseMessage = messageHandler.handleMessage(
                MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>").build());
        
        Assert.assertEquals(responseMessage.getPayload(), "<StaticTestResponse>Hello World!</StaticTestResponse>");
        
        verify(connectionFactory, connection, session, messageConsumer, messageProducer);
    }

}
