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
import org.citrusframework.springintegration.channel.ChannelEndpoint;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChannelEndpointParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testChannelEndpointParser() {
        Map<String, ChannelEndpoint> endpoints = beanDefinitionContext.getBeansOfType(ChannelEndpoint.class);

        Assertions.assertEquals(4, endpoints.size());

        // 1st message receiver
        ChannelEndpoint channelEndpoint = endpoints.get("channelEndpoint1");
        Assertions.assertEquals("channelName", channelEndpoint.getEndpointConfiguration().getChannelName());
        Assertions.assertNull(channelEndpoint.getEndpointConfiguration().getChannel());
        Assertions.assertEquals(5000L, channelEndpoint.getEndpointConfiguration().getTimeout());
        Assertions.assertNotNull(channelEndpoint.getEndpointConfiguration().getChannelResolver());
        Assertions.assertFalse(channelEndpoint.getEndpointConfiguration().isUseObjectMessages());
        Assertions.assertTrue(channelEndpoint.getEndpointConfiguration().isFilterInternalHeaders());

        // 2nd message receiver
        channelEndpoint = endpoints.get("channelEndpoint2");
        Assertions.assertNull(channelEndpoint.getEndpointConfiguration().getChannelName());
        Assertions.assertNotNull(channelEndpoint.getEndpointConfiguration().getChannel());
        Assertions.assertEquals(10000L, channelEndpoint.getEndpointConfiguration().getTimeout());
        Assertions.assertNull(channelEndpoint.getEndpointConfiguration().getChannelResolver());

        // 3rd message receiver
        channelEndpoint = endpoints.get("channelEndpoint3");
        Assertions.assertNull(channelEndpoint.getEndpointConfiguration().getChannelName());
        Assertions.assertNull(channelEndpoint.getEndpointConfiguration().getChannel());
        Assertions.assertNull(channelEndpoint.getEndpointConfiguration().getChannelResolver());

        // 4th message receiver
        channelEndpoint = endpoints.get("channelEndpoint4");
        Assertions.assertNotNull(channelEndpoint.getActor());
        Assertions.assertTrue(channelEndpoint.getEndpointConfiguration().isUseObjectMessages());
        Assertions.assertFalse(channelEndpoint.getEndpointConfiguration().isFilterInternalHeaders());
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), channelEndpoint.getActor());
    }
}
