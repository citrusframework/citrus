/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.jms.xml;

import java.util.HashMap;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.jms.actions.PurgeJmsQueuesAction;
import org.citrusframework.jms.endpoint.TextMessageImpl;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.xml.actions.XmlTestActionBuilder;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class PurgeQueuesTest extends AbstractXmlActionTest {

    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private Connection connection;
    @Mock
    private Session session;
    @Mock
    private Destination destination;
    @Mock
    private Queue queue;
    @Mock
    private MessageConsumer messageConsumer;

    @Test
    public void shouldLoadJmsActions() throws Exception {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/jms/xml/purge-queues-test.xml");

        context.getReferenceResolver().bind("connectionFactory", connectionFactory);
        context.getReferenceResolver().bind("myConnectionFactory", connectionFactory);
        context.getReferenceResolver().bind("myQueue", queue);

        reset(connectionFactory, connection, session, messageConsumer);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);
        when(session.createQueue(anyString())).thenReturn(queue);
        when(session.createConsumer(queue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(100L)).thenReturn(new TextMessageImpl("Hello Citrus!", new HashMap<>())).thenReturn(null);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "PurgeQueuesTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 3L);
        Assert.assertEquals(result.getTestAction(0).getClass(), PurgeJmsQueuesAction.class);
        Assert.assertEquals(result.getTestAction(0).getName(), "purge-queue");

        int actionIndex = 0;

        PurgeJmsQueuesAction action = (PurgeJmsQueuesAction) result.getTestAction(actionIndex++);
        Assert.assertNotNull(action.getConnectionFactory());
        Assert.assertEquals(action.getReceiveTimeout(), 100L);
        Assert.assertEquals(action.getSleepTime(), 350L);
        Assert.assertEquals(action.getQueues().size(), 0);
        Assert.assertEquals(action.getQueueNames().size(), 1);
        Assert.assertEquals(action.getQueueNames().get(0), "JMS.Queue.1");

        action = (PurgeJmsQueuesAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getReceiveTimeout(), 125L);
        Assert.assertEquals(action.getSleepTime(), 250L);
        Assert.assertNotNull(action.getConnectionFactory());
        Assert.assertEquals(action.getQueues().size(), 0);
        Assert.assertEquals(action.getQueueNames().size(), 2);
        Assert.assertEquals(action.getQueueNames().get(0), "JMS.Queue.2");
        Assert.assertEquals(action.getQueueNames().get(1), "JMS.Queue.3");

        action = (PurgeJmsQueuesAction) result.getTestAction(actionIndex);
        Assert.assertNotNull(action.getConnectionFactory());
        Assert.assertEquals(action.getQueues().size(), 1);
        Assert.assertEquals(action.getQueues().get(0), queue);
        Assert.assertEquals(action.getQueueNames().size(), 0);
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Assert.assertTrue(XmlTestActionBuilder.lookup("purge-jms-queues").isPresent());
        Assert.assertEquals(XmlTestActionBuilder.lookup("purge-jms-queues").get().getClass(), PurgeQueues.class);
        Assert.assertTrue(XmlTestActionBuilder.lookup("purgeQueues").isPresent());
        Assert.assertEquals(XmlTestActionBuilder.lookup("purgeQueues").get().getClass(), PurgeQueues.class);
    }
}
