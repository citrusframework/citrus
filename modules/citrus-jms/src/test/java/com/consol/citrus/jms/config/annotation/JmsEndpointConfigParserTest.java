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
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import com.consol.citrus.jms.message.JmsMessageConverter;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

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
            destination="jmsQueue")
    private JmsEndpoint jmsEndpoint2;

    @CitrusEndpoint
    @JmsEndpointConfig(pubSubDomain=true,
            useObjectMessages=true,
            jmsTemplate="jmsTemplate")
    private JmsEndpoint jmsEndpoint3;

    @CitrusEndpoint
    @JmsEndpointConfig(destinationName="JMS.Queue.Test",
            actor="testActor")
    private JmsEndpoint jmsEndpoint4;

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
    @Mock
    private Destination jmsQueue = Mockito.mock(Destination.class);
    @Mock
    private ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
    @Mock
    private ConnectionFactory jmsConnectionFactory = Mockito.mock(ConnectionFactory.class);
    @Mock
    private JmsMessageConverter messageConverter = Mockito.mock(JmsMessageConverter.class);
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
        when(applicationContext.getBean("messageConverter", JmsMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("connectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        when(applicationContext.getBean("jmsConnectionFactory", ConnectionFactory.class)).thenReturn(jmsConnectionFactory);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
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
        Assert.assertEquals(jmsEndpoint1.getEndpointConfiguration().isUseObjectMessages(), false);

        // 2nd message receiver
        Assert.assertNotNull(jmsEndpoint2.getEndpointConfiguration().getConnectionFactory());
        Assert.assertEquals(jmsEndpoint2.getEndpointConfiguration().getConnectionFactory(), jmsConnectionFactory);
        Assert.assertEquals(jmsEndpoint2.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertNull(jmsEndpoint2.getEndpointConfiguration().getDestinationName());
        Assert.assertNotNull(jmsEndpoint2.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsEndpoint2.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd message receiver
        Assert.assertNull(jmsEndpoint3.getEndpointConfiguration().getConnectionFactory());
        Assert.assertNull(jmsEndpoint3.getEndpointConfiguration().getDestinationName());
        Assert.assertNull(jmsEndpoint3.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsEndpoint3.getEndpointConfiguration().isPubSubDomain(), true);
        Assert.assertEquals(jmsEndpoint3.getEndpointConfiguration().isUseObjectMessages(), true);

        // 4th message receiver
        Assert.assertNotNull(jmsEndpoint4.getActor());
        Assert.assertEquals(jmsEndpoint4.getActor(), testActor);
    }
}
