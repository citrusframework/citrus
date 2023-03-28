/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.jms.config.xml;

import org.citrusframework.TestActor;
import org.citrusframework.jms.endpoint.JmsSyncEndpoint;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class JmsSyncEndpointParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testJmsSyncEndpointAsConsumerParser() {
        Map<String, JmsSyncEndpoint> endpoints = beanDefinitionContext.getBeansOfType(JmsSyncEndpoint.class);

        Assert.assertEquals(endpoints.size(), 4);

        // 1st message receiver
        JmsSyncEndpoint jmsSyncEndpoint = endpoints.get("jmsSyncEndpoint1");
        Assert.assertNotNull(jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory(), beanDefinitionContext.getBean("connectionFactory"));
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getDestinationName(), "JMS.Queue.Test");
        Assert.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);

        // 2nd message receiver
        jmsSyncEndpoint = endpoints.get("jmsSyncEndpoint2");
        Assert.assertNotNull(jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory(), beanDefinitionContext.getBean("jmsConnectionFactory"));
        Assert.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getDestinationName());
        Assert.assertNotNull(jmsSyncEndpoint.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getCorrelator(), beanDefinitionContext.getBean("replyMessageCorrelator", MessageCorrelator.class));
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getDestinationResolver(), beanDefinitionContext.getBean("destinationResolver"));
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getDestinationNameResolver(), beanDefinitionContext.getBean("destinationNameResolver"));

        // 3rd message receiver
        jmsSyncEndpoint = endpoints.get("jmsSyncEndpoint3");
        Assert.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assert.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getDestinationName());
        Assert.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getCorrelator(), beanDefinitionContext.getBean("replyMessageCorrelator", MessageCorrelator.class));
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().isPubSubDomain(), true);

        // 4th message receiver
        jmsSyncEndpoint = endpoints.get("jmsSyncEndpoint4");
        Assert.assertNotNull(jmsSyncEndpoint.getActor());
        Assert.assertEquals(jmsSyncEndpoint.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }

    @Test
    public void testJmsSyncEndpointAsProducerParser() {
        beanDefinitionContext = createApplicationContext("context2");

        Map<String, JmsSyncEndpoint> endpoints = beanDefinitionContext.getBeansOfType(JmsSyncEndpoint.class);

        Assert.assertEquals(endpoints.size(), 4);

        // 1st message sender
        JmsSyncEndpoint jmsSyncEndpoint = endpoints.get("jmsSyncEndpoint1");
        Assert.assertNotNull(jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory(), beanDefinitionContext.getBean("connectionFactory"));
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getDestinationName(), "JMS.Queue.Test");
        Assert.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getPollingInterval(), 500L);
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getReplyDestinationName(), "JMS.Reply.Queue");
        Assert.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getReplyDestination());
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);

        // 2nd message sender
        jmsSyncEndpoint = endpoints.get("jmsSyncEndpoint2");
        Assert.assertNotNull(jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory(), beanDefinitionContext.getBean("jmsConnectionFactory"));
        Assert.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getDestinationName());
        Assert.assertNotNull(jmsSyncEndpoint.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getReplyDestinationName());
        Assert.assertNotNull(jmsSyncEndpoint.getEndpointConfiguration().getReplyDestination());
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getCorrelator(), beanDefinitionContext.getBean("replyMessageCorrelator", MessageCorrelator.class));

        // 3rd message sender
        jmsSyncEndpoint = endpoints.get("jmsSyncEndpoint3");
        Assert.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assert.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getDestinationName());
        Assert.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().isPubSubDomain(), true);
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getCorrelator(), beanDefinitionContext.getBean("replyMessageCorrelator", MessageCorrelator.class));

        // 4th message sender
        jmsSyncEndpoint = endpoints.get("jmsSyncEndpoint4");
        Assert.assertNotNull(jmsSyncEndpoint.getEndpointConfiguration().getPollingInterval());
        Assert.assertEquals(jmsSyncEndpoint.getEndpointConfiguration().getPollingInterval(), 250L);
        Assert.assertNotNull(jmsSyncEndpoint.getActor());
        Assert.assertEquals(jmsSyncEndpoint.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }
}
