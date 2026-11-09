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

package org.citrusframework.jmx.config.annotation;

import java.util.Map;
import javax.management.NotificationFilter;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.jmx.client.JmxClient;
import org.citrusframework.jmx.message.JmxMessageConverter;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class JmxClientConfigParserTest extends AbstractUnitTest {

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

    @BeforeEach
    public void setup() {
        lenient().when(referenceResolver.resolve("messageConverter", JmxMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("messageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        lenient().when(referenceResolver.resolve("notificationFilter", NotificationFilter.class)).thenReturn(notificationFilter);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeEach
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testJmxClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st client
        Assertions.assertEquals("platform", jmxClient1.getEndpointConfiguration().getServerUrl());
        Assertions.assertNull(jmxClient1.getEndpointConfiguration().getUsername());
        Assertions.assertNull(jmxClient1.getEndpointConfiguration().getPassword());
        Assertions.assertFalse(jmxClient1.getEndpointConfiguration().isAutoReconnect());
        Assertions.assertEquals(1000L, jmxClient1.getEndpointConfiguration().getDelayOnReconnect());
        Assertions.assertNull(jmxClient1.getEndpointConfiguration().getNotificationFilter());
        Assertions.assertNotNull(jmxClient1.getEndpointConfiguration().getMessageConverter());
        Assertions.assertNotNull(jmxClient1.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(5000L, jmxClient1.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(500L, jmxClient1.getEndpointConfiguration().getPollingInterval());
        Assertions.assertNull(jmxClient1.getActor());

        // 2nd client
        Assertions.assertEquals("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi", jmxClient2.getEndpointConfiguration().getServerUrl());
        Assertions.assertEquals("user", jmxClient2.getEndpointConfiguration().getUsername());
        Assertions.assertEquals("s!cr!t", jmxClient2.getEndpointConfiguration().getPassword());
        Assertions.assertTrue(jmxClient2.getEndpointConfiguration().isAutoReconnect());
        Assertions.assertEquals(5000L, jmxClient2.getEndpointConfiguration().getDelayOnReconnect());
        Assertions.assertEquals(notificationFilter, jmxClient2.getEndpointConfiguration().getNotificationFilter());
        Assertions.assertEquals(messageConverter, jmxClient2.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals(messageCorrelator, jmxClient2.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(10000L, jmxClient2.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(100L, jmxClient2.getEndpointConfiguration().getPollingInterval());
        Assertions.assertEquals(testActor, jmxClient2.getActor());
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = AnnotationConfigParser.lookup();
        Assertions.assertEquals(4L, validators.size());
        Assertions.assertNotNull(validators.get("direct.async"));
        Assertions.assertEquals(DirectEndpointConfigParser.class, validators.get("direct.async").getClass());
        Assertions.assertNotNull(validators.get("direct.sync"));
        Assertions.assertEquals(DirectSyncEndpointConfigParser.class, validators.get("direct.sync").getClass());
        Assertions.assertNotNull(validators.get("jmx.client"));
        Assertions.assertEquals(JmxClientConfigParser.class, validators.get("jmx.client").getClass());
        Assertions.assertNotNull(validators.get("jmx.server"));
        Assertions.assertEquals(JmxServerConfigParser.class, validators.get("jmx.server").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("jmx.client").isPresent());
    }
}
