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

package com.consol.citrus.jms.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.endpoint.resolver.EndpointUriResolver;
import com.consol.citrus.jms.endpoint.JmsSyncEndpoint;
import com.consol.citrus.jms.message.JmsMessageConverter;
import com.consol.citrus.message.DefaultMessageCorrelator;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

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

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
    @Mock
    private Destination jmsQueue = Mockito.mock(Destination.class);
    @Mock
    private Destination replyQueue = Mockito.mock(Destination.class);
    @Mock
    private ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
    @Mock
    private ConnectionFactory jmsConnectionFactory = Mockito.mock(ConnectionFactory.class);
    @Mock
    private JmsMessageConverter messageConverter = Mockito.mock(JmsMessageConverter.class);
    @Mock
    private DestinationResolver destinationResolver = Mockito.mock(DestinationResolver.class);
    @Mock
    private EndpointUriResolver destinationNameResolver = Mockito.mock(EndpointUriResolver.class);
    @Mock
    private MessageCorrelator messageCorrelator = Mockito.mock(MessageCorrelator.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("jmsTemplate", JmsTemplate.class)).thenReturn(jmsTemplate);
        when(applicationContext.getBean("jmsQueue", Destination.class)).thenReturn(jmsQueue);
        when(applicationContext.getBean("replyQueue", Destination.class)).thenReturn(replyQueue);
        when(applicationContext.getBean("messageConverter", JmsMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("destinationResolver", DestinationResolver.class)).thenReturn(destinationResolver);
        when(applicationContext.getBean("destinationNameResolver", EndpointUriResolver.class)).thenReturn(destinationNameResolver);
        when(applicationContext.getBean("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(applicationContext.getBean("connectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        when(applicationContext.getBean("jmsConnectionFactory", ConnectionFactory.class)).thenReturn(jmsConnectionFactory);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
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
        Assert.assertEquals(jmsSyncEndpoint3.getEndpointConfiguration().isPubSubDomain(), true);

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

        // 7th message sender
        Assert.assertNull(jmsSyncEndpoint7.getEndpointConfiguration().getConnectionFactory());
        Assert.assertNull(jmsSyncEndpoint7.getEndpointConfiguration().getDestinationName());
        Assert.assertNull(jmsSyncEndpoint7.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsSyncEndpoint7.getEndpointConfiguration().isPubSubDomain(), true);
        Assert.assertEquals(jmsSyncEndpoint7.getEndpointConfiguration().getCorrelator(), messageCorrelator);

        // 8th message sender
        Assert.assertNotNull(jmsSyncEndpoint8.getEndpointConfiguration().getPollingInterval());
        Assert.assertEquals(jmsSyncEndpoint8.getEndpointConfiguration().getPollingInterval(), 250L);
        Assert.assertNotNull(jmsSyncEndpoint8.getActor());
        Assert.assertEquals(jmsSyncEndpoint8.getActor(), testActor);
    }
}
