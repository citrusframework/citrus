/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.jms.actions.dsl;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.jms.UnitTestSupport;
import org.citrusframework.jms.actions.PurgeJmsQueuesAction;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.jms.actions.PurgeJmsQueuesAction.Builder.purgeQueues;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class PurgeJmsQueueTestActionBuilderTest extends UnitTestSupport {
    private final ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
    private final Connection connection = Mockito.mock(Connection.class);
    private final Session session = Mockito.mock(Session.class);
    private final MessageConsumer messageConsumer = Mockito.mock(MessageConsumer.class);
    private final Queue queue1 = Mockito.mock(Queue.class);
    private final Queue queue2 = Mockito.mock(Queue.class);
    private final Queue queue3 = Mockito.mock(Queue.class);
    private final Queue queue4 = Mockito.mock(Queue.class);

    @Test
    public void testPurgeJmsQueuesBuilderWithQueueNames() throws JMSException {
        reset(connectionFactory, connection, session, messageConsumer, queue1, queue2, queue3, queue4);
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createConsumer(any(Destination.class))).thenReturn(messageConsumer);

        when(session.createQueue("q1")).thenReturn(queue1);
        when(session.createQueue("q2")).thenReturn(queue2);
        when(session.createQueue("q3")).thenReturn(queue3);
        when(session.createQueue("q4")).thenReturn(queue4);

        when(messageConsumer.receive(200L)).thenReturn(null);

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(purgeQueues().connectionFactory(connectionFactory)
            .queueNames("q1", "q2", "q3")
            .queue("q4")
            .timeout(200L)
            .sleep(150L));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeJmsQueuesAction.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "purge-queue");

        PurgeJmsQueuesAction action = (PurgeJmsQueuesAction)test.getActions().get(0);
        Assert.assertEquals(action.getReceiveTimeout(), 200L);
        Assert.assertEquals(action.getSleepTime(), 150L);
        Assert.assertEquals(action.getConnectionFactory(), connectionFactory);
        Assert.assertEquals(action.getQueueNames().size(), 4);
        Assert.assertEquals(action.getQueueNames().toString(), "[q1, q2, q3, q4]");
        Assert.assertEquals(action.getQueues().size(), 0);

        verify(connection).start();
    }

    @Test
    public void testPurgeJmsQueuesBuilderWithQueues() throws JMSException {
        reset(connectionFactory, connection, session, messageConsumer, queue1, queue2, queue3, queue4);
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createConsumer(any(Destination.class))).thenReturn(messageConsumer);

        when(queue1.getQueueName()).thenReturn("q1");
        when(queue2.getQueueName()).thenReturn("q2");
        when(queue3.getQueueName()).thenReturn("q3");

        when(messageConsumer.receive(200L)).thenReturn(null);

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(purgeQueues().connectionFactory(connectionFactory)
            .queues(queue1, queue2)
            .queue(queue3)
            .timeout(200L)
            .sleep(150L));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeJmsQueuesAction.class);

        PurgeJmsQueuesAction action = (PurgeJmsQueuesAction)test.getActions().get(0);
        Assert.assertEquals(action.getReceiveTimeout(), 200L);
        Assert.assertEquals(action.getSleepTime(), 150L);
        Assert.assertEquals(action.getConnectionFactory(), connectionFactory);
        Assert.assertEquals(action.getQueueNames().size(), 0);
        Assert.assertEquals(action.getQueues().size(), 3);
        Assert.assertEquals(action.getQueues().toString(), "[" + queue1.toString() + ", " + queue2.toString() + ", " + queue3.toString() + "]");

        verify(connection).start();
    }

}
