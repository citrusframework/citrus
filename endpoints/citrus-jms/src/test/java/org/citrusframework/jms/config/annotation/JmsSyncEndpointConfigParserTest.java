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

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.jms.endpoint.JmsSyncEndpoint;
import org.citrusframework.jms.message.JmsMessageConverter;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
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
public class JmsSyncEndpointConfigParserTest extends AbstractTestNGUnitTest {

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

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("jmsTemplate", JmsTemplate.class)).thenReturn(jmsTemplate);
        when(referenceResolver.resolve("jmsQueue", Destination.class)).thenReturn(jmsQueue);
        when(referenceResolver.resolve("replyQueue", Destination.class)).thenReturn(replyQueue);
        when(referenceResolver.resolve("messageConverter", JmsMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("destinationResolver", DestinationResolver.class)).thenReturn(destinationResolver);
        when(referenceResolver.resolve("destinationNameResolver", EndpointUriResolver.class)).thenReturn(destinationNameResolver);
        when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(referenceResolver.resolve("connectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        when(referenceResolver.resolve("jmsConnectionFactory", ConnectionFactory.class)).thenReturn(jmsConnectionFactory);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testJmsSyncEndpointAsConsumerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assert.assertNotNull(jmsSyncEndpoint1.getEndpointConfiguration().getConnectionFactory());
        Assert.assertEquals(jmsSyncEndpoint1.getEndpointConfiguration().getConnectionFactory(), connectionFactory);
        Assert.assertEquals(jmsSyncEndpoint1.getEndpointConfiguration().getDestinationName(), "JMS.Queue.Test");
        Assert.assertNull(jmsSyncEndpoint1.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsSyncEndpoint1.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(jmsSyncEndpoint1.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);

        // 2nd message receiver
        Assert.assertNotNull(jmsSyncEndpoint2.getEndpointConfiguration().getConnectionFactory());
        Assert.assertEquals(jmsSyncEndpoint2.getEndpointConfiguration().getConnectionFactory(), jmsConnectionFactory);
        Assert.assertNull(jmsSyncEndpoint2.getEndpointConfiguration().getDestinationName());
        Assert.assertNotNull(jmsSyncEndpoint2.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsSyncEndpoint2.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(jmsSyncEndpoint2.getEndpointConfiguration().getCorrelator(), messageCorrelator);

        // 3rd message receiver
        Assert.assertNull(jmsSyncEndpoint3.getEndpointConfiguration().getConnectionFactory());
        Assert.assertNull(jmsSyncEndpoint3.getEndpointConfiguration().getDestinationName());
        Assert.assertNull(jmsSyncEndpoint3.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsSyncEndpoint3.getEndpointConfiguration().getCorrelator(), messageCorrelator);
        Assert.assertTrue(jmsSyncEndpoint3.getEndpointConfiguration().isPubSubDomain());
        Assert.assertFalse(jmsSyncEndpoint3.getEndpointConfiguration().isUseObjectMessages());
        Assert.assertTrue(jmsSyncEndpoint3.getEndpointConfiguration().isFilterInternalHeaders());

        // 4th message receiver
        Assert.assertNotNull(jmsSyncEndpoint4.getActor());
        Assert.assertEquals(jmsSyncEndpoint4.getActor(), testActor);

        // 5th message receiver
        Assert.assertNotNull(jmsSyncEndpoint5.getEndpointConfiguration().getConnectionFactory());
        Assert.assertEquals(jmsSyncEndpoint5.getEndpointConfiguration().getConnectionFactory(), connectionFactory);
        Assert.assertEquals(jmsSyncEndpoint5.getEndpointConfiguration().getDestinationName(), "JMS.Queue.Test");
        Assert.assertNull(jmsSyncEndpoint5.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsSyncEndpoint5.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(jmsSyncEndpoint5.getEndpointConfiguration().getPollingInterval(), 500L);
        Assert.assertEquals(jmsSyncEndpoint5.getEndpointConfiguration().getReplyDestinationName(), "JMS.Reply.Queue");
        Assert.assertNull(jmsSyncEndpoint5.getEndpointConfiguration().getReplyDestination());
        Assert.assertEquals(jmsSyncEndpoint5.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);

        // 6th message sender
        Assert.assertNotNull(jmsSyncEndpoint6.getEndpointConfiguration().getConnectionFactory());
        Assert.assertEquals(jmsSyncEndpoint6.getEndpointConfiguration().getConnectionFactory(), jmsConnectionFactory);
        Assert.assertNull(jmsSyncEndpoint6.getEndpointConfiguration().getDestinationName());
        Assert.assertNotNull(jmsSyncEndpoint6.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsSyncEndpoint6.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertNull(jmsSyncEndpoint6.getEndpointConfiguration().getReplyDestinationName());
        Assert.assertNotNull(jmsSyncEndpoint6.getEndpointConfiguration().getReplyDestination());
        Assert.assertEquals(jmsSyncEndpoint6.getEndpointConfiguration().getCorrelator(), messageCorrelator);
        Assert.assertEquals(jmsSyncEndpoint6.getEndpointConfiguration().getDestinationResolver(), destinationResolver);
        Assert.assertEquals(jmsSyncEndpoint6.getEndpointConfiguration().getDestinationNameResolver(), destinationNameResolver);
        Assert.assertTrue(jmsSyncEndpoint6.getEndpointConfiguration().isUseObjectMessages());
        Assert.assertFalse(jmsSyncEndpoint6.getEndpointConfiguration().isFilterInternalHeaders());

        // 7th message sender
        Assert.assertNull(jmsSyncEndpoint7.getEndpointConfiguration().getConnectionFactory());
        Assert.assertNull(jmsSyncEndpoint7.getEndpointConfiguration().getDestinationName());
        Assert.assertNull(jmsSyncEndpoint7.getEndpointConfiguration().getDestination());
        Assert.assertTrue(jmsSyncEndpoint7.getEndpointConfiguration().isPubSubDomain());
        Assert.assertEquals(jmsSyncEndpoint7.getEndpointConfiguration().getCorrelator(), messageCorrelator);

        // 8th message sender
        Assert.assertEquals(jmsSyncEndpoint8.getEndpointConfiguration().getPollingInterval(), 250L);
        Assert.assertNotNull(jmsSyncEndpoint8.getActor());
        Assert.assertEquals(jmsSyncEndpoint8.getActor(), testActor);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("jms.sync").isPresent());
    }
}
