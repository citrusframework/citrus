/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.jms.config.annotation;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.Topic;
import jakarta.jms.TopicConnection;
import jakarta.jms.TopicConnectionFactory;
import jakarta.jms.TopicSession;
import jakarta.jms.TopicSubscriber;
import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.config.annotation.ChannelEndpointConfigParser;
import org.citrusframework.config.annotation.ChannelSyncEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.jms.message.JmsMessageConverter;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class JmsEndpointConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint
    @JmsEndpointConfig(destinationName="JMS.Queue.Test")
    private JmsEndpoint jmsEndpoint1;

    @CitrusEndpoint
    @JmsEndpointConfig(connectionFactory="jmsConnectionFactory",
            timeout=10000L,
            messageConverter="messageConverter",
            destinationResolver="destinationResolver",
            destinationNameResolver="destinationNameResolver",
            destination="jmsQueue")
    private JmsEndpoint jmsEndpoint2;

    @CitrusEndpoint
    @JmsEndpointConfig(pubSubDomain=true,
            autoStart=true,
            useObjectMessages=true,
            filterInternalHeaders=false,
            jmsTemplate="jmsTemplate")
    private JmsEndpoint jmsEndpoint3;

    @CitrusEndpoint
    @JmsEndpointConfig(pubSubDomain=true,
            autoStart=true,
            durableSubscription=true,
            durableSubscriberName="durableSubscriber",
            useObjectMessages=true,
            destinationName = "JMS.Topic.Test",
            connectionFactory="jmsTopicConnectionFactory")
    private JmsEndpoint jmsEndpoint4;

    @CitrusEndpoint
    @JmsEndpointConfig(destinationName="JMS.Queue.Test",
            actor="testActor")
    private JmsEndpoint jmsEndpoint5;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private JmsTemplate jmsTemplate;
    @Mock
    private Destination jmsQueue;
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
    private DestinationResolver destinationResolver;
    @Mock
    private EndpointUriResolver destinationNameResolver;
    @Mock
    private TestActor testActor;

    @BeforeClass
    public void setup() throws JMSException {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("jmsTemplate", JmsTemplate.class)).thenReturn(jmsTemplate);

        when(jmsTemplate.getConnectionFactory()).thenReturn(topicConnectionFactory);
        when(topicConnectionFactory.createTopicConnection()).thenReturn(topicConnection);
        when(topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(topicSession);
        when(jmsTemplate.getDefaultDestinationName()).thenReturn("JMS.Topic.Test");
        when(topicSession.createTopic("JMS.Topic.Test")).thenReturn(topic);
        when(topicSession.createSubscriber(topic)).thenReturn(topicSubscriber);
        when(topicSession.createDurableSubscriber(topic, "durableSubscriber")).thenReturn(topicSubscriber);

        when(referenceResolver.resolve("jmsQueue", Destination.class)).thenReturn(jmsQueue);
        when(referenceResolver.resolve("messageConverter", JmsMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("destinationResolver", DestinationResolver.class)).thenReturn(destinationResolver);
        when(referenceResolver.resolve("destinationNameResolver", EndpointUriResolver.class)).thenReturn(destinationNameResolver);
        when(referenceResolver.resolve("connectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        when(referenceResolver.resolve("jmsConnectionFactory", ConnectionFactory.class)).thenReturn(jmsConnectionFactory);
        when(referenceResolver.resolve("jmsTopicConnectionFactory", ConnectionFactory.class)).thenReturn(topicConnectionFactory);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testJmsEndpointParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assert.assertNotNull(jmsEndpoint1.getEndpointConfiguration().getConnectionFactory());
        Assert.assertEquals(jmsEndpoint1.getEndpointConfiguration().getConnectionFactory(), connectionFactory);
        Assert.assertEquals(jmsEndpoint1.getEndpointConfiguration().getMessageConverter().getClass(), JmsMessageConverter.class);
        Assert.assertEquals(jmsEndpoint1.getEndpointConfiguration().getDestinationName(), "JMS.Queue.Test");
        Assert.assertNull(jmsEndpoint1.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsEndpoint1.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertFalse(jmsEndpoint1.getEndpointConfiguration().isPubSubDomain());
        Assert.assertFalse(jmsEndpoint1.getEndpointConfiguration().isAutoStart());
        Assert.assertFalse(jmsEndpoint1.getEndpointConfiguration().isDurableSubscription());
        Assert.assertFalse(jmsEndpoint1.getEndpointConfiguration().isUseObjectMessages());
        Assert.assertTrue(jmsEndpoint1.getEndpointConfiguration().isFilterInternalHeaders());

        // 2nd message receiver
        Assert.assertNotNull(jmsEndpoint2.getEndpointConfiguration().getConnectionFactory());
        Assert.assertEquals(jmsEndpoint2.getEndpointConfiguration().getConnectionFactory(), jmsConnectionFactory);
        Assert.assertEquals(jmsEndpoint2.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertEquals(jmsEndpoint2.getEndpointConfiguration().getDestinationResolver(), destinationResolver);
        Assert.assertEquals(jmsEndpoint2.getEndpointConfiguration().getDestinationNameResolver(), destinationNameResolver);
        Assert.assertNull(jmsEndpoint2.getEndpointConfiguration().getDestinationName());
        Assert.assertNotNull(jmsEndpoint2.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsEndpoint2.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd message receiver
        Assert.assertEquals(jmsEndpoint3.getEndpointConfiguration().getJmsTemplate(), jmsTemplate);
        Assert.assertNull(jmsEndpoint3.getEndpointConfiguration().getConnectionFactory());
        Assert.assertNull(jmsEndpoint3.getEndpointConfiguration().getDestinationName());
        Assert.assertNull(jmsEndpoint3.getEndpointConfiguration().getDestination());
        Assert.assertTrue(jmsEndpoint3.getEndpointConfiguration().isPubSubDomain());
        Assert.assertTrue(jmsEndpoint3.getEndpointConfiguration().isAutoStart());
        Assert.assertTrue(jmsEndpoint3.getEndpointConfiguration().isUseObjectMessages());
        Assert.assertFalse(jmsEndpoint3.getEndpointConfiguration().isFilterInternalHeaders());

        // 4th message receiver
        Assert.assertEquals(jmsEndpoint4.getEndpointConfiguration().getConnectionFactory(), topicConnectionFactory);
        Assert.assertEquals(jmsEndpoint4.getEndpointConfiguration().getDestinationName(), "JMS.Topic.Test");
        Assert.assertNull(jmsEndpoint4.getEndpointConfiguration().getDestination());
        Assert.assertTrue(jmsEndpoint4.getEndpointConfiguration().isPubSubDomain());
        Assert.assertTrue(jmsEndpoint4.getEndpointConfiguration().isAutoStart());
        Assert.assertTrue(jmsEndpoint4.getEndpointConfiguration().isDurableSubscription());
        Assert.assertEquals(jmsEndpoint4.getEndpointConfiguration().getDurableSubscriberName(), "durableSubscriber");
        Assert.assertTrue(jmsEndpoint4.getEndpointConfiguration().isUseObjectMessages());

        // 5th message receiver
        Assert.assertNotNull(jmsEndpoint5.getActor());
        Assert.assertEquals(jmsEndpoint5.getActor(), testActor);
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 6L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("jms.async"));
        Assert.assertEquals(validators.get("jms.async").getClass(), JmsEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("jms.sync"));
        Assert.assertEquals(validators.get("jms.sync").getClass(), JmsSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("channel.async"));
        Assert.assertEquals(validators.get("channel.async").getClass(), ChannelEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("channel.sync"));
        Assert.assertEquals(validators.get("channel.sync").getClass(), ChannelSyncEndpointConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("jms.async").isPresent());
    }
}
