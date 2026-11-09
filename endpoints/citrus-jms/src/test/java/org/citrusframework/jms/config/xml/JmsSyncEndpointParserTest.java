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

import org.citrusframework.TestActor;
import org.citrusframework.jms.endpoint.JmsSyncEndpoint;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.Map;

public class JmsSyncEndpointParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testJmsSyncEndpointAsConsumerParser() {
        Map<String, JmsSyncEndpoint> endpoints = beanDefinitionContext.getBeansOfType(JmsSyncEndpoint.class);

        Assertions.assertEquals(4, endpoints.size());

        // 1st message receiver
        JmsSyncEndpoint jmsSyncEndpoint = endpoints.get("jmsSyncEndpoint1");
        Assertions.assertNotNull(jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals(beanDefinitionContext.getBean("connectionFactory"), jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals("JMS.Queue.Test", jmsSyncEndpoint.getEndpointConfiguration().getDestinationName());
        Assertions.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getDestination());
        Assertions.assertEquals(5000L, jmsSyncEndpoint.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(DefaultMessageCorrelator.class, jmsSyncEndpoint.getEndpointConfiguration().getCorrelator().getClass());

        // 2nd message receiver
        jmsSyncEndpoint = endpoints.get("jmsSyncEndpoint2");
        Assertions.assertNotNull(jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals(beanDefinitionContext.getBean("jmsConnectionFactory"), jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getDestinationName());
        Assertions.assertNotNull(jmsSyncEndpoint.getEndpointConfiguration().getDestination());
        Assertions.assertEquals(10000L, jmsSyncEndpoint.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(beanDefinitionContext.getBean("replyMessageCorrelator", MessageCorrelator.class), jmsSyncEndpoint.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(beanDefinitionContext.getBean("destinationResolver"), jmsSyncEndpoint.getEndpointConfiguration().getDestinationResolver());
        Assertions.assertEquals(beanDefinitionContext.getBean("destinationNameResolver"), jmsSyncEndpoint.getEndpointConfiguration().getDestinationNameResolver());

        // 3rd message receiver
        jmsSyncEndpoint = endpoints.get("jmsSyncEndpoint3");
        Assertions.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getDestinationName());
        Assertions.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getDestination());
        Assertions.assertEquals(beanDefinitionContext.getBean("replyMessageCorrelator", MessageCorrelator.class), jmsSyncEndpoint.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(true, jmsSyncEndpoint.getEndpointConfiguration().isPubSubDomain());

        // 4th message receiver
        jmsSyncEndpoint = endpoints.get("jmsSyncEndpoint4");
        Assertions.assertNotNull(jmsSyncEndpoint.getActor());
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), jmsSyncEndpoint.getActor());
    }

    @Test
    public void testJmsSyncEndpointAsProducerParser() {
        ApplicationContext producerContext = createApplicationContext("context2");

        Map<String, JmsSyncEndpoint> endpoints = producerContext.getBeansOfType(JmsSyncEndpoint.class);

        Assertions.assertEquals(4, endpoints.size());

        // 1st message sender
        JmsSyncEndpoint jmsSyncEndpoint = endpoints.get("jmsSyncEndpoint1");
        Assertions.assertNotNull(jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals(producerContext.getBean("connectionFactory"), jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals("JMS.Queue.Test", jmsSyncEndpoint.getEndpointConfiguration().getDestinationName());
        Assertions.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getDestination());
        Assertions.assertEquals(5000L, jmsSyncEndpoint.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(500L, jmsSyncEndpoint.getEndpointConfiguration().getPollingInterval());
        Assertions.assertEquals("JMS.Reply.Queue", jmsSyncEndpoint.getEndpointConfiguration().getReplyDestinationName());
        Assertions.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getReplyDestination());
        Assertions.assertEquals(DefaultMessageCorrelator.class, jmsSyncEndpoint.getEndpointConfiguration().getCorrelator().getClass());

        // 2nd message sender
        jmsSyncEndpoint = endpoints.get("jmsSyncEndpoint2");
        Assertions.assertNotNull(jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertEquals(producerContext.getBean("jmsConnectionFactory"), jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getDestinationName());
        Assertions.assertNotNull(jmsSyncEndpoint.getEndpointConfiguration().getDestination());
        Assertions.assertEquals(10000L, jmsSyncEndpoint.getEndpointConfiguration().getTimeout());
        Assertions.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getReplyDestinationName());
        Assertions.assertNotNull(jmsSyncEndpoint.getEndpointConfiguration().getReplyDestination());
        Assertions.assertEquals(producerContext.getBean("replyMessageCorrelator", MessageCorrelator.class), jmsSyncEndpoint.getEndpointConfiguration().getCorrelator());

        // 3rd message sender
        jmsSyncEndpoint = endpoints.get("jmsSyncEndpoint3");
        Assertions.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory());
        Assertions.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getDestinationName());
        Assertions.assertNull(jmsSyncEndpoint.getEndpointConfiguration().getDestination());
        Assertions.assertEquals(true, jmsSyncEndpoint.getEndpointConfiguration().isPubSubDomain());
        Assertions.assertEquals(producerContext.getBean("replyMessageCorrelator", MessageCorrelator.class), jmsSyncEndpoint.getEndpointConfiguration().getCorrelator());

        // 4th message sender
        jmsSyncEndpoint = endpoints.get("jmsSyncEndpoint4");
        Assertions.assertNotNull(jmsSyncEndpoint.getEndpointConfiguration().getPollingInterval());
        Assertions.assertEquals(250L, jmsSyncEndpoint.getEndpointConfiguration().getPollingInterval());
        Assertions.assertNotNull(jmsSyncEndpoint.getActor());
        Assertions.assertEquals(producerContext.getBean("testActor", TestActor.class), jmsSyncEndpoint.getActor());
    }
}
