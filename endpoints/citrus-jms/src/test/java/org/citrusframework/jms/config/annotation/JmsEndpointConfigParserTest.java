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

package org.citrusframework.jms.config.annotation;

import java.util.Map;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.Topic;
import jakarta.jms.TopicConnection;
import jakarta.jms.TopicConnectionFactory;
import jakarta.jms.TopicSession;
import jakarta.jms.TopicSubscriber;
import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.springintegration.config.annotation.ChannelEndpointConfigParser;
import org.citrusframework.springintegration.config.annotation.ChannelSyncEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.jms.message.JmsMessageConverter;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class JmsEndpointConfigParserTest extends AbstractUnitTest {

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

    @BeforeEach
    public void setup() throws JMSException {

        lenient().when(referenceResolver.resolve("jmsTemplate", JmsTemplate.class)).thenReturn(jmsTemplate);

        lenient().when(jmsTemplate.getConnectionFactory()).thenReturn(topicConnectionFactory);
        lenient().when(topicConnectionFactory.createTopicConnection()).thenReturn(topicConnection);
        lenient().when(topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(topicSession);
        lenient().when(jmsTemplate.getDefaultDestinationName()).thenReturn("JMS.Topic.Test");
        lenient().when(topicSession.createTopic("JMS.Topic.Test")).thenReturn(topic);
        lenient().when(topicSession.createSubscriber(topic)).thenReturn(topicSubscriber);
        lenient().when(topicSession.createDurableSubscriber(topic, "durableSubscriber")).thenReturn(topicSubscriber);

        lenient().when(referenceResolver.resolve("jmsQueue", Destination.class)).thenReturn(jmsQueue);
        lenient().when(referenceResolver.resolve("messageConverter", JmsMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("destinationResolver", DestinationResolver.class)).thenReturn(destinationResolver);
        lenient().when(referenceResolver.resolve("destinationNameResolver", EndpointUriResolver.class)).thenReturn(destinationNameResolver);
        lenient().when(referenceResolver.resolve("connectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        lenient().when(referenceResolver.resolve("jmsConnectionFactory", ConnectionFactory.class)).thenReturn(jmsConnectionFactory);
        lenient().when(referenceResolver.resolve("jmsTopicConnectionFactory", ConnectionFactory.class)).thenReturn(topicConnectionFactory);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeEach
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testJmsEndpointParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assertions.assertNotNull(jmsEndpoint1.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals(connectionFactory, jmsEndpoint1.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals(JmsMessageConverter.class, jmsEndpoint1.getEndpointConfiguration().getMessageConverter().getClass());
        Assertions.assertEquals("JMS.Queue.Test", jmsEndpoint1.getEndpointConfiguration().getDestinationName());
        Assertions.assertNull(jmsEndpoint1.getEndpointConfiguration().getDestination());
        Assertions.assertEquals(5000L, jmsEndpoint1.getEndpointConfiguration().getTimeout());
        Assertions.assertFalse(jmsEndpoint1.getEndpointConfiguration().isPubSubDomain());
        Assertions.assertFalse(jmsEndpoint1.getEndpointConfiguration().isAutoStart());
        Assertions.assertFalse(jmsEndpoint1.getEndpointConfiguration().isDurableSubscription());
        Assertions.assertFalse(jmsEndpoint1.getEndpointConfiguration().isUseObjectMessages());
        Assertions.assertTrue(jmsEndpoint1.getEndpointConfiguration().isFilterInternalHeaders());

        // 2nd message receiver
        Assertions.assertNotNull(jmsEndpoint2.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals(jmsConnectionFactory, jmsEndpoint2.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals(messageConverter, jmsEndpoint2.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals(destinationResolver, jmsEndpoint2.getEndpointConfiguration().getDestinationResolver());
        Assertions.assertEquals(destinationNameResolver, jmsEndpoint2.getEndpointConfiguration().getDestinationNameResolver());
        Assertions.assertNull(jmsEndpoint2.getEndpointConfiguration().getDestinationName());
        Assertions.assertNotNull(jmsEndpoint2.getEndpointConfiguration().getDestination());
        Assertions.assertEquals(10000L, jmsEndpoint2.getEndpointConfiguration().getTimeout());

        // 3rd message receiver
        Assertions.assertEquals(jmsTemplate, jmsEndpoint3.getEndpointConfiguration().getJmsTemplate());
        Assertions.assertNull(jmsEndpoint3.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertNull(jmsEndpoint3.getEndpointConfiguration().getDestinationName());
        Assertions.assertNull(jmsEndpoint3.getEndpointConfiguration().getDestination());
        Assertions.assertTrue(jmsEndpoint3.getEndpointConfiguration().isPubSubDomain());
        Assertions.assertTrue(jmsEndpoint3.getEndpointConfiguration().isAutoStart());
        Assertions.assertTrue(jmsEndpoint3.getEndpointConfiguration().isUseObjectMessages());
        Assertions.assertFalse(jmsEndpoint3.getEndpointConfiguration().isFilterInternalHeaders());

        // 4th message receiver
        Assertions.assertEquals(topicConnectionFactory, jmsEndpoint4.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals("JMS.Topic.Test", jmsEndpoint4.getEndpointConfiguration().getDestinationName());
        Assertions.assertNull(jmsEndpoint4.getEndpointConfiguration().getDestination());
        Assertions.assertTrue(jmsEndpoint4.getEndpointConfiguration().isPubSubDomain());
        Assertions.assertTrue(jmsEndpoint4.getEndpointConfiguration().isAutoStart());
        Assertions.assertTrue(jmsEndpoint4.getEndpointConfiguration().isDurableSubscription());
        Assertions.assertEquals("durableSubscriber", jmsEndpoint4.getEndpointConfiguration().getDurableSubscriberName());
        Assertions.assertTrue(jmsEndpoint4.getEndpointConfiguration().isUseObjectMessages());

        // 5th message receiver
        Assertions.assertNotNull(jmsEndpoint5.getActor());
        Assertions.assertEquals(testActor, jmsEndpoint5.getActor());
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = AnnotationConfigParser.lookup();
        Assertions.assertEquals(6L, validators.size());
        Assertions.assertNotNull(validators.get("direct.async"));
        Assertions.assertEquals(DirectEndpointConfigParser.class, validators.get("direct.async").getClass());
        Assertions.assertNotNull(validators.get("direct.sync"));
        Assertions.assertEquals(DirectSyncEndpointConfigParser.class, validators.get("direct.sync").getClass());
        Assertions.assertNotNull(validators.get("jms.async"));
        Assertions.assertEquals(JmsEndpointConfigParser.class, validators.get("jms.async").getClass());
        Assertions.assertNotNull(validators.get("jms.sync"));
        Assertions.assertEquals(JmsSyncEndpointConfigParser.class, validators.get("jms.sync").getClass());
        Assertions.assertNotNull(validators.get("channel.async"));
        Assertions.assertEquals(ChannelEndpointConfigParser.class, validators.get("channel.async").getClass());
        Assertions.assertNotNull(validators.get("channel.sync"));
        Assertions.assertEquals(ChannelSyncEndpointConfigParser.class, validators.get("channel.sync").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("jms.async").isPresent());
    }
}
