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
import org.testng.Assert;
import org.testng.annotations.Test;

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
    public void shouldLoadCamelActions() throws Exception {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/jms/yaml/purge-queues-test.yaml");

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
        Assert.assertEquals(result.getName(), "PurgeQueuesTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
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

        action = (PurgeJmsQueuesAction) result.getTestAction(actionIndex);
        Assert.assertEquals(action.getReceiveTimeout(), 125L);
        Assert.assertEquals(action.getSleepTime(), 250L);
        Assert.assertNotNull(action.getConnectionFactory());
        Assert.assertEquals(action.getQueues().size(), 0);
        Assert.assertEquals(action.getQueueNames().size(), 2);
        Assert.assertEquals(action.getQueueNames().get(0), "JMS.Queue.2");
        Assert.assertEquals(action.getQueueNames().get(1), "JMS.Queue.3");
    }


    @Test
    public void shouldLookupTestActionBuilder() {
        Assert.assertTrue(YamlTestActionBuilder.lookup().containsKey("purgeQueues"));
        Assert.assertTrue(YamlTestActionBuilder.lookup("purgeQueues").isPresent());
        Assert.assertEquals(YamlTestActionBuilder.lookup("purgeQueues").get().getClass(), PurgeQueues.class);
    }
}
