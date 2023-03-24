/*
 * Copyright 2006-2015 the original author or authors.
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
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.management.NotificationFilter;
import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class JmxClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testJmxClientParser() {
        Map<String, JmxClient> clients = beanDefinitionContext.getBeansOfType(JmxClient.class);

        Assert.assertEquals(clients.size(), 2);

        // 1st client
        JmxClient jmxClient = clients.get("jmxClient1");
        Assert.assertEquals(jmxClient.getEndpointConfiguration().getServerUrl(), "platform");
        Assert.assertNull(jmxClient.getEndpointConfiguration().getUsername());
        Assert.assertNull(jmxClient.getEndpointConfiguration().getPassword());
        Assert.assertFalse(jmxClient.getEndpointConfiguration().isAutoReconnect());
        Assert.assertEquals(jmxClient.getEndpointConfiguration().getDelayOnReconnect(), 1000L);
        Assert.assertNull(jmxClient.getEndpointConfiguration().getNotificationFilter());
        Assert.assertNotNull(jmxClient.getEndpointConfiguration().getMessageConverter());
        Assert.assertNotNull(jmxClient.getEndpointConfiguration().getCorrelator());
        Assert.assertEquals(jmxClient.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(jmxClient.getEndpointConfiguration().getPollingInterval(), 500L);
        Assert.assertNull(jmxClient.getActor());

        // 2nd client
        jmxClient = clients.get("jmxClient2");
        Assert.assertEquals(jmxClient.getEndpointConfiguration().getServerUrl(), "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
        Assert.assertEquals(jmxClient.getEndpointConfiguration().getUsername(), "user");
        Assert.assertEquals(jmxClient.getEndpointConfiguration().getPassword(), "s!cr!t");
        Assert.assertTrue(jmxClient.getEndpointConfiguration().isAutoReconnect());
        Assert.assertEquals(jmxClient.getEndpointConfiguration().getDelayOnReconnect(), 5000L);
        Assert.assertEquals(jmxClient.getEndpointConfiguration().getNotificationFilter(), beanDefinitionContext.getBean("notificationFilter", NotificationFilter.class));
        Assert.assertEquals(jmxClient.getEndpointConfiguration().getMessageConverter(), beanDefinitionContext.getBean("messageConverter", JmxMessageConverter.class));
        Assert.assertEquals(jmxClient.getEndpointConfiguration().getCorrelator(), beanDefinitionContext.getBean("messageCorrelator", MessageCorrelator.class));
        Assert.assertEquals(jmxClient.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(jmxClient.getEndpointConfiguration().getPollingInterval(), 100L);
        Assert.assertEquals(jmxClient.getActor(), beanDefinitionContext.getBean("actor", TestActor.class));
    }
}
