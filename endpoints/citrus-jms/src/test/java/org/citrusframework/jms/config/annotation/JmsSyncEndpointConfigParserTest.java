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

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.jms.endpoint.JmsSyncEndpoint;
import org.citrusframework.jms.message.JmsMessageConverter;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
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
public class JmsSyncEndpointConfigParserTest extends AbstractUnitTest {

    @CitrusEndpoint
    @JmsSyncEndpointConfig(destinationName="JMS.Queue.Test")
    private JmsSyncEndpoint jmsSyncEndpoint1;

    @CitrusEndpoint
    @JmsSyncEndpointConfig(connectionFactory="jmsConnectionFactory",
            timeout=10000L,
            destination="jmsQueue",
            correlator="replyMessageCorrelator")
    private JmsSyncEndpoint jmsSyncEndpoint2;

    @CitrusEndpoint
    @JmsSyncEndpointConfig(pubSubDomain=true,
            jmsTemplate="jmsTemplate",
            correlator="replyMessageCorrelator")
    private JmsSyncEndpoint jmsSyncEndpoint3;

    @CitrusEndpoint
    @JmsSyncEndpointConfig(destinationName="JMS.Queue.Test",
            actor="testActor")
    private JmsSyncEndpoint jmsSyncEndpoint4;

    @CitrusEndpoint
    @JmsSyncEndpointConfig(destinationName="JMS.Queue.Test",
            replyDestinationName="JMS.Reply.Queue")
    private JmsSyncEndpoint jmsSyncEndpoint5;

    @CitrusEndpoint
    @JmsSyncEndpointConfig(timeout=10000L,
            connectionFactory="jmsConnectionFactory",
            destination="jmsQueue",
            replyDestination="replyQueue",
            destinationResolver="destinationResolver",
            destinationNameResolver="destinationNameResolver",
            useObjectMessages = true,
            filterInternalHeaders = false,
            correlator="replyMessageCorrelator")
    private JmsSyncEndpoint jmsSyncEndpoint6;

    @CitrusEndpoint
    @JmsSyncEndpointConfig(pubSubDomain=true,
            jmsTemplate="jmsTemplate",
            correlator="replyMessageCorrelator")
    private JmsSyncEndpoint jmsSyncEndpoint7;

    @CitrusEndpoint
    @JmsSyncEndpointConfig(destinationName="JMS.Queue.Test",
            replyDestinationName="JMS.Reply.Queue",
            pollingInterval=250,
            actor="testActor")
    private JmsSyncEndpoint jmsSyncEndpoint8;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private JmsTemplate jmsTemplate;
    @Mock
    private Destination jmsQueue;
    @Mock
    private Destination replyQueue;
    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private ConnectionFactory jmsConnectionFactory;
    @Mock
    private JmsMessageConverter messageConverter;
    @Mock
    private DestinationResolver destinationResolver;
    @Mock
    private EndpointUriResolver destinationNameResolver;
    @Mock
    private MessageCorrelator messageCorrelator;
    @Mock
    private TestActor testActor;

