/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.actions;

import static org.easymock.EasyMock.*;

import java.util.*;

import javax.jms.*;
import javax.jms.Queue;

import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class PurgeJmsQueuesActionTest extends AbstractBaseTest {
	
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
        
        Map<String, String> requestHeaders = new HashMap<String, String>();
        TextMessage jmsRequest = new TextMessageImpl("<TestRequest>Hello World!</TestRequest>", requestHeaders);
        
        reset(connectionFactory, connection, session, messageConsumer);
        
        expect(connectionFactory.createConnection()).andReturn(connection).once();
        connection.start();
        expectLastCall().once();
        
        expect(connection.createSession(anyBoolean(), anyInt())).andReturn(session).once();
        
        expect(session.createQueue("myQueue")).andReturn(queue).once();
        
        expect(session.createConsumer(queue)).andReturn(messageConsumer).times(3);
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
	
	private static class TextMessageImpl implements TextMessage {
        private String payload = "";
        
        private Map<String, String> headers = new HashMap<String, String>();
        
        public TextMessageImpl(String payload, Map<String, String> headers) {
            this.payload = payload;
            this.headers = headers;
        }
        
        public void setStringProperty(String name, String value) throws JMSException {headers.put(name, value);}
        public void setShortProperty(String name, short value) throws JMSException {}
        public void setObjectProperty(String name, Object value) throws JMSException {}
        public void setLongProperty(String name, long value) throws JMSException {}
        public void setJMSType(String type) throws JMSException {}
        public void setJMSTimestamp(long timestamp) throws JMSException {}
        public void setJMSReplyTo(Destination replyTo) throws JMSException {}
        public void setJMSRedelivered(boolean redelivered) throws JMSException {}
        public void setJMSPriority(int priority) throws JMSException {}
        public void setJMSMessageID(String id) throws JMSException {}
        public void setJMSExpiration(long expiration) throws JMSException {}
        public void setJMSDestination(Destination destination) throws JMSException {}
        public void setJMSDeliveryMode(int deliveryMode) throws JMSException {}
        public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {}
        public void setJMSCorrelationID(String correlationID) throws JMSException {}
        public void setIntProperty(String name, int value) throws JMSException {}
        public void setFloatProperty(String name, float value) throws JMSException {}
        public void setDoubleProperty(String name, double value) throws JMSException {}
        public void setByteProperty(String name, byte value) throws JMSException {}
        public void setBooleanProperty(String name, boolean value) throws JMSException {}
        public boolean propertyExists(String name) throws JMSException {return false;}
        public String getStringProperty(String name) throws JMSException {return headers.get(name);}
        public short getShortProperty(String name) throws JMSException {return 0;}
        @SuppressWarnings("unchecked")
        public Enumeration getPropertyNames() throws JMSException {return null;}
        public Object getObjectProperty(String name) throws JMSException {return null;}
        public long getLongProperty(String name) throws JMSException {return 0;}
        public String getJMSType() throws JMSException {return null;}
        public long getJMSTimestamp() throws JMSException {return 0;}
        public Destination getJMSReplyTo() throws JMSException {return null;}
        public boolean getJMSRedelivered() throws JMSException {return false;}
        public int getJMSPriority() throws JMSException {return 0;}
        public String getJMSMessageID() throws JMSException {return "123456789";}
        public long getJMSExpiration() throws JMSException {return 0;}
        public Destination getJMSDestination() throws JMSException {return null;}
        public int getJMSDeliveryMode() throws JMSException {return 0;}
        public byte[] getJMSCorrelationIDAsBytes() throws JMSException {return null;}
        public String getJMSCorrelationID() throws JMSException {return null;}
        public int getIntProperty(String name) throws JMSException {return 0;}
        public float getFloatProperty(String name) throws JMSException {return 0;}
        public double getDoubleProperty(String name) throws JMSException {return 0;}
        public byte getByteProperty(String name) throws JMSException {return 0;}
        public boolean getBooleanProperty(String name) throws JMSException {return false;}
        public void clearProperties() throws JMSException {}
        public void clearBody() throws JMSException {}
        public void acknowledge() throws JMSException {}
        public void setText(String string) throws JMSException {this.payload = string;}
        public String getText() throws JMSException {return payload;}
    }
}
