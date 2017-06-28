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

package com.consol.citrus.jms.actions;

import com.consol.citrus.jms.endpoint.TextMessageImpl;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class PurgeJmsQueuesActionTest extends AbstractTestNGUnitTest {
	
    private ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
    private Connection connection = Mockito.mock(Connection.class);
    private Session session = Mockito.mock(Session.class);
    private MessageConsumer messageConsumer = Mockito.mock(MessageConsumer.class);
    
    private Queue queue = Mockito.mock(Queue.class);
    
    @Test
    public void testPurgeWithQueueNamesConsumeMessages() throws JMSException {
        PurgeJmsQueuesAction purgeQueuesAction = new PurgeJmsQueuesAction();
        purgeQueuesAction.setConnectionFactory(connectionFactory);
        
        List<String> queueNames = new ArrayList<String>();
        queueNames.add("myQueue");
        purgeQueuesAction.setQueueNames(queueNames);
        
        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        TextMessage jmsRequest = new TextMessageImpl("<TestRequest>Hello World!</TestRequest>", requestHeaders);
        
        reset(connectionFactory, connection, session, messageConsumer);
        
        when(connectionFactory.createConnection()).thenReturn(connection);

        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createQueue("myQueue")).thenReturn(queue);

        when(session.createConsumer(queue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(100L)).thenReturn(jmsRequest).thenReturn(null);

        purgeQueuesAction.execute(context);
        verify(connection).start();
    }
    
	@Test
	public void testPurgeWithQueueNamesNoMessages() throws JMSException {
		PurgeJmsQueuesAction purgeQueuesAction = new PurgeJmsQueuesAction();
		purgeQueuesAction.setConnectionFactory(connectionFactory);
		
		List<String> queueNames = new ArrayList<String>();
		queueNames.add("myQueue");
		purgeQueuesAction.setQueueNames(queueNames);
		
		reset(connectionFactory, connection, session, messageConsumer);
        
        when(connectionFactory.createConnection()).thenReturn(connection);

        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createQueue("myQueue")).thenReturn(queue);

        when(session.createConsumer(queue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(100L)).thenReturn(null);

		purgeQueuesAction.execute(context);
        verify(connection).start();
	}
	
	@Test
    public void testPurgeQueueNameList() throws JMSException {
        PurgeJmsQueuesAction purgeQueuesAction = new PurgeJmsQueuesAction();
        purgeQueuesAction.setConnectionFactory(connectionFactory);
        
        List<String> queueNames = new ArrayList<String>();
        queueNames.add("myQueue");
        queueNames.add("anotherQueue");
        queueNames.add("someQueue");
        purgeQueuesAction.setQueueNames(queueNames);
        
        reset(connectionFactory, connection, session, messageConsumer);
        
        when(connectionFactory.createConnection()).thenReturn(connection);

        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createQueue("myQueue")).thenReturn(queue);
        when(session.createQueue("anotherQueue")).thenReturn(queue);
        when(session.createQueue("someQueue")).thenReturn(queue);

        when(session.createConsumer(queue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(100L)).thenReturn(null);

        purgeQueuesAction.execute(context);
        verify(connection).start();
    }
	
	@Test
    public void testPurgeQueueList() throws JMSException {
        PurgeJmsQueuesAction purgeQueuesAction = new PurgeJmsQueuesAction();
        purgeQueuesAction.setConnectionFactory(connectionFactory);
        
        List<Queue> queueNames = new ArrayList<Queue>();
        queueNames.add(queue);
        queueNames.add(queue);
        queueNames.add(queue);
        purgeQueuesAction.setQueues(queueNames);
        
        reset(connectionFactory, connection, session, messageConsumer, queue);
        
        when(connectionFactory.createConnection()).thenReturn(connection);

        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(queue.getQueueName()).thenReturn("myQueue");
        when(session.createConsumer(queue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(100L)).thenReturn(null);

        purgeQueuesAction.execute(context);
        verify(connection).start();
    }

    @Test
    public void testPurgeQueueNameVariable() throws JMSException {
        PurgeJmsQueuesAction purgeQueuesAction = new PurgeJmsQueuesAction();
        purgeQueuesAction.setConnectionFactory(connectionFactory);

        context.getVariables().put("variableQueueName", "queueName");
        context.getVariables().put("secondQueueName", "secondQueue");

        List<String> queueNames = new ArrayList<String>();
        queueNames.add("${variableQueueName}");
        queueNames.add("${secondQueueName}");
        purgeQueuesAction.setQueueNames(queueNames);

        reset(connectionFactory, connection, session, messageConsumer);

        when(connectionFactory.createConnection()).thenReturn(connection);

        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createQueue("queueName")).thenReturn(queue);
        when(session.createQueue("secondQueue")).thenReturn(queue);

        when(session.createConsumer(queue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(100L)).thenReturn(null);

        purgeQueuesAction.execute(context);
        verify(connection).start();
    }

    @Test
    public void testPurgeWithVariableQueueNamesConsumeMessages() throws JMSException {
        PurgeJmsQueuesAction purgeQueuesAction = new PurgeJmsQueuesAction();
        purgeQueuesAction.setConnectionFactory(connectionFactory);

        context.getVariables().put("variableQueueName", "queueName");

        List<String> queueNames = new ArrayList<String>();
        queueNames.add("${variableQueueName}");
        purgeQueuesAction.setQueueNames(queueNames);

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        TextMessage jmsRequest = new TextMessageImpl("<TestRequest>Hello World!</TestRequest>", requestHeaders);

        reset(connectionFactory, connection, session, messageConsumer);

        when(connectionFactory.createConnection()).thenReturn(connection);

        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createQueue("queueName")).thenReturn(queue);

        when(session.createConsumer(queue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(100L)).thenReturn(jmsRequest).thenReturn(null);

        purgeQueuesAction.execute(context);
        verify(connection).start();
    }

    @Test
    public void testPurgeWithCustomTimeout() throws JMSException {
        PurgeJmsQueuesAction purgeQueuesAction = new PurgeJmsQueuesAction();
        purgeQueuesAction.setConnectionFactory(connectionFactory);
        
        purgeQueuesAction.setReceiveTimeout(500L);
        
        List<String> queueNames = new ArrayList<String>();
        queueNames.add("myQueue");
        purgeQueuesAction.setQueueNames(queueNames);
        
        reset(connectionFactory, connection, session, messageConsumer);
        
        when(connectionFactory.createConnection()).thenReturn(connection);

        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

        when(session.createQueue("myQueue")).thenReturn(queue);

        when(session.createConsumer(queue)).thenReturn(messageConsumer);
        when(messageConsumer.receive(500L)).thenReturn(null);

        purgeQueuesAction.execute(context);
        verify(connection).start();
    }
}
