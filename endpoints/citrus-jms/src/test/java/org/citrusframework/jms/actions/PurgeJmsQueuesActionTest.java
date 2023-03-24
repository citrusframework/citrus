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

package org.citrusframework.jms.actions;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.jms.endpoint.TextMessageImpl;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
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
public class PurgeJmsQueuesActionTest extends AbstractTestNGUnitTest {

    private final ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
    private final Connection connection = Mockito.mock(Connection.class);
    private final Session session = Mockito.mock(Session.class);
    private final MessageConsumer messageConsumer = Mockito.mock(MessageConsumer.class);

    private final Queue queue = Mockito.mock(Queue.class);

    @Test
    public void testPurgeWithQueueNamesConsumeMessages() throws JMSException {
        List<String> queueNames = new ArrayList<>();
        queueNames.add("myQueue");

        Map<String, Object> requestHeaders = new HashMap<>();
        TextMessage jmsRequest = new TextMessageImpl("<TestRequest>Hello World!</TestRequest>", requestHeaders);

        reset(connectionFactory, connection, session, messageConsumer);

        when(connectionFactory.createConnection()).thenReturn(connection);

        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createQueue("myQueue")).thenReturn(queue);

        when(session.createConsumer(queue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(100L)).thenReturn(jmsRequest).thenReturn(null);

        PurgeJmsQueuesAction purgeQueuesAction = new PurgeJmsQueuesAction.Builder()
                .connectionFactory(connectionFactory)
                .queueNames(queueNames)
                .build();
        purgeQueuesAction.execute(context);
        verify(connection).start();
    }

	@Test
	public void testPurgeWithQueueNamesNoMessages() throws JMSException {
		List<String> queueNames = new ArrayList<>();
		queueNames.add("myQueue");

		reset(connectionFactory, connection, session, messageConsumer);

        when(connectionFactory.createConnection()).thenReturn(connection);

        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createQueue("myQueue")).thenReturn(queue);

        when(session.createConsumer(queue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(100L)).thenReturn(null);

        PurgeJmsQueuesAction purgeQueuesAction = new PurgeJmsQueuesAction.Builder()
                .connectionFactory(connectionFactory)
                .queueNames(queueNames)
                .build();
		purgeQueuesAction.execute(context);
        verify(connection).start();
	}

	@Test
    public void testPurgeQueueNameList() throws JMSException {
        List<String> queueNames = new ArrayList<>();
        queueNames.add("myQueue");
        queueNames.add("anotherQueue");
        queueNames.add("someQueue");

        reset(connectionFactory, connection, session, messageConsumer);

        when(connectionFactory.createConnection()).thenReturn(connection);

        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createQueue("myQueue")).thenReturn(queue);
        when(session.createQueue("anotherQueue")).thenReturn(queue);
        when(session.createQueue("someQueue")).thenReturn(queue);

        when(session.createConsumer(queue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(100L)).thenReturn(null);

        PurgeJmsQueuesAction purgeQueuesAction = new PurgeJmsQueuesAction.Builder()
                .connectionFactory(connectionFactory)
                .queueNames(queueNames)
                .build();
        purgeQueuesAction.execute(context);
        verify(connection).start();
    }

	@Test
    public void testPurgeQueueList() throws JMSException {
        List<Queue> queues = new ArrayList<>();
        queues.add(queue);
        queues.add(queue);
        queues.add(queue);

        reset(connectionFactory, connection, session, messageConsumer, queue);

        when(connectionFactory.createConnection()).thenReturn(connection);

        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(queue.getQueueName()).thenReturn("myQueue");
        when(session.createConsumer(queue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(100L)).thenReturn(null);

        PurgeJmsQueuesAction purgeQueuesAction = new PurgeJmsQueuesAction.Builder()
                .connectionFactory(connectionFactory)
                .queues(queues)
                .build();
        purgeQueuesAction.execute(context);
        verify(connection).start();
    }

    @Test
    public void testPurgeQueueNameVariable() throws JMSException {
        context.getVariables().put("variableQueueName", "queueName");
        context.getVariables().put("secondQueueName", "secondQueue");

        List<String> queueNames = new ArrayList<>();
        queueNames.add("${variableQueueName}");
        queueNames.add("${secondQueueName}");

        reset(connectionFactory, connection, session, messageConsumer);

        when(connectionFactory.createConnection()).thenReturn(connection);

        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createQueue("queueName")).thenReturn(queue);
        when(session.createQueue("secondQueue")).thenReturn(queue);

        when(session.createConsumer(queue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(100L)).thenReturn(null);

        PurgeJmsQueuesAction purgeQueuesAction = new PurgeJmsQueuesAction.Builder()
                .connectionFactory(connectionFactory)
                .queueNames(queueNames)
                .build();
        purgeQueuesAction.execute(context);
        verify(connection).start();
    }

    @Test
    public void testPurgeWithVariableQueueNamesConsumeMessages() throws JMSException {
        context.getVariables().put("variableQueueName", "queueName");

        List<String> queueNames = new ArrayList<>();
        queueNames.add("${variableQueueName}");

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        TextMessage jmsRequest = new TextMessageImpl("<TestRequest>Hello World!</TestRequest>", requestHeaders);

        reset(connectionFactory, connection, session, messageConsumer);

        when(connectionFactory.createConnection()).thenReturn(connection);

        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createQueue("queueName")).thenReturn(queue);

        when(session.createConsumer(queue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(100L)).thenReturn(jmsRequest).thenReturn(null);

        PurgeJmsQueuesAction purgeQueuesAction = new PurgeJmsQueuesAction.Builder()
                .connectionFactory(connectionFactory)
                .queueNames(queueNames)
                .build();
        purgeQueuesAction.execute(context);
        verify(connection).start();
    }

    @Test
    public void testPurgeWithCustomTimeout() throws JMSException {
        List<String> queueNames = new ArrayList<>();
        queueNames.add("myQueue");

        reset(connectionFactory, connection, session, messageConsumer);

        when(connectionFactory.createConnection()).thenReturn(connection);

        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createQueue("myQueue")).thenReturn(queue);

        when(session.createConsumer(queue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(500L)).thenReturn(null);

        PurgeJmsQueuesAction purgeQueuesAction = new PurgeJmsQueuesAction.Builder()
                .connectionFactory(connectionFactory)
                .queueNames(queueNames)
                .timeout(500L)
                .build();
        purgeQueuesAction.execute(context);
        verify(connection).start();
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Map<String, TestActionBuilder<?>> endpointBuilders = TestActionBuilder.lookup();
        Assert.assertTrue(endpointBuilders.containsKey("purgeQueues"));

        Assert.assertTrue(TestActionBuilder.lookup("purgeQueues").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("purgeQueues").get().getClass(), PurgeJmsQueuesAction.Builder.class);
    }
}
