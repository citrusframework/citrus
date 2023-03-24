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

package org.citrusframework.config.xml;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.channel.ChannelSyncEndpoint;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.springframework.integration.core.MessagingTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class ChannelSyncEndpointParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testChannelSyncEndpointAsConsumerParser() {
        Map<String, ChannelSyncEndpoint> endpoints = beanDefinitionContext.getBeansOfType(ChannelSyncEndpoint.class);

        Assert.assertEquals(endpoints.size(), 3);

        // 1st message receiver
        ChannelSyncEndpoint channelSyncEndpoint = endpoints.get("syncChannelEndpoint1");
        Assert.assertEquals(channelSyncEndpoint.getEndpointConfiguration().getChannelName(), "channelName");
        Assert.assertNull(channelSyncEndpoint.getEndpointConfiguration().getChannel());
        Assert.assertEquals(channelSyncEndpoint.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(channelSyncEndpoint.getEndpointConfiguration().getPollingInterval(), 500L);
        Assert.assertNotNull(channelSyncEndpoint.getEndpointConfiguration().getChannelResolver());
        Assert.assertEquals(channelSyncEndpoint.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertTrue(channelSyncEndpoint.getEndpointConfiguration().isFilterInternalHeaders());

        // 2nd message receiver
        channelSyncEndpoint = endpoints.get("syncChannelEndpoint2");
        Assert.assertNull(channelSyncEndpoint.getEndpointConfiguration().getChannelName());
        Assert.assertNotNull(channelSyncEndpoint.getEndpointConfiguration().getChannel());
        Assert.assertEquals(channelSyncEndpoint.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertNull(channelSyncEndpoint.getEndpointConfiguration().getChannelResolver());
        Assert.assertEquals(channelSyncEndpoint.getEndpointConfiguration().getCorrelator(), beanDefinitionContext.getBean("replyMessageCorrelator", MessageCorrelator.class));

        // 3rd message receiver
        channelSyncEndpoint = endpoints.get("syncChannelEndpoint3");
        Assert.assertNull(channelSyncEndpoint.getEndpointConfiguration().getChannelName());
        Assert.assertNull(channelSyncEndpoint.getEndpointConfiguration().getChannel());
        Assert.assertNull(channelSyncEndpoint.getEndpointConfiguration().getChannelResolver());
        Assert.assertEquals(channelSyncEndpoint.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertNotNull(channelSyncEndpoint.getEndpointConfiguration().getMessagingTemplate());
        Assert.assertEquals(channelSyncEndpoint.getEndpointConfiguration().getMessagingTemplate(), beanDefinitionContext.getBean("messagingTemplate", MessagingTemplate.class));
        Assert.assertEquals(channelSyncEndpoint.getEndpointConfiguration().getPollingInterval(), 250L);
        Assert.assertFalse(channelSyncEndpoint.getEndpointConfiguration().isFilterInternalHeaders());
        Assert.assertNotNull(channelSyncEndpoint.getActor());
        Assert.assertEquals(channelSyncEndpoint.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }


}
