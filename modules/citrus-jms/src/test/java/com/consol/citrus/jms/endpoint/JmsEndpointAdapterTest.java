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

package com.consol.citrus.jms.endpoint;

import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.jms.*;
import java.util.HashMap;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class JmsEndpointAdapterTest extends AbstractTestNGUnitTest {

    private ConnectionFactory connectionFactory = org.easymock.EasyMock.createMock(ConnectionFactory.class);
    private Connection connection = EasyMock.createMock(Connection.class);
    private Session session = EasyMock.createMock(Session.class);
    private Destination destination = EasyMock.createMock(Destination.class);
    private MessageConsumer messageConsumer = EasyMock.createMock(MessageConsumer.class);
    private MessageProducer messageProducer = EasyMock.createMock(MessageProducer.class);
    private TemporaryQueue tempReplyQueue = EasyMock.createMock(TemporaryQueue.class);

    private JmsEndpointAdapter endpointAdapter;
    private JmsSyncEndpointConfiguration endpointConfiguration;

    @Autowired
    private TestContextFactory testContextFactory;

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

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();

        expect(session.createTemporaryQueue()).andReturn(tempReplyQueue).once();

        expect(session.createConsumer(tempReplyQueue)).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(jmsResponse).once();

        expect(session.createProducer(destination)).andReturn(messageProducer).once();
        messageProducer.send((TextMessage)anyObject());
        expectLastCall().once();

        expect(session.createTextMessage("<TestMessage><text>Hi!</text></TestMessage>")).andReturn(
                new TextMessageImpl("<TestMessage><text>Hi!</text></TestMessage>", new HashMap<String, Object>()));

        connection.start();
        expectLastCall().once();

        tempReplyQueue.delete();
        expectLastCall().once();

        replay(connectionFactory, connection, session, messageConsumer, messageProducer, tempReplyQueue);

        Message response = endpointAdapter.handleMessage(new DefaultMessage("<TestMessage><text>Hi!</text></TestMessage>"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getPayload(String.class), "<TestResponse>Hello World!</TestResponse>");

        verify(connectionFactory, connection, session, messageConsumer, messageProducer, tempReplyQueue);
    }

    @Test
    public void testNoResponse() throws JMSException {
        reset(connectionFactory, connection, session, messageConsumer, messageProducer, tempReplyQueue);

        expect(connectionFactory.createConnection()).andReturn(connection).once();
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();

        expect(session.createTemporaryQueue()).andReturn(tempReplyQueue).once();

        expect(session.createConsumer(tempReplyQueue)).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyLong())).andReturn(null).once();

        expect(session.createProducer(destination)).andReturn(messageProducer).once();
        messageProducer.send((TextMessage)anyObject());
        expectLastCall().once();

        expect(session.createTextMessage("<TestMessage><text>Hi!</text></TestMessage>")).andReturn(
                new TextMessageImpl("<TestMessage><text>Hi!</text></TestMessage>", new HashMap<String, Object>()));

        connection.start();
        expectLastCall().once();

        tempReplyQueue.delete();
        expectLastCall().once();

        replay(connectionFactory, connection, session, messageConsumer, messageProducer, tempReplyQueue);

        Assert.assertNull(endpointAdapter.handleMessage(new DefaultMessage("<TestMessage><text>Hi!</text></TestMessage>")));

        verify(connectionFactory, connection, session, messageConsumer, messageProducer, tempReplyQueue);
    }

}
