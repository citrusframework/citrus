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

import javax.management.NotificationFilter;
import java.util.Map;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.config.annotation.AnnotationConfigParser;
import com.consol.citrus.endpoint.direct.annotation.DirectEndpointConfigParser;
import com.consol.citrus.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import com.consol.citrus.jmx.client.JmxClient;
import com.consol.citrus.jmx.message.JmxMessageConverter;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private JmxMessageConverter messageConverter;
    @Mock
    private MessageCorrelator messageCorrelator;
    @Mock
    private NotificationFilter notificationFilter;
    @Mock
    private TestActor testActor;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(referenceResolver.resolve("messageConverter", JmxMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("messageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(referenceResolver.resolve("notificationFilter", NotificationFilter.class)).thenReturn(notificationFilter);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
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

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 4L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("jmx.client"));
        Assert.assertEquals(validators.get("jmx.client").getClass(), JmxClientConfigParser.class);
        Assert.assertNotNull(validators.get("jmx.server"));
        Assert.assertEquals(validators.get("jmx.server").getClass(), JmxServerConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("jmx.client").isPresent());
    }
}
