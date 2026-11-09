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

package org.citrusframework.jmx.config.xml;

import org.citrusframework.TestActor;
import org.citrusframework.jmx.client.JmxClient;
import org.citrusframework.jmx.message.JmxMessageConverter;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.management.NotificationFilter;
import java.util.Map;

public class JmxClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testJmxClientParser() {
        Map<String, JmxClient> clients = beanDefinitionContext.getBeansOfType(JmxClient.class);

        Assertions.assertEquals(2, clients.size());

        // 1st client
        JmxClient jmxClient = clients.get("jmxClient1");
        Assertions.assertEquals("platform", jmxClient.getEndpointConfiguration().getServerUrl());
        Assertions.assertNull(jmxClient.getEndpointConfiguration().getUsername());
        Assertions.assertNull(jmxClient.getEndpointConfiguration().getPassword());
        Assertions.assertFalse(jmxClient.getEndpointConfiguration().isAutoReconnect());
        Assertions.assertEquals(1000L, jmxClient.getEndpointConfiguration().getDelayOnReconnect());
        Assertions.assertNull(jmxClient.getEndpointConfiguration().getNotificationFilter());
        Assertions.assertNotNull(jmxClient.getEndpointConfiguration().getMessageConverter());
        Assertions.assertNotNull(jmxClient.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(5000L, jmxClient.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(500L, jmxClient.getEndpointConfiguration().getPollingInterval());
        Assertions.assertNull(jmxClient.getActor());

        // 2nd client
        jmxClient = clients.get("jmxClient2");
        Assertions.assertEquals("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi", jmxClient.getEndpointConfiguration().getServerUrl());
        Assertions.assertEquals("user", jmxClient.getEndpointConfiguration().getUsername());
        Assertions.assertEquals("s!cr!t", jmxClient.getEndpointConfiguration().getPassword());
        Assertions.assertTrue(jmxClient.getEndpointConfiguration().isAutoReconnect());
        Assertions.assertEquals(5000L, jmxClient.getEndpointConfiguration().getDelayOnReconnect());
        Assertions.assertEquals(beanDefinitionContext.getBean("notificationFilter", NotificationFilter.class), jmxClient.getEndpointConfiguration().getNotificationFilter());
        Assertions.assertEquals(beanDefinitionContext.getBean("messageConverter", JmxMessageConverter.class), jmxClient.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals(beanDefinitionContext.getBean("messageCorrelator", MessageCorrelator.class), jmxClient.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(10000L, jmxClient.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(100L, jmxClient.getEndpointConfiguration().getPollingInterval());
        Assertions.assertEquals(beanDefinitionContext.getBean("actor", TestActor.class), jmxClient.getActor());
    }
}
