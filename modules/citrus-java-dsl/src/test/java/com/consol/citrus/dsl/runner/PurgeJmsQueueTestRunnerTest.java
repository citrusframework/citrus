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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.PurgeJmsQueuesBuilder;
import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.jms.*;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class PurgeJmsQueueTestRunnerTest extends AbstractTestNGUnitTest {
    private ConnectionFactory connectionFactory = EasyMock.createMock(ConnectionFactory.class);
    private Connection connection = EasyMock.createMock(Connection.class);
    private Session session = EasyMock.createMock(Session.class);
    private MessageConsumer messageConsumer = EasyMock.createMock(MessageConsumer.class);
    private Queue queue1 = EasyMock.createMock(Queue.class);
    private Queue queue2 = EasyMock.createMock(Queue.class);
    private Queue queue3 = EasyMock.createMock(Queue.class);
    private Queue queue4 = EasyMock.createMock(Queue.class);

    @Test
    public void testPurgeJmsQueuesBuilderWithQueueNames() throws JMSException {
        reset(connectionFactory, connection, session, messageConsumer, queue1, queue2, queue3, queue4);
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        connection.start();
        expectLastCall().once();
        expect(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).andReturn(session);
        expect(session.createConsumer(anyObject(Destination.class))).andReturn(messageConsumer).atLeastOnce();

        expect(session.createQueue("q1")).andReturn(queue1).atLeastOnce();
        expect(session.createQueue("q2")).andReturn(queue2).atLeastOnce();
        expect(session.createQueue("q3")).andReturn(queue3).atLeastOnce();
        expect(session.createQueue("q4")).andReturn(queue4).atLeastOnce();

        expect(messageConsumer.receive(200L)).andReturn(null).atLeastOnce();

        replay(connectionFactory, connection, session, messageConsumer, queue1, queue2, queue3, queue4);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                purgeQueues(new BuilderSupport<PurgeJmsQueuesBuilder>() {
                    @Override
                    public void configure(PurgeJmsQueuesBuilder builder) {
                        builder.connectionFactory(connectionFactory)
                                .queueNames("q1", "q2", "q3")
                                .queue("q4")
                                .timeout(200L)
                                .sleep(150L);
                    }
                });
            }
        };

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

        verify(connectionFactory, connection, session, messageConsumer, queue1, queue2, queue3, queue4);
    }
    
    @Test
    public void testPurgeJmsQueuesBuilderWithQueues() throws JMSException {
        reset(connectionFactory, connection, session, messageConsumer, queue1, queue2, queue3, queue4);
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        connection.start();
        expectLastCall().once();
        expect(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).andReturn(session);
        expect(session.createConsumer(anyObject(Destination.class))).andReturn(messageConsumer).atLeastOnce();

        expect(queue1.getQueueName()).andReturn("q1");
        expect(queue2.getQueueName()).andReturn("q2");
        expect(queue3.getQueueName()).andReturn("q3");

        expect(messageConsumer.receive(200L)).andReturn(null).atLeastOnce();

        replay(connectionFactory, connection, session, messageConsumer, queue1, queue2, queue3, queue4);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                purgeQueues(new BuilderSupport<PurgeJmsQueuesBuilder>() {
                    @Override
                    public void configure(PurgeJmsQueuesBuilder builder) {
                        builder.connectionFactory(connectionFactory)
                                .queues(queue1, queue2)
                                .queue(queue3)
                                .timeout(200L)
                                .sleep(150L);
                    }
                });
            }
        };

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

        verify(connectionFactory, connection, session, messageConsumer, queue1, queue2, queue3, queue4);
    }
    
}
