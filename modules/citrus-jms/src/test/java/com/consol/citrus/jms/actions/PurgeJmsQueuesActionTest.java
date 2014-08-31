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
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class PurgeJmsQueuesActionTest extends AbstractTestNGUnitTest {
	
    private ConnectionFactory connectionFactory = EasyMock.createMock(ConnectionFactory.class);
    private Connection connection = EasyMock.createMock(Connection.class);
    private Session session = EasyMock.createMock(Session.class);
    private MessageConsumer messageConsumer = EasyMock.createMock(MessageConsumer.class);
    
    private Queue queue = EasyMock.createMock(Queue.class);
    
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
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        connection.start();
        expectLastCall().once();
        
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        
        expect(session.createQueue("myQueue")).andReturn(queue).once();
        
        expect(session.createConsumer(queue)).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(100L)).andReturn(jmsRequest).times(2).andReturn(null);
        
        replay(connectionFactory, connection, session, messageConsumer);
        
        purgeQueuesAction.execute(context);
        
        verify(connectionFactory, connection, session, messageConsumer);
    }
    
	@Test
	public void testPurgeWithQueueNamesNoMessages() throws JMSException {
		PurgeJmsQueuesAction purgeQueuesAction = new PurgeJmsQueuesAction();
		purgeQueuesAction.setConnectionFactory(connectionFactory);
		
		List<String> queueNames = new ArrayList<String>();
		queueNames.add("myQueue");
		purgeQueuesAction.setQueueNames(queueNames);
		
		reset(connectionFactory, connection, session, messageConsumer);
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        connection.start();
        expectLastCall().once();
        
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        
        expect(session.createQueue("myQueue")).andReturn(queue).once();
        
        expect(session.createConsumer(queue)).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(100L)).andReturn(null).once();
        
        replay(connectionFactory, connection, session, messageConsumer);
		
		purgeQueuesAction.execute(context);
		
		verify(connectionFactory, connection, session, messageConsumer);
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
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        connection.start();
        expectLastCall().once();
        
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        
        expect(session.createQueue("myQueue")).andReturn(queue).once();
        expect(session.createQueue("anotherQueue")).andReturn(queue).once();
        expect(session.createQueue("someQueue")).andReturn(queue).once();
        
        expect(session.createConsumer(queue)).andReturn(messageConsumer).times(3);
        expect(messageConsumer.receive(100L)).andReturn(null).times(3);
        
        replay(connectionFactory, connection, session, messageConsumer);
        
        purgeQueuesAction.execute(context);
        
        verify(connectionFactory, connection, session, messageConsumer);
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
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        connection.start();
        expectLastCall().once();
        
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        
        expect(queue.getQueueName()).andReturn("myQueue").times(3);
        expect(session.createConsumer(queue)).andReturn(messageConsumer).times(3);
        expect(messageConsumer.receive(100L)).andReturn(null).times(3);
        
        replay(connectionFactory, connection, session, messageConsumer, queue);
        
        purgeQueuesAction.execute(context);
        
        verify(connectionFactory, connection, session, messageConsumer, queue);
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
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        connection.start();
        expectLastCall().once();
        
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        
        expect(session.createQueue("myQueue")).andReturn(queue).once();
        
        expect(session.createConsumer(queue)).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(500L)).andReturn(null).once();
        
        replay(connectionFactory, connection, session, messageConsumer);
        
        purgeQueuesAction.execute(context);
        
        verify(connectionFactory, connection, session, messageConsumer);
    }
}
