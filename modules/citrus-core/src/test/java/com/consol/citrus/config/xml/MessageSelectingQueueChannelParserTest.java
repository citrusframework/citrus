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

package com.consol.citrus.config.xml;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.channel.MessageSelectingQueueChannel;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;

/**
 * @author Christoph Deppisch
 */
public class MessageSelectingQueueChannelParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testMessageSelectingQueueChannelParser() {
        Map<String, MessageSelectingQueueChannel> channels = beanDefinitionContext.getBeansOfType(MessageSelectingQueueChannel.class);
        
        Assert.assertEquals(channels.size(), 2);
        
        // 1st channel
        Assert.assertTrue(channels.containsKey("channel1"));
        
        // 2nd chanel with capacity
        MessageSelectingQueueChannel channel = channels.get("channel2");
        Assert.assertEquals(channel.getRemainingCapacity(), 5);
    }
}
