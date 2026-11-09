/*
 * Copyright the original author or authors.
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

package org.citrusframework.jms.yaml;

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
import org.citrusframework.yaml.YamlTestLoader;
import org.citrusframework.yaml.actions.YamlTestActionBuilder;
import org.mockito.Mock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class PurgeQueuesTest extends AbstractYamlActionTest {

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
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/jms/yaml/purge-queues.citrus.it.yaml");

        context.getReferenceResolver().bind("connectionFactory", connectionFactory);
        context.getReferenceResolver().bind("myConnectionFactory", connectionFactory);

        reset(connectionFactory, connection, session, messageConsumer);

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);
        when(session.createQueue(anyString())).thenReturn(queue);
        when(session.createConsumer(queue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(100L)).thenReturn(new TextMessageImpl("Hello Citrus!", new HashMap<>())).thenReturn(null);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("PurgeQueuesTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(PurgeJmsQueuesAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("purge-queue", result.getTestAction(0).getName());

        int actionIndex = 0;

        PurgeJmsQueuesAction action = (PurgeJmsQueuesAction) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(action.getConnectionFactory());
        Assertions.assertEquals(100L, action.getReceiveTimeout());
        Assertions.assertEquals(350L, action.getSleepTime());
        Assertions.assertEquals(0, action.getQueues().size());
        Assertions.assertEquals(1, action.getQueueNames().size());
        Assertions.assertEquals("JMS.Queue.1", action.getQueueNames().get(0));

        action = (PurgeJmsQueuesAction) result.getTestAction(actionIndex);
        Assertions.assertEquals(125L, action.getReceiveTimeout());
        Assertions.assertEquals(250L, action.getSleepTime());
        Assertions.assertNotNull(action.getConnectionFactory());
        Assertions.assertEquals(0, action.getQueues().size());
        Assertions.assertEquals(2, action.getQueueNames().size());
        Assertions.assertEquals("JMS.Queue.2", action.getQueueNames().get(0));
        Assertions.assertEquals("JMS.Queue.3", action.getQueueNames().get(1));
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Assertions.assertTrue(YamlTestActionBuilder.lookup().containsKey("purgeQueues"));
        Assertions.assertTrue(YamlTestActionBuilder.lookup("purgeQueues").isPresent());
        Assertions.assertEquals(PurgeQueues.class, YamlTestActionBuilder.lookup("purgeQueues").get().getClass());
    }
}
