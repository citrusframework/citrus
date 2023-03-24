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

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.jms.message.JmsMessageConverter;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class JmsEndpointParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testJmsEndpointParser() {
        Map<String, JmsEndpoint> endpoints = beanDefinitionContext.getBeansOfType(JmsEndpoint.class);

        Assert.assertEquals(endpoints.size(), 4);

        // 1st message receiver
        JmsEndpoint jmsEndpoint = endpoints.get("jmsEndpoint1");
        Assert.assertNotNull(jmsEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assert.assertEquals(jmsEndpoint.getEndpointConfiguration().getConnectionFactory(), beanDefinitionContext.getBean("connectionFactory"));
        Assert.assertEquals(jmsEndpoint.getEndpointConfiguration().getMessageConverter().getClass(), JmsMessageConverter.class);
        Assert.assertEquals(jmsEndpoint.getEndpointConfiguration().getDestinationName(), "JMS.Queue.Test");
        Assert.assertNull(jmsEndpoint.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsEndpoint.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertFalse(jmsEndpoint.getEndpointConfiguration().isPubSubDomain());
        Assert.assertFalse(jmsEndpoint.getEndpointConfiguration().isAutoStart());
        Assert.assertFalse(jmsEndpoint.getEndpointConfiguration().isDurableSubscription());
        Assert.assertFalse(jmsEndpoint.getEndpointConfiguration().isUseObjectMessages());
        Assert.assertTrue(jmsEndpoint.getEndpointConfiguration().isFilterInternalHeaders());

        // 2nd message receiver
        jmsEndpoint = endpoints.get("jmsEndpoint2");
        Assert.assertNotNull(jmsEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assert.assertEquals(jmsEndpoint.getEndpointConfiguration().getConnectionFactory(), beanDefinitionContext.getBean("jmsConnectionFactory"));
        Assert.assertEquals(jmsEndpoint.getEndpointConfiguration().getMessageConverter(), beanDefinitionContext.getBean("messageConverter"));
        Assert.assertEquals(jmsEndpoint.getEndpointConfiguration().getDestinationResolver(), beanDefinitionContext.getBean("destinationResolver"));
        Assert.assertEquals(jmsEndpoint.getEndpointConfiguration().getDestinationNameResolver(), beanDefinitionContext.getBean("destinationNameResolver"));
        Assert.assertNull(jmsEndpoint.getEndpointConfiguration().getDestinationName());
        Assert.assertNotNull(jmsEndpoint.getEndpointConfiguration().getDestination());
        Assert.assertEquals(jmsEndpoint.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd message receiver
        jmsEndpoint = endpoints.get("jmsEndpoint3");
        Assert.assertNull(jmsEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assert.assertNull(jmsEndpoint.getEndpointConfiguration().getDestinationName());
        Assert.assertNull(jmsEndpoint.getEndpointConfiguration().getDestination());
        Assert.assertTrue(jmsEndpoint.getEndpointConfiguration().isPubSubDomain());
        Assert.assertTrue(jmsEndpoint.getEndpointConfiguration().isAutoStart());
        Assert.assertTrue(jmsEndpoint.getEndpointConfiguration().isDurableSubscription());
        Assert.assertEquals(jmsEndpoint.getEndpointConfiguration().getDurableSubscriberName(), "durableSubscriber");
        Assert.assertTrue(jmsEndpoint.getEndpointConfiguration().isUseObjectMessages());
        Assert.assertFalse(jmsEndpoint.getEndpointConfiguration().isFilterInternalHeaders());

        // 4th message receiver
        jmsEndpoint = endpoints.get("jmsEndpoint4");
        Assert.assertNotNull(jmsEndpoint.getActor());
        Assert.assertEquals(jmsEndpoint.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }
}
