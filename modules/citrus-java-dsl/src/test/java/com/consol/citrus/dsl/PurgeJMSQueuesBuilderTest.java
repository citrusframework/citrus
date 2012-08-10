package com.consol.citrus.dsl;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.PurgeJmsQueuesAction;

public class PurgeJMSQueuesBuilderTest {
	ConnectionFactory connectionFactory = EasyMock.createMock(ConnectionFactory.class);
	Queue queue1 = EasyMock.createMock(Queue.class);
	Queue queue2 = EasyMock.createMock(Queue.class);
	
	@Test
	public void testPurgeJMSQueuesBuilder(){
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
            	purgeJMSQueues(connectionFactory)
            	.queueNames("q1", "q2", "q3")
            	.queues(queue1, queue2)
            	.receiveTimeout(2000)
            	.sleep(1000);
            }
          };
          
          builder.configure();
          
          Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
          Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), PurgeJmsQueuesAction.class);
          
          PurgeJmsQueuesAction action = (PurgeJmsQueuesAction)builder.getTestCase().getActions().get(0);
          Assert.assertEquals(action.getReceiveTimeout(), 2000);
          Assert.assertEquals(action.getSleepTime(), 1000);
          Assert.assertEquals(action.getConnectionFactory(), connectionFactory);
          Assert.assertEquals(action.getQueueNames().size(), 3);
          Assert.assertEquals(action.getQueueNames().toString(), "[q1, q2, q3]");
          Assert.assertEquals(action.getQueues().size(), 2);
          Assert.assertEquals(action.getQueues().toString(), "[" + queue1.toString() + ", " + queue2.toString() + "]");
	}
}
