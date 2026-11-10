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

package org.citrusframework.springintegration.config.xml;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.springintegration.channel.ChannelSyncEndpoint;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.springframework.integration.core.MessagingTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChannelSyncEndpointParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testChannelSyncEndpointAsConsumerParser() {
        Map<String, ChannelSyncEndpoint> endpoints = beanDefinitionContext.getBeansOfType(ChannelSyncEndpoint.class);

        Assertions.assertEquals(3, endpoints.size());

        // 1st message receiver
        ChannelSyncEndpoint channelSyncEndpoint = endpoints.get("syncChannelEndpoint1");
        Assertions.assertEquals("channelName", channelSyncEndpoint.getEndpointConfiguration().getChannelName());
        Assertions.assertNull(channelSyncEndpoint.getEndpointConfiguration().getChannel());
        Assertions.assertEquals(5000L, channelSyncEndpoint.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(500L, channelSyncEndpoint.getEndpointConfiguration().getPollingInterval());
        Assertions.assertNotNull(channelSyncEndpoint.getEndpointConfiguration().getChannelResolver());
        Assertions.assertEquals(DefaultMessageCorrelator.class, channelSyncEndpoint.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertTrue(channelSyncEndpoint.getEndpointConfiguration().isFilterInternalHeaders());

        // 2nd message receiver
        channelSyncEndpoint = endpoints.get("syncChannelEndpoint2");
        Assertions.assertNull(channelSyncEndpoint.getEndpointConfiguration().getChannelName());
        Assertions.assertNotNull(channelSyncEndpoint.getEndpointConfiguration().getChannel());
        Assertions.assertEquals(10000L, channelSyncEndpoint.getEndpointConfiguration().getTimeout());
        Assertions.assertNull(channelSyncEndpoint.getEndpointConfiguration().getChannelResolver());
        Assertions.assertEquals(beanDefinitionContext.getBean("replyMessageCorrelator", MessageCorrelator.class), channelSyncEndpoint.getEndpointConfiguration().getCorrelator());

        // 3rd message receiver
        channelSyncEndpoint = endpoints.get("syncChannelEndpoint3");
        Assertions.assertNull(channelSyncEndpoint.getEndpointConfiguration().getChannelName());
        Assertions.assertNull(channelSyncEndpoint.getEndpointConfiguration().getChannel());
        Assertions.assertNull(channelSyncEndpoint.getEndpointConfiguration().getChannelResolver());
        Assertions.assertEquals(DefaultMessageCorrelator.class, channelSyncEndpoint.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertNotNull(channelSyncEndpoint.getEndpointConfiguration().getMessagingTemplate());
        Assertions.assertEquals(beanDefinitionContext.getBean("messagingTemplate", MessagingTemplate.class), channelSyncEndpoint.getEndpointConfiguration().getMessagingTemplate());
        Assertions.assertEquals(250L, channelSyncEndpoint.getEndpointConfiguration().getPollingInterval());
        Assertions.assertFalse(channelSyncEndpoint.getEndpointConfiguration().isFilterInternalHeaders());
        Assertions.assertNotNull(channelSyncEndpoint.getActor());
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), channelSyncEndpoint.getActor());
    }

}
