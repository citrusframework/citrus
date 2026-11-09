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

package org.citrusframework.jms.config.xml;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.jms.message.JmsMessageConverter;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JmsEndpointParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testJmsEndpointParser() {
        Map<String, JmsEndpoint> endpoints = beanDefinitionContext.getBeansOfType(JmsEndpoint.class);

        Assertions.assertEquals(4, endpoints.size());

        // 1st message receiver
        JmsEndpoint jmsEndpoint = endpoints.get("jmsEndpoint1");
        Assertions.assertNotNull(jmsEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals(beanDefinitionContext.getBean("connectionFactory"), jmsEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals(JmsMessageConverter.class, jmsEndpoint.getEndpointConfiguration().getMessageConverter().getClass());
        Assertions.assertEquals("JMS.Queue.Test", jmsEndpoint.getEndpointConfiguration().getDestinationName());
        Assertions.assertNull(jmsEndpoint.getEndpointConfiguration().getDestination());
        Assertions.assertEquals(5000L, jmsEndpoint.getEndpointConfiguration().getTimeout());
        Assertions.assertFalse(jmsEndpoint.getEndpointConfiguration().isPubSubDomain());
        Assertions.assertFalse(jmsEndpoint.getEndpointConfiguration().isAutoStart());
        Assertions.assertFalse(jmsEndpoint.getEndpointConfiguration().isDurableSubscription());
        Assertions.assertFalse(jmsEndpoint.getEndpointConfiguration().isUseObjectMessages());
        Assertions.assertTrue(jmsEndpoint.getEndpointConfiguration().isFilterInternalHeaders());

        // 2nd message receiver
        jmsEndpoint = endpoints.get("jmsEndpoint2");
        Assertions.assertNotNull(jmsEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals(beanDefinitionContext.getBean("jmsConnectionFactory"), jmsEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals(beanDefinitionContext.getBean("messageConverter"), jmsEndpoint.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals(beanDefinitionContext.getBean("destinationResolver"), jmsEndpoint.getEndpointConfiguration().getDestinationResolver());
        Assertions.assertEquals(beanDefinitionContext.getBean("destinationNameResolver"), jmsEndpoint.getEndpointConfiguration().getDestinationNameResolver());
        Assertions.assertNull(jmsEndpoint.getEndpointConfiguration().getDestinationName());
        Assertions.assertNotNull(jmsEndpoint.getEndpointConfiguration().getDestination());
        Assertions.assertEquals(10000L, jmsEndpoint.getEndpointConfiguration().getTimeout());

        // 3rd message receiver
        jmsEndpoint = endpoints.get("jmsEndpoint3");
        Assertions.assertNull(jmsEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertNull(jmsEndpoint.getEndpointConfiguration().getDestinationName());
        Assertions.assertNull(jmsEndpoint.getEndpointConfiguration().getDestination());
        Assertions.assertTrue(jmsEndpoint.getEndpointConfiguration().isPubSubDomain());
        Assertions.assertTrue(jmsEndpoint.getEndpointConfiguration().isAutoStart());
        Assertions.assertTrue(jmsEndpoint.getEndpointConfiguration().isDurableSubscription());
        Assertions.assertEquals("durableSubscriber", jmsEndpoint.getEndpointConfiguration().getDurableSubscriberName());
        Assertions.assertTrue(jmsEndpoint.getEndpointConfiguration().isUseObjectMessages());
        Assertions.assertFalse(jmsEndpoint.getEndpointConfiguration().isFilterInternalHeaders());

        // 4th message receiver
        jmsEndpoint = endpoints.get("jmsEndpoint4");
        Assertions.assertNotNull(jmsEndpoint.getActor());
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), jmsEndpoint.getActor());
    }
}