    @BeforeEach
    public void setup() {

        lenient().when(referenceResolver.resolve("jmsTemplate", JmsTemplate.class)).thenReturn(jmsTemplate);
        lenient().when(referenceResolver.resolve("jmsQueue", Destination.class)).thenReturn(jmsQueue);
        lenient().when(referenceResolver.resolve("replyQueue", Destination.class)).thenReturn(replyQueue);
        lenient().when(referenceResolver.resolve("messageConverter", JmsMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("destinationResolver", DestinationResolver.class)).thenReturn(destinationResolver);
        lenient().when(referenceResolver.resolve("destinationNameResolver", EndpointUriResolver.class)).thenReturn(destinationNameResolver);
        lenient().when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        lenient().when(referenceResolver.resolve("connectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        lenient().when(referenceResolver.resolve("jmsConnectionFactory", ConnectionFactory.class)).thenReturn(jmsConnectionFactory);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeEach
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testJmsSyncEndpointAsConsumerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assertions.assertNotNull(jmsSyncEndpoint1.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals(connectionFactory, jmsSyncEndpoint1.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals("JMS.Queue.Test", jmsSyncEndpoint1.getEndpointConfiguration().getDestinationName());
        Assertions.assertNull(jmsSyncEndpoint1.getEndpointConfiguration().getDestination());
        Assertions.assertEquals(5000L, jmsSyncEndpoint1.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(DefaultMessageCorrelator.class, jmsSyncEndpoint1.getEndpointConfiguration().getCorrelator().getClass());

        // 2nd message receiver
        Assertions.assertNotNull(jmsSyncEndpoint2.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals(jmsConnectionFactory, jmsSyncEndpoint2.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertNull(jmsSyncEndpoint2.getEndpointConfiguration().getDestinationName());
        Assertions.assertNotNull(jmsSyncEndpoint2.getEndpointConfiguration().getDestination());
        Assertions.assertEquals(10000L, jmsSyncEndpoint2.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(messageCorrelator, jmsSyncEndpoint2.getEndpointConfiguration().getCorrelator());

        // 3rd message receiver
        Assertions.assertNull(jmsSyncEndpoint3.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertNull(jmsSyncEndpoint3.getEndpointConfiguration().getDestinationName());
        Assertions.assertNull(jmsSyncEndpoint3.getEndpointConfiguration().getDestination());
        Assertions.assertEquals(messageCorrelator, jmsSyncEndpoint3.getEndpointConfiguration().getCorrelator());
        Assertions.assertTrue(jmsSyncEndpoint3.getEndpointConfiguration().isPubSubDomain());
        Assertions.assertFalse(jmsSyncEndpoint3.getEndpointConfiguration().isUseObjectMessages());
        Assertions.assertTrue(jmsSyncEndpoint3.getEndpointConfiguration().isFilterInternalHeaders());

        // 4th message receiver
        Assertions.assertNotNull(jmsSyncEndpoint4.getActor());
        Assertions.assertEquals(testActor, jmsSyncEndpoint4.getActor());

        // 5th message receiver
        Assertions.assertNotNull(jmsSyncEndpoint5.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals(connectionFactory, jmsSyncEndpoint5.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals("JMS.Queue.Test", jmsSyncEndpoint5.getEndpointConfiguration().getDestinationName());
        Assertions.assertNull(jmsSyncEndpoint5.getEndpointConfiguration().getDestination());
        Assertions.assertEquals(5000L, jmsSyncEndpoint5.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(500L, jmsSyncEndpoint5.getEndpointConfiguration().getPollingInterval());
        Assertions.assertEquals("JMS.Reply.Queue", jmsSyncEndpoint5.getEndpointConfiguration().getReplyDestinationName());
        Assertions.assertNull(jmsSyncEndpoint5.getEndpointConfiguration().getReplyDestination());
        Assertions.assertEquals(DefaultMessageCorrelator.class, jmsSyncEndpoint5.getEndpointConfiguration().getCorrelator().getClass());

        // 6th message sender
        Assertions.assertNotNull(jmsSyncEndpoint6.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals(jmsConnectionFactory, jmsSyncEndpoint6.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertNull(jmsSyncEndpoint6.getEndpointConfiguration().getDestinationName());
        Assertions.assertNotNull(jmsSyncEndpoint6.getEndpointConfiguration().getDestination());
        Assertions.assertEquals(10000L, jmsSyncEndpoint6.getEndpointConfiguration().getTimeout());
        Assertions.assertNull(jmsSyncEndpoint6.getEndpointConfiguration().getReplyDestinationName());
        Assertions.assertNotNull(jmsSyncEndpoint6.getEndpointConfiguration().getReplyDestination());
        Assertions.assertEquals(messageCorrelator, jmsSyncEndpoint6.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(destinationResolver, jmsSyncEndpoint6.getEndpointConfiguration().getDestinationResolver());
        Assertions.assertEquals(destinationNameResolver, jmsSyncEndpoint6.getEndpointConfiguration().getDestinationNameResolver());
        Assertions.assertTrue(jmsSyncEndpoint6.getEndpointConfiguration().isUseObjectMessages());
        Assertions.assertFalse(jmsSyncEndpoint6.getEndpointConfiguration().isFilterInternalHeaders());

        // 7th message sender
        Assertions.assertNull(jmsSyncEndpoint7.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertNull(jmsSyncEndpoint7.getEndpointConfiguration().getDestinationName());
        Assertions.assertNull(jmsSyncEndpoint7.getEndpointConfiguration().getDestination());
        Assertions.assertTrue(jmsSyncEndpoint7.getEndpointConfiguration().isPubSubDomain());
        Assertions.assertEquals(messageCorrelator, jmsSyncEndpoint7.getEndpointConfiguration().getCorrelator());

        // 8th message sender
        Assertions.assertEquals(250L, jmsSyncEndpoint8.getEndpointConfiguration().getPollingInterval());
        Assertions.assertNotNull(jmsSyncEndpoint8.getActor());
        Assertions.assertEquals(testActor, jmsSyncEndpoint8.getActor());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("jms.sync").isPresent());
    }
}
