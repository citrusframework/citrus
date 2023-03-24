/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.jms.endpoint;

import org.citrusframework.TestActor;
import org.citrusframework.jms.message.JmsMessageConverter;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.Topic;
import jakarta.jms.TopicConnection;
import jakarta.jms.TopicConnectionFactory;
import jakarta.jms.TopicSession;
import jakarta.jms.TopicSubscriber;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class JmsTopicSubscriberTest extends AbstractTestNGUnitTest {

    @Mock
    private JmsTemplate jmsTemplate;
    @Mock
    private Destination queue;
    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private TopicConnectionFactory topicConnectionFactory;
    @Mock
    private TopicConnection topicConnection;
    @Mock
    private TopicSession topicSession;
    @Mock
    private Topic topic;
    @Mock
    private TopicSubscriber topicSubscriber;
    @Mock
    private ConnectionFactory jmsConnectionFactory;
    @Mock
    private JmsMessageConverter messageConverter;
    @Mock
    private TestActor testActor;
    @Mock
    private ApplicationContext applicationContext;

    @BeforeClass
    public void setup() throws JMSException {
        MockitoAnnotations.openMocks(this);

        when(applicationContext.getBean("jmsTemplate", JmsTemplate.class)).thenReturn(jmsTemplate);

        when(jmsTemplate.getConnectionFactory()).thenReturn(topicConnectionFactory);
        when(topicConnectionFactory.createTopicConnection()).thenReturn(topicConnection);
        when(topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(topicSession);
        when(jmsTemplate.getDefaultDestinationName()).thenReturn("JMS.Topic.Test");
        when(topicSession.createTopic("JMS.Topic.Test")).thenReturn(topic);

        when(applicationContext.getBean("jmsQueue", Destination.class)).thenReturn(queue);
        when(applicationContext.getBean("jmsTopic", Destination.class)).thenReturn(topic);
        when(applicationContext.getBean("messageConverter", JmsMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("connectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        when(applicationContext.getBean("jmsConnectionFactory", ConnectionFactory.class)).thenReturn(jmsConnectionFactory);
        when(applicationContext.getBean("jmsTopicConnectionFactory", ConnectionFactory.class)).thenReturn(topicConnectionFactory);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void clearSubscriberMocking() throws JMSException{
        topicSubscriber = mock(TopicSubscriber.class);
        when(topicSession.createSubscriber(topic)).thenReturn(topicSubscriber);
        when(topicSession.createDurableSubscriber(topic, "jmsTopicEndpoint:subscriber")).thenReturn(topicSubscriber);
    }

    @Test
    public void testSubscriberWithConnectionFactory() throws JMSException {
        JmsEndpointConfiguration endpointConfiguration = new JmsEndpointConfiguration();
        endpointConfiguration.setPubSubDomain(true);
        endpointConfiguration.setAutoStart(true);
        endpointConfiguration.setConnectionFactory(topicConnectionFactory);
        endpointConfiguration.setDestinationName("JMS.Topic.Test");

        when(topicSubscriber.receive()).thenReturn(new TextMessageImpl("Foo1", Collections.emptyMap()))
                                       .thenReturn(new TextMessageImpl("Foo2", Collections.emptyMap()))
                                       .thenReturn(new TextMessageImpl("Foo3", Collections.emptyMap()))
                                       .thenReturn(null);

        JmsEndpoint jmsEndpoint = new JmsEndpoint(endpointConfiguration);

        JmsConsumer consumer = (JmsConsumer) jmsEndpoint.createConsumer();
        Assert.assertTrue(consumer instanceof JmsTopicSubscriber);

        JmsTopicSubscriber jmsTopicSubscriber = (JmsTopicSubscriber) consumer;
        Assert.assertTrue(jmsTopicSubscriber.isRunning());

        Assert.assertEquals(consumer.receive(context, endpointConfiguration.getTimeout()).getPayload(String.class), "Foo1");
        Assert.assertEquals(consumer.receive(context, endpointConfiguration.getTimeout()).getPayload(String.class), "Foo2");
        Assert.assertEquals(consumer.receive(context, endpointConfiguration.getTimeout()).getPayload(String.class), "Foo3");
    }

    @Test
    public void testSubscriberWithTopicDestination() throws JMSException {
        JmsEndpointConfiguration endpointConfiguration = new JmsEndpointConfiguration();
        endpointConfiguration.setPubSubDomain(true);
        endpointConfiguration.setAutoStart(true);
        endpointConfiguration.setConnectionFactory(topicConnectionFactory);
        endpointConfiguration.setDestination(topic);

        when(topicSubscriber.receive()).thenReturn(new TextMessageImpl("Foo1", Collections.emptyMap()))
                                       .thenReturn(null);

        JmsEndpoint jmsEndpoint = new JmsEndpoint(endpointConfiguration);

        JmsTopicSubscriber jmsTopicSubscriber = (JmsTopicSubscriber) jmsEndpoint.createConsumer();

        Assert.assertEquals(jmsTopicSubscriber.receive(context, endpointConfiguration.getTimeout()).getPayload(String.class), "Foo1");
    }

    @Test
    public void testSubscriberWithJmsTemplate() throws JMSException {
        JmsEndpointConfiguration endpointConfiguration = new JmsEndpointConfiguration();
        endpointConfiguration.setPubSubDomain(true);
        endpointConfiguration.setAutoStart(true);
        endpointConfiguration.setJmsTemplate(jmsTemplate);

        when(topicSubscriber.receive()).thenReturn(new TextMessageImpl("Foo1", Collections.emptyMap()))
                                       .thenReturn(null);

        JmsEndpoint jmsEndpoint = new JmsEndpoint(endpointConfiguration);

        JmsTopicSubscriber jmsTopicSubscriber = (JmsTopicSubscriber) jmsEndpoint.createConsumer();

        Assert.assertEquals(jmsTopicSubscriber.receive(context, endpointConfiguration.getTimeout()).getPayload(String.class), "Foo1");
    }

    @Test
    public void testSubscriberStop() throws JMSException, InterruptedException {
        JmsEndpointConfiguration endpointConfiguration = new JmsEndpointConfiguration();
        endpointConfiguration.setPubSubDomain(true);
        endpointConfiguration.setAutoStart(true);
        endpointConfiguration.setConnectionFactory(topicConnectionFactory);
        endpointConfiguration.setDestinationName("JMS.Topic.Test");

        when(topicSubscriber.receive()).thenReturn(new TextMessageImpl("Foo1", Collections.emptyMap()))
                .thenReturn(new TextMessageImpl("Foo2", Collections.emptyMap()))
                .thenReturn(new TextMessageImpl("Foo3", Collections.emptyMap()))
                .thenReturn(null);

        JmsEndpoint jmsEndpoint = new JmsEndpoint(endpointConfiguration);

        JmsTopicSubscriber jmsTopicSubscriber = (JmsTopicSubscriber) jmsEndpoint.createConsumer();

        Assert.assertEquals(jmsTopicSubscriber.receive(context, endpointConfiguration.getTimeout()).getPayload(String.class), "Foo1");

        Thread.sleep(1000L);

        jmsTopicSubscriber.stop();

        Assert.assertEquals(jmsTopicSubscriber.receive(context, endpointConfiguration.getTimeout()).getPayload(String.class), "Foo2");
        Assert.assertEquals(jmsTopicSubscriber.receive(context, endpointConfiguration.getTimeout()).getPayload(String.class), "Foo3");
    }

    @Test
    public void testDurableSubscriber() throws JMSException {
        JmsEndpointConfiguration endpointConfiguration = new JmsEndpointConfiguration();
        endpointConfiguration.setPubSubDomain(true);
        endpointConfiguration.setAutoStart(true);
        endpointConfiguration.setDurableSubscription(true);
        endpointConfiguration.setConnectionFactory(topicConnectionFactory);
        endpointConfiguration.setDestinationName("JMS.Topic.Test");

        when(topicSubscriber.receive()).thenReturn(new TextMessageImpl("Foo1", Collections.emptyMap()))
                .thenReturn(new TextMessageImpl("Foo2", Collections.emptyMap()))
                .thenReturn(new TextMessageImpl("Foo3", Collections.emptyMap()))
                .thenReturn(null);

        JmsEndpoint jmsEndpoint = new JmsEndpoint(endpointConfiguration);
        jmsEndpoint.setName("jmsTopicEndpoint");

        JmsConsumer consumer = (JmsConsumer) jmsEndpoint.createConsumer();
        Assert.assertTrue(consumer instanceof JmsTopicSubscriber);

        JmsTopicSubscriber jmsTopicSubscriber = (JmsTopicSubscriber) consumer;
        Assert.assertTrue(jmsTopicSubscriber.isRunning());

        Assert.assertEquals(consumer.receive(context, endpointConfiguration.getTimeout()).getPayload(String.class), "Foo1");
        Assert.assertEquals(consumer.receive(context, endpointConfiguration.getTimeout()).getPayload(String.class), "Foo2");
        Assert.assertEquals(consumer.receive(context, endpointConfiguration.getTimeout()).getPayload(String.class), "Foo3");
    }
}
