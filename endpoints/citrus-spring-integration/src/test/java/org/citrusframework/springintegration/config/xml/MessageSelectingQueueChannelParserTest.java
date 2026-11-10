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

import org.citrusframework.springintegration.channel.MessageSelectingQueueChannel;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class MessageSelectingQueueChannelParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testMessageSelectingQueueChannelParser() {
        Map<String, MessageSelectingQueueChannel> channels = beanDefinitionContext.getBeansOfType(MessageSelectingQueueChannel.class);

        Assertions.assertEquals(6, channels.size());

        // 1st channel
        Assertions.assertTrue(channels.containsKey("channel1"));

        // 2nd chanel with capacity
        MessageSelectingQueueChannel channel = channels.get("channel2");
        Assertions.assertEquals(5, channel.getRemainingCapacity());

        // 3rd chanel with polling interval
        channel = channels.get("channel3");
        Assertions.assertEquals(550, channel.getPollingInterval());

        // 4th channel
        Assertions.assertTrue(channels.containsKey("channel4"));

        // 5th chanel with capacity
        channel = channels.get("channel5");
        Assertions.assertEquals(5, channel.getRemainingCapacity());

        // 6th chanel with polling interval
        channel = channels.get("channel6");
        Assertions.assertEquals(550, channel.getPollingInterval());
    }
}
