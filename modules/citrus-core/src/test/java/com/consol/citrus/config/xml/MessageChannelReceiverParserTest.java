/*
 * Copyright 2006-2010 the original author or authors.
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

import com.consol.citrus.TestActor;
import com.consol.citrus.channel.MessageChannelReceiver;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @deprecated since Citrus 1.4
 */
@Deprecated
public class MessageChannelReceiverParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testMessageChannelReceiverParser() {
        Map<String, MessageChannelReceiver> messageReceivers = beanDefinitionContext.getBeansOfType(MessageChannelReceiver.class);
        
        Assert.assertEquals(messageReceivers.size(), 4);
        
        // 1st message receiver
        MessageChannelReceiver messageReceiver = messageReceivers.get("messageChannelReceiver1");
        Assert.assertEquals(messageReceiver.getChannelName(), "channelName");
        Assert.assertNull(messageReceiver.getChannel());
        Assert.assertEquals(messageReceiver.getReceiveTimeout(), 5000L);
        Assert.assertNotNull(messageReceiver.getChannelResolver());
        
        // 2nd message receiver
        messageReceiver = messageReceivers.get("messageChannelReceiver2");
        Assert.assertNull(messageReceiver.getChannelName());
        Assert.assertNotNull(messageReceiver.getChannel());
        Assert.assertEquals(messageReceiver.getReceiveTimeout(), 10000L);
        Assert.assertNull(messageReceiver.getChannelResolver());
        
        // 3rd message receiver
        messageReceiver = messageReceivers.get("messageChannelReceiver3");
        Assert.assertNull(messageReceiver.getChannelName());
        Assert.assertNull(messageReceiver.getChannel());
        Assert.assertNull(messageReceiver.getChannelResolver());
        
        // 4th message receiver
        messageReceiver = messageReceivers.get("messageChannelReceiver4");
        Assert.assertNotNull(messageReceiver.getActor());
        Assert.assertEquals(messageReceiver.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }
}
