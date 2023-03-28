/*
 * Copyright 2006-2012 the original author or authors.
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

package org.citrusframework.dsl.design;

import org.citrusframework.TestCase;
import org.citrusframework.dsl.UnitTestSupport;
import org.citrusframework.jms.actions.PurgeJmsQueuesAction;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 1.3
 */
public class PurgeJmsQueueTestDesignerTest extends UnitTestSupport {
    private ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);

    private Queue queue1 = Mockito.mock(Queue.class);
    private Queue queue2 = Mockito.mock(Queue.class);
    private Queue queue3 = Mockito.mock(Queue.class);

    @Test
    public void testPurgeJmsQueuesBuilderWithQueueNames() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                purgeQueues()
                    .connectionFactory(connectionFactory)
                    .queueNames("q1", "q2", "q3")
                    .queue("q4")
                    .timeout(2000L)
                    .sleep(1000L);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeJmsQueuesAction.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "purge-queue");

        PurgeJmsQueuesAction action = (PurgeJmsQueuesAction)test.getActions().get(0);
        Assert.assertEquals(action.getReceiveTimeout(), 2000L);
        Assert.assertEquals(action.getSleepTime(), 1000L);
        Assert.assertEquals(action.getConnectionFactory(), connectionFactory);
        Assert.assertEquals(action.getQueueNames().size(), 4);
        Assert.assertEquals(action.getQueueNames().toString(), "[q1, q2, q3, q4]");
        Assert.assertEquals(action.getQueues().size(), 0);
    }

    @Test
    public void testPurgeJmsQueuesBuilderWithQueues() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                purgeQueues()
                    .connectionFactory(connectionFactory)
                    .queues(queue1, queue2)
                    .queue(queue3)
                    .timeout(2000L)
                    .sleep(1000L);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeJmsQueuesAction.class);

        PurgeJmsQueuesAction action = (PurgeJmsQueuesAction)test.getActions().get(0);
        Assert.assertEquals(action.getReceiveTimeout(), 2000L);
        Assert.assertEquals(action.getSleepTime(), 1000L);
        Assert.assertEquals(action.getConnectionFactory(), connectionFactory);
        Assert.assertEquals(action.getQueueNames().size(), 0);
        Assert.assertEquals(action.getQueues().size(), 3);
        Assert.assertEquals(action.getQueues().toString(), "[" + queue1.toString() + ", " + queue2.toString() + ", " + queue3.toString() + "]");
    }

}
