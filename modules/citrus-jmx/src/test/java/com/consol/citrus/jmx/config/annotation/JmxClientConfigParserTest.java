/*
 *  Copyright 2006-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.consol.citrus.jmx.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.jmx.client.JmxClient;
import com.consol.citrus.jmx.message.JmxMessageConverter;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.management.NotificationFilter;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class JmxClientConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "jmxClient1")
    @JmxClientConfig
    private JmxClient jmxClient1;

    @CitrusEndpoint
    @JmxClientConfig(serverUrl="service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi",
            username="user",
            password="s!cr!t",
            autoReconnect=true,
            reconnectDelay=5000L,
            pollingInterval=100,
            timeout=10000L,
            notificationFilter="notificationFilter",
            messageConverter="messageConverter",
            correlator="messageCorrelator",
            actor="testActor")
    private JmxClient jmxClient2;

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private JmxMessageConverter messageConverter = Mockito.mock(JmxMessageConverter.class);
    @Mock
    private MessageCorrelator messageCorrelator = Mockito.mock(MessageCorrelator.class);
    @Mock
    private NotificationFilter notificationFilter = Mockito.mock(NotificationFilter.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("messageConverter", JmxMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("messageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(applicationContext.getBean("notificationFilter", NotificationFilter.class)).thenReturn(notificationFilter);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
    }

    @Test
    public void testJmxClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st client
        Assert.assertEquals(jmxClient1.getEndpointConfiguration().getServerUrl(), "platform");
        Assert.assertNull(jmxClient1.getEndpointConfiguration().getUsername());
        Assert.assertNull(jmxClient1.getEndpointConfiguration().getPassword());
        Assert.assertFalse(jmxClient1.getEndpointConfiguration().isAutoReconnect());
        Assert.assertEquals(jmxClient1.getEndpointConfiguration().getDelayOnReconnect(), 1000L);
        Assert.assertNull(jmxClient1.getEndpointConfiguration().getNotificationFilter());
        Assert.assertNotNull(jmxClient1.getEndpointConfiguration().getMessageConverter());
        Assert.assertNotNull(jmxClient1.getEndpointConfiguration().getCorrelator());
        Assert.assertEquals(jmxClient1.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(jmxClient1.getEndpointConfiguration().getPollingInterval(), 500L);
        Assert.assertNull(jmxClient1.getActor());

        // 2nd client
        Assert.assertEquals(jmxClient2.getEndpointConfiguration().getServerUrl(), "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
        Assert.assertEquals(jmxClient2.getEndpointConfiguration().getUsername(), "user");
        Assert.assertEquals(jmxClient2.getEndpointConfiguration().getPassword(), "s!cr!t");
        Assert.assertTrue(jmxClient2.getEndpointConfiguration().isAutoReconnect());
        Assert.assertEquals(jmxClient2.getEndpointConfiguration().getDelayOnReconnect(), 5000L);
        Assert.assertEquals(jmxClient2.getEndpointConfiguration().getNotificationFilter(), notificationFilter);
        Assert.assertEquals(jmxClient2.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertEquals(jmxClient2.getEndpointConfiguration().getCorrelator(), messageCorrelator);
        Assert.assertEquals(jmxClient2.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(jmxClient2.getEndpointConfiguration().getPollingInterval(), 100L);
        Assert.assertEquals(jmxClient2.getActor(), testActor);
    }
}
