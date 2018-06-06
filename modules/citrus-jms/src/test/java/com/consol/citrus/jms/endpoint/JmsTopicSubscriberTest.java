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

package com.consol.citrus.jms.endpoint;

import com.consol.citrus.TestActor;
import com.consol.citrus.jms.message.JmsMessageConverter;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.*;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.testng.Assert;
import org.testng.annotations.*;

import javax.jms.*;

import java.util.Collections;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class JmsTopicSubscriberTest extends AbstractTestNGUnitTest {

    @Mock
    private JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
    @Mock
    private Destination queue = Mockito.mock(Destination.class);
    @Mock
    private ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
    @Mock
    private TopicConnectionFactory topicConnectionFactory = Mockito.mock(TopicConnectionFactory.class);
    @Mock
    private TopicConnection topicConnection = Mockito.mock(TopicConnection.class);
    @Mock
    private TopicSession topicSession = Mockito.mock(TopicSession.class);
    @Mock
    private Topic topic = Mockito.mock(Topic.class);
    @Mock
    private TopicSubscriber topicSubscriber = Mockito.mock(TopicSubscriber.class);
    @Mock
    private ConnectionFactory jmsConnectionFactory = Mockito.mock(ConnectionFactory.class);
    @Mock
    private JmsMessageConverter messageConverter = Mockito.mock(JmsMessageConverter.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() throws JMSException {
        MockitoAnnotations.initMocks(this);

        when(applicationContext.getBean("jmsTemplate", JmsTemplate.class)).thenReturn(jmsTemplate);

        when(jmsTemplate.getConnectionFactory()).thenReturn(topicConnectionFactory);
        when(topicConnectionFactory.createTopicConnection()).thenReturn(topicConnection);
        when(topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(topicSession);
        when(jmsTemplate.getDefaultDestinationName()).thenReturn("JMS.Topic.Test");
        when(topicSession.createTopic("JMS.Topic.Test")).thenReturn(topic);
        when(topicSession.createSubscriber(topic)).thenReturn(topicSubscriber);
        when(topicSession.createDurableSubscriber(topic, "jmsTopicEndpoint:subscriber")).thenReturn(topicSubscriber);

        when(applicationContext.getBean("jmsQueue", Destination.class)).thenReturn(queue);
        when(applicationContext.getBean("jmsTopic", Destination.class)).thenReturn(topic);
        when(applicationContext.getBean("messageConverter", JmsMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("connectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        when(applicationContext.getBean("jmsConnectionFactory", ConnectionFactory.class)).thenReturn(jmsConnectionFactory);
        when(applicationContext.getBean("jmsTopicConnectionFactory", ConnectionFactory.class)).thenReturn(topicConnectionFactory);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void clearSubscriberMocking() {
        reset(topicSubscriber);
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
        Assert.assertEquals(jmsTopicSubscriber.isRunning(), true);

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
        Assert.assertEquals(jmsTopicSubscriber.isRunning(), true);

        Assert.assertEquals(consumer.receive(context, endpointConfiguration.getTimeout()).getPayload(String.class), "Foo1");
        Assert.assertEquals(consumer.receive(context, endpointConfiguration.getTimeout()).getPayload(String.class), "Foo2");
        Assert.assertEquals(consumer.receive(context, endpointConfiguration.getTimeout()).getPayload(String.class), "Foo3");
    }
}
