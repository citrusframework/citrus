/*
 * Copyright 2006-2012 the original author or authors.
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

import org.citrusframework.channel.MessageSelectingQueueChannel;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class MessageSelectingQueueChannelParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testMessageSelectingQueueChannelParser() {
        Map<String, MessageSelectingQueueChannel> channels = beanDefinitionContext.getBeansOfType(MessageSelectingQueueChannel.class);
        
        Assert.assertEquals(channels.size(), 6);
        
        // 1st channel
        Assert.assertTrue(channels.containsKey("channel1"));
        
        // 2nd chanel with capacity
        MessageSelectingQueueChannel channel = channels.get("channel2");
        Assert.assertEquals(channel.getRemainingCapacity(), 5);
        
        // 3rd chanel with polling interval
        channel = channels.get("channel3");
        Assert.assertEquals(channel.getPollingInterval(), 550);

        // 4th channel
        Assert.assertTrue(channels.containsKey("channel4"));

        // 5th chanel with capacity
        channel = channels.get("channel5");
        Assert.assertEquals(channel.getRemainingCapacity(), 5);

        // 6th chanel with polling interval
        channel = channels.get("channel6");
        Assert.assertEquals(channel.getPollingInterval(), 550);
    }
}
